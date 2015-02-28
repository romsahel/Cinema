/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import files.Database;
import files.FileWalker;
import files.Settings;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import main.MainController;
import netscape.javascript.JSObject;
import utils.Formatter;
import utils.Utils;

/**
 *
 * @author Romsahel
 */
public final class CWebEngine
{

	private static final ArrayList<Media> medias = new ArrayList<>();
	private static ExecutorService executor = null;
	private static WebEngine webEngine;
	private static int newItems;
	private static final MainController controller = MainController.getInstance();

	public CWebEngine(WebView webBrowser)
	{
		initExecutor();
		// Obtain the webEngine to navigate
		webEngine = webBrowser.getEngine();
		webEngine.load("file:///" + new File(Utils.APPDATA + "public_html/index.html").getAbsolutePath().replace('\\', '/'));
		webEngine.getLoadWorker().stateProperty().addListener(new CChangeListener());

		((JSObject) webEngine.executeScript("window")).setMember("app", new JsToJava());

		walkFiles();
	}

	public static void walkFiles()
	{
		controller.startLoading();
		if (executor.isShutdown())
			initExecutor();

		medias.clear();
		THashMap<String, Location> locations = Settings.getInstance().getLocations();
		for (Map.Entry<String, Location> entrySet : locations.entrySet())
		{
			String key = entrySet.getKey();
			Location value = entrySet.getValue();
			medias.addAll(FileWalker.getInstance().walk(key, value));
		}

		newItems = medias.size();
		if (medias != null && newItems > 0)
			getImages(medias);

		final Collection<Media> values = Database.getInstance().getDatabase().values();
		for (Media value : values)
			if (value != null)
			{
				final THashMap<String, String> info = value.getInfo();
				info.remove("loading");
				if (info.remove("error") != null)
					threadedGetImage(value);
				if (locations.contains(info.get("location")))
					medias.add(value);
			}

		executor.shutdown();
	}

	private static void initExecutor()
	{
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println(cores + " cores available!");
		executor = java.util.concurrent.Executors.newFixedThreadPool((cores <= 2) ? 1 : cores - 1);
	}

	public static void refreshList()
	{
		Utils.callFuncJS(webEngine, "emptyMediaList");

		for (Media o : medias)
		{
			if (o == null)
				continue;
			//	adds the media to the database
			Database.getInstance().getDatabase().put(o.getId(), o);
			Utils.callFuncJS(webEngine, "addMedia", Long.toString(o.getId()), o.toJSArray());
		}
		Database.getInstance().writeDatabase();

		final THashMap<String, String> general = Settings.getInstance().getGeneral();
		Utils.callFuncJS(webEngine, "sortMediaList");
		Utils.callFuncJS(webEngine, "$(\"#locationsList > li\")[0].click");
		Utils.callFuncJS(webEngine, "setSelection", general.get("currentMedia"), general.get("currentSeason"), general.get("currentEpisode"));

		Thread t = new Thread((Runnable) () ->
		{
			try
			{
				executor.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException ex)
			{
				Logger.getLogger(CWebEngine.class.getName()).log(Level.SEVERE, null, ex);
			}
			mergeByPoster();
			newItems = 0;

			controller.stopLoading();
			Database.getInstance().writeDatabase();
		});
		t.start();
	}

	private static void getImages(ArrayList<Media> list)
	{
		for (Media o : list)
		{
			if (o == null)
				continue;
			threadedGetImage(o);
		}
	}

	private static void threadedGetImage(Media o)
	{
		Thread t = new Thread()
		{
			@Override
			public void run()
			{
				final String previousImg = o.getInfo().get("img");
				if (previousImg == null)
				{
					Logger.getLogger(CWebEngine.class.getName()).log(Level.INFO, "Downloading info for: " + o.getInfo().get("name"));
					final String newImg = o.downloadInfos();
					if (newImg != null && Database.getInstance().getDatabase().containsKey(o.getId()))
						Utils.callFuncJS(webEngine, "updateMedia", Long.toString(o.getId()), o.toJSArray());

					Logger.getLogger(CWebEngine.class.getName()).log(Level.INFO, "Img downloaded: " + o.getInfo().get("name"));
				}
			}
		};
		executor.execute(t);
	}

	public static void mergeByPoster()
	{
		if ("0".equals(Settings.getInstance().getGeneral().get("automerge")))
			return;
		ArrayList<Media> deleted = new ArrayList<>();
		THashMap<Long, Media> changed = new THashMap<>();
		for (Media outer : medias)
		{
			if (outer == null || deleted.contains(outer))
				continue;
			final TreeMap<String, TreeMap<String, Episode>> seasons = outer.getSeasons();
			final THashMap<String, String> outInfo = outer.getInfo();
			String outSeason = null;
			for (Media inner : medias)
				if (inner != null && !deleted.contains(inner) && outer != inner)
				{
					final THashMap<String, String> inInfo = inner.getInfo();
					if (!inInfo.get("location").equals(outInfo.get("location")))
						continue;
					final String outImg = outInfo.get("img");
					if (outImg != null && outImg.equals(inInfo.get("img")))
					{
						if (outSeason == null)
						{
							// First time we merge the outer media with another
							// We format the name (remove season, etc)
							final String outName = outInfo.get("name");
							outSeason = Formatter.getFormattedSeason(outName, outName);
							outInfo.put("name", Formatter.removeSeason(outName));

							if (seasons.size() == 1)
							{
								final TreeMap<String, Episode> firstSeason = outer.getFirstSeason();
								seasons.clear();
								seasons.put(outSeason, firstSeason);
							}
						}
						String inSeason = null;
						if (inner.getSeasons().size() == 1)
							inSeason = Formatter.getFormattedSeason(inInfo.get("name"));

						for (Map.Entry<String, TreeMap<String, Episode>> entrySet : inner.getSeasons().entrySet())
						{
							String key = (inSeason != null) ? inSeason : entrySet.getKey();
							TreeMap<String, Episode> value = entrySet.getValue();
							seasons.put(key, value);
						}
						deleted.add(inner);
						changed.put(outer.getId(), outer);
						Database.getInstance().removeMedia(true, inner);
					}
				}
		}

		if (deleted.size() > 0)
		{
			StringBuilder builder = new StringBuilder();
			builder.append("\\[");
			for (Media m : deleted)
				builder.append('"').append(m.getId()).append('"').append(',');

			builder.append("], [");
			for (Map.Entry<Long, Media> entrySet : changed.entrySet())
			{
				final Media m = entrySet.getValue();
				builder.append(m.toJSArray().substring(1)).append(", ");
			}
			builder.append("]");
			Utils.callFuncJS(webEngine, "mergeAndUpdate", builder.toString());

			medias.removeAll(deleted);
		}
	}

	public static String longestCommonPrefix(String... strings)
	{
		if (strings.length == 0)
			return ""; // Or maybe return null?

		for (int prefixLen = 0; prefixLen < strings[0].length(); prefixLen++)
		{
			char c = strings[0].charAt(prefixLen);
			for (int i = 1; i < strings.length; i++)
				if (prefixLen >= strings[i].length()
					|| strings[i].charAt(prefixLen) != c)
					// Mismatch found
					return strings[i].substring(0, prefixLen);
		}
		return strings[0];
	}

	/**
	 * @return the webEngine
	 */
	public static WebEngine getWebEngine()
	{
		return webEngine;
	}

	// <editor-fold defaultstate="collapsed" desc="Inside class">
	private static class CChangeListener implements javafx.beans.value.ChangeListener<Worker.State>
	{

		@Override
		public void changed(ObservableValue<? extends Worker.State> observable,
							Worker.State oldValue, Worker.State newValue)
		{
			if (newValue != Worker.State.SUCCEEDED)
				return;

			main.Main.setReady(true);
			for (Map.Entry<String, Location> next : Settings.getInstance().getLocations().entrySet())
				Utils.callFuncJS(webEngine, "addLocation", next.getKey());

			(new Thread(() ->
			 {
				 refreshList();
			})).start();
		}
	}
  // </editor-fold>
}

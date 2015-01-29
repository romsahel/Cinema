/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

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
import utils.Utils;
import videomanagerjava.files.Database;
import videomanagerjava.files.FileWalker;
import videomanagerjava.files.Settings;

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

	public CWebEngine(WebView webBrowser)
	{	// Obtain the webEngine to navigate
		webEngine = webBrowser.getEngine();
		webEngine.load("file:///" + new File(Utils.APPDATA + "public_html/index.html").getAbsolutePath().replace('\\', '/'));
		webEngine.getLoadWorker().stateProperty().addListener(new CChangeListener());

		((JSObject) webEngine.executeScript("window")).setMember("app", new JsToJava());

		walkFiles();
	}

	public static void walkFiles()
	{
		medias.clear();
		THashMap<String, Location> locations = Settings.getInstance().getLocations();
		for (Map.Entry<String, Location> entrySet : locations.entrySet())
		{
			String key = entrySet.getKey();
			Location value = entrySet.getValue();
			medias.addAll(FileWalker.getInstance().walk(key, value));
		}

		newItems = medias.size();
		getImages();

		final Collection<Media> values = Database.getInstance().getDatabase().values();
		for (Media value : values)
			if (value != null && locations.contains(value.getInfo().get("location")))
				medias.add(value);
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
		Utils.callFuncJS(webEngine, "setSelection", general.get("currentMedia"), general.get("currentSeason"), general.get("currentEpisode"));
		Utils.callFuncJS(webEngine, "setToggles", general.get("playList"), general.get("withSubtitles"));
		Utils.callFuncJS(webEngine, "sortMediaList");
		Utils.callFuncJS(webEngine, "$(\"#locationsList > li\")[0].click");

		MainController.stopLoading();
		if (newItems > 0)
		{
			try
			{
				executor.awaitTermination(1, TimeUnit.DAYS);
			} catch (InterruptedException ex)
			{
				Logger.getLogger(CWebEngine.class.getName()).log(Level.SEVERE, null, ex);
			}
			mergeByPoster();
			newItems = 0;
		}
	}

	private static void getImages()
	{
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println(cores + " cores available!");
		executor = java.util.concurrent.Executors.newFixedThreadPool(cores);
		if (medias == null)
			return;

		for (Media o : medias)
		{
			if (o == null)
				continue;
			Thread t = new Thread()
			{
				@Override
				public void run()
				{
					final String previousImg = o.getInfo().get("img");
					if (previousImg == null)
					{
						final String newImg = o.downloadInfos();
						if (newImg != null && Database.getInstance().getDatabase().containsKey(o.getId()))
							Utils.callFuncJS(webEngine, "updateMedia", Long.toString(o.getId()), o.toJSArray());

						Logger.getLogger(CWebEngine.class.getName()).log(Level.INFO, "Img downloaded: {0}", o.getInfo().get("name"));
					}
				}
			};
			executor.execute(t);
		}
		executor.shutdown();
	}

	public static void mergeByPoster()
	{
		final THashMap<Long, Media> database = Database.getInstance().getDatabase();
		ArrayList<Media> toDelete = new ArrayList<>();
		for (Media outer : medias)
		{
			if (outer == null || toDelete.contains(outer))
				continue;
			final TreeMap<String, TreeMap<String, Episode>> seasons = outer.getSeasons();
			final THashMap<String, String> outInfo = outer.getInfo();
			String outSeason = null;
			for (Media inner : medias)
				if (inner != null && !toDelete.contains(inner) && outer != inner)
				{
					final THashMap<String, String> inInfo = inner.getInfo();
					if (outInfo.get("img").equals(inInfo.get("img")))
					{
						if (outSeason == null)
							if (seasons.size() == 1)
							{
								final String outName = outInfo.get("name");
								outSeason = Utils.getFormattedSeason(outName, outName);
								final TreeMap<String, Episode> firstSeason = outer.getFirstSeason();
								seasons.clear();
								seasons.put(outSeason, firstSeason);
							}

						final String inName = inInfo.get("name");
						String inSeason = Utils.getFormattedSeason(inName, inName);
						final TreeMap<String, Episode> firstSeason = inner.getFirstSeason();
						seasons.put(inSeason, firstSeason);
						toDelete.add(inner);
						database.put(inner.getId(), null);
					}
				}
		}
		medias.removeAll(toDelete);
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

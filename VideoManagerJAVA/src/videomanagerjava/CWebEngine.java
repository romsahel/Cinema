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
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
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
		webEngine.load("file:///" + new File("public_html/index.html").getAbsolutePath().replace('\\', '/'));
		webEngine.getLoadWorker().stateProperty().addListener(new CChangeListener());

		((JSObject) webEngine.executeScript("window")).setMember("app", new JsToJava());

		walkFiles();
	}

	public static void walkFiles()
	{
		walkFiles(null, null);
	}

	public static void walkFiles(String name, String path)
	{
		medias.clear();
		THashMap<String, String> locations = null;
		if (name != null && path != null)
			medias.addAll(FileWalker.getInstance().walk(name, path));
		else
		{
			locations = Settings.getInstance().getLocations();
			for (Map.Entry<String, String> entrySet : locations.entrySet())
			{
				String key = entrySet.getKey();
				String value = entrySet.getValue();
				medias.addAll(FileWalker.getInstance().walk(key, value));
			}
		}

		newItems = medias.size();
		getImages();

		final Collection<Media> values = Database.getInstance().getDatabase().values();
		if (name != null && path != null)
		{
			for (Media value : values)
				if (value != null && name.equals(value.getInfo().get("location")))
					medias.add(value);
		}
		else if (locations != null)
			for (Media value : values)
				if (value != null && locations.contains(value.getInfo().get("location")))
					medias.add(value);
	}

	public static void refreshList()
	{
		Utils.callFuncJS(webEngine, "emptyMediaList");

		//	wait for the info-getting job to be terminated
		while (executor == null);
		try
		{
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e)
		{
		}

		if (newItems > 0)
			mergeByPoster();
		newItems = 0;

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
	}

	private static void getImages()
	{
		executor = java.util.concurrent.Executors.newFixedThreadPool(4);
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
					if (o.getInfo().get("img") == null)
						o.downloadInfos();
				}
			};
			executor.execute(t);
		}
		executor.shutdown();
	}

	private static void mergeByPoster()
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

			for (Map.Entry<String, String> next : Settings.getInstance().getLocations().entrySet())
				Utils.callFuncJS(webEngine, "addLocation", next.getKey());

			(new Thread(() ->
			 {
				 refreshList();
			})).start();
		}
	}
  // </editor-fold>
}

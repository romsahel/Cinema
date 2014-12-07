/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
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
	private static ExecutorService executor;
	private static WebEngine webEngine;

	public CWebEngine(WebView webBrowser)
	{	// Obtain the webEngine to navigate
		webEngine = webBrowser.getEngine();
		webEngine.load("file:///" + new File("public_html/index.html").getAbsolutePath().replace('\\', '/'));
		webEngine.getLoadWorker().stateProperty().addListener(new CChangeListener());

		((JSObject) webEngine.executeScript("window")).setMember("app", new JsToJava());

		walkFiles();

	}

	private static String[] getLocations()
	{
		final THashMap<String, String> locations = Settings.getInstance().getLocations();
		String[] array = new String[locations.size()];
		return locations.values().toArray(array);
	}

	public static void walkFiles(String... locations)
	{
		locations = locations.length == 0 ? getLocations() : locations;
		medias.clear();
		for (String location : locations)
			if (location != null)
				medias.addAll(FileWalker.getInstance().walk(location));

		medias.addAll(Database.getInstance().getDatabase().values());

		getImages();
	}

	public static void refreshList()
	{
		Utils.callFuncJS(webEngine, "emptyMediaList");

		//	wait for the info-getting job to be terminated
		while (!executor.isTerminated());

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

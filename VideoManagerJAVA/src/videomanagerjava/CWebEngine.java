/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import videomanagerjava.files.FileWalker;
import videomanagerjava.files.Settings;
import videomanagerjava.files.Database;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

/**
 *
 * @author Romsahel
 */
public class CWebEngine
{

	private final ArrayList<Media> medias;
	private ExecutorService executor;

	public CWebEngine(WebView webBrowser)
	{	// Obtain the webEngine to navigate
		final WebEngine webEngine = webBrowser.getEngine();
		webEngine.load("file:///" + new File("public_html/index.html").getAbsolutePath().replace('\\', '/'));
		webEngine.getLoadWorker().stateProperty().addListener(new CChangeListener(webEngine));

		((JSObject) webEngine.executeScript("window")).setMember("app", new DatabaseJS());

		medias = FileWalker.getInstance().walk(Settings.getInstance().getLocations().get("Vidéos"));
		getImages();

	}

	private void pageLoaded(WebEngine webEngine)
	{
		//	wait for the info-getting job to be terminated
		while (!executor.isTerminated());

		for (Media o : medias)
		{
			//	adds the media to the database
			Database.getInstance().getDatabase().put(o.getId(), o);
			StringBuilder sb = new StringBuilder();

			//	create an array with all the needed data: first general info
			sb.append(String.format("'%s': '%s',", "id", o.getId()));
			for (Map.Entry<String, String> entrySet : o.getInfo().entrySet())
				sb.append(String.format("'%s': '%s',", entrySet.getKey(), entrySet.getValue().replace("'", "\\'")));

			//	appends the season episodes
			sb.append("'seasons': {");

			for (Map.Entry<String, HashMap<String, String>> season : o.getSeasons().entrySet())
			{
				sb.append(String.format("'%s': {", season.getKey()));
				for (Map.Entry<String, String> entrySet : season.getValue().entrySet())
					sb.append(String.format("'%s': '%s',", entrySet.getKey(), entrySet.getValue().replace("'", "\\'").replace("\\", "\\\\")));
				sb.append(" }, ");
			}
			sb.append("}");

			String array = "{" + sb.toString() + "}";
			Utils.callJS(webEngine, "addMedia", Long.toString(o.getId()), "\\" + array);

			break;
		}

		for (Map.Entry<String, String> next : Settings.getInstance().getLocations().entrySet())
			Utils.callJS(webEngine, "addLocation", next.getKey());

		Database.getInstance().writeDatabase();
	}

	private void getImages()
	{
		executor = java.util.concurrent.Executors.newFixedThreadPool(4);
		if (medias == null)
			return;

		for (Media o : medias)
		{
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

	// <editor-fold defaultstate="collapsed" desc="Inside class">
	private class CChangeListener implements javafx.beans.value.ChangeListener<Worker.State>
	{

		private final WebEngine webEngine;

		public CChangeListener(WebEngine webEngine)
		{
			this.webEngine = webEngine;
		}

		@Override
		public void changed(ObservableValue<? extends Worker.State> observable,
							Worker.State oldValue, Worker.State newValue)
		{
			if (newValue != Worker.State.SUCCEEDED)
				return;
			(new Thread(() ->
			 {
				 pageLoaded(webEngine);
			})).start();
		}
	}
  // </editor-fold>
}

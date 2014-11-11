/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.json.simple.JSONValue;
import videomanagerjava.files.Database;
import videomanagerjava.files.FileWalker;
import videomanagerjava.files.Settings;

/**
 *
 * @author Romsahel
 */
public final class CWebEngine
{

	private final ArrayList<Media> medias = new ArrayList<>();
	private ExecutorService executor;
	private final WebEngine webEngine;

	public CWebEngine(WebView webBrowser)
	{	// Obtain the webEngine to navigate
		webEngine = webBrowser.getEngine();
		webEngine.load("file:///" + new File("public_html/index.html").getAbsolutePath().replace('\\', '/'));
		webEngine.getLoadWorker().stateProperty().addListener(new CChangeListener(getWebEngine()));

		((JSObject) webEngine.executeScript("window")).setMember("app", new JsToJava(getWebEngine()));

		for (Map.Entry<String, String> entrySet : Settings.getInstance().getLocations().entrySet())
		{
			String value = entrySet.getValue();
			medias.addAll(FileWalker.getInstance().walk(value));
		}
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
			sb.append(String.format("\"%s\": \"%s\",", "id", o.getId()));
			sb.append("\"info\": ").append(JSONValue.toJSONString(o.getInfo()));

			//	appends the season episodes
			sb.append(", \"seasons\": ").append(JSONValue.toJSONString(o.getSeasons()));

			String array = "{" + sb.toString() + "}";
			Pattern pattern = Pattern.compile("genres\":\"\\[(.*)\\]\"");
			Matcher matcher = pattern.matcher(array);
			if (matcher.find())
				array = array.replace(matcher.group(), "genres\": [" + matcher.group(1).replace("\\\"", "\"") + "]");
			
			Utils.callJS(webEngine, "addMedia", Long.toString(o.getId()), "\\" + array);
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

	/**
	 * @return the webEngine
	 */
	public WebEngine getWebEngine()
	{
		return webEngine;
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

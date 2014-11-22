/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.RequestUtils;
import videomanagerjava.files.Database;

/**
 *
 * @author Romsahel
 */
public class VLCController
{

	static Timer timer = null;
	static TimerTask timerTask = null;
	static Episode currentEpisode;

	public static void play(final Episode episode)
	{
		if (currentEpisode != null)
			cancelTimer(false, currentEpisode.getProperties());

		currentEpisode = episode;

		final HashMap<String, String> properties = episode.getProperties();
		final String path = properties.get("path");
		String parameter = RequestUtils.getInstance().pathToUrl(path);

//		If the request is null, we must launch VLC and wait for it to be prepared
		if (parameter == null || sendRequest("command=pl_empty") == null)
			runVLC(path);
		else
			sendRequest(parameter);

		System.out.println("Waiting for VLC and getting filename");
		String filename = null;
		while (filename == null)
			filename = (String) getNestedObject(sendRequest("command=seek&val=" + properties.get("time")),
												"information", "category", "meta", "filename");
		System.out.println(filename);
		properties.put("seen", "true");
		timer = new Timer();
		timerTask = new TimerTaskImpl(properties, filename);
		timer.scheduleAtFixedRate(timerTask, 5000, 10000);
	}

	public static boolean cancelTimer(boolean checkStatus)
	{
		if (currentEpisode == null)
			return false;

		return cancelTimer(checkStatus, currentEpisode.getProperties());
	}

	public static boolean cancelTimer(boolean checkStatus, HashMap<String, String> properties)
	{
		if (timer != null && timerTask != null)
		{
			timerTask.cancel();
			timer.cancel();

			if (checkStatus)
			{
				final String status = sendRequest(null);
				if (status != null)
					setEpisodeTime(status, properties);
			}
			Database.getInstance().writeDatabase();
			return true;
		}
		return false;
	}

	private static JSONObject setEpisodeTime(final String status, final HashMap<String, String> properties)
	{
		JSONObject obj = getJSONObject(status);
		if (obj != null)
			properties.put("time", Long.toString((long) obj.get("time")));
		return obj;
	}

	private static Object getNestedObject(String obj, String... keys)
	{
		return getNestedObject(getJSONObject(obj), keys);
	}

	private static Object getNestedObject(JSONObject obj, String... keys)
	{
		if (obj == null)
			return null;

		for (String key : keys)
		{
			final Object value = obj.get(key);
			if (value != null && value.getClass() == JSONObject.class)
				obj = (JSONObject) value;
			else
				return value;
		}
		return obj;
	}

	private static JSONObject getJSONObject(String json)
	{
		JSONObject obj = null;
		if (json != null)
			try
			{
				obj = (JSONObject) new JSONParser().parse(json);
			} catch (ParseException ex)
			{
				Logger.getLogger(VLCController.class.getName()).log(Level.SEVERE, null, ex);
			}
		return obj;
	}

	private static void runVLC(String file)
	{
		System.out.println("Launching VLC and adding file to playlist.");
		try
		{
			final String vlc = "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe";
			final String cmd = String.format("\"%s\" \"%s\"", vlc, file);
			System.out.println(cmd);
			final ProcessBuilder process = new ProcessBuilder(cmd);
			process.start();
		} catch (IOException ex)
		{
			Logger.getLogger(VLCController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static String sendRequest(String parameter)
	{
		return RequestUtils.getInstance().sendGetRequest(parameter);
	}

	private static class TimerTaskImpl extends TimerTask
	{

		private final HashMap<String, String> properties;
		private final String filename;

		public TimerTaskImpl(HashMap<String, String> properties, String filename)
		{
			this.properties = properties;
			this.filename = filename;
		}

		@Override
		public void run()
		{
			final String status = sendRequest(null);
			if (status == null)
			{
				cancelTimer(false, properties);
				return;
			}

			JSONObject obj = setEpisodeTime(status, properties);
			String name = "";
			if (obj != null)
				name = (String) getNestedObject(obj, "information", "category", "meta", "filename");

			if (!name.equals(filename))
				cancelTimer(false, properties);
		}
	}
}

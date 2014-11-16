/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.io.IOException;
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

	public static void play(final Episode episode)
	{
		final String file = episode.getPath();
		String parameter = String.format("command=in_play&input=%s?command=pl_empty", file);
//		If the request is null, we must launch VLC and wait for it to be prepared
		if (sendRequest(null) == null)
			runVLC(file);
		else
			sendRequest(parameter);

		final String filename = (String) getNestedObject(sendRequest("command=seek&val=" + episode.getTime()),
														 "information", "category", "meta", "filename");
		cancelTimer();
		timer = new Timer();
		timerTask = new TimerTask()
		{
			int i = 0;

			@Override
			public void run()
			{
				final String status = sendRequest(null);
				if (status == null)
				{
					cancelTimer();
					return;
				}
				try
				{
					JSONObject obj = (JSONObject) new JSONParser().parse(status);
					episode.setTime((long) obj.get("time"));

					String name = (String) getNestedObject(obj, "information", "category", "meta", "filename");

					if (!name.equals(filename))
						cancelTimer();

				} catch (ParseException ex)
				{
					Logger.getLogger(VLCController.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		};
		timer.scheduleAtFixedRate(timerTask, 0, 10000);
	}

	public static boolean cancelTimer()
	{
		if (timer != null && timerTask != null)
		{
			timerTask.cancel();
			timer.cancel();
			Database.getInstance().writeDatabase();
			return true;
		}
		return false;
	}

	private static Object getNestedObject(String json, String... keys)
	{
		JSONObject obj;
		try
		{
			obj = (JSONObject) new JSONParser().parse(json);
		} catch (ParseException ex)
		{
			Logger.getLogger(VLCController.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
		return getNestedObject(obj, keys);
	}

	private static Object getNestedObject(JSONObject obj, String... keys)
	{
		for (String key : keys)
		{
			final Object value = obj.get(key);
			if (value.getClass() == JSONObject.class)
				obj = (JSONObject) value;
			else
				return value;
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
			while (sendRequest(null) == null)
				System.out.println("Waiting");
		} catch (IOException ex)
		{
			Logger.getLogger(VLCController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static String sendRequest(String parameter)
	{
		return RequestUtils.getInstance().sendGetRequest(parameter);
	}
}

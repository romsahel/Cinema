/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import files.Database;
import files.Settings;
import gnu.trove.map.hash.THashMap;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.MainController;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.RequestUtils;
import utils.Utils;

/**
 *
 * @author Romsahel
 */
public class VLCController
{

	private static Timer timer = null;
	private static TimerTaskImpl timerTask = null;
	private static Episode currentEpisode;
	private static long firstId = -1;
	private static long currentId = -1;
	private static Episode[] followingEpisodes;
	private static final MainController controller = MainController.getInstance();

	private static void init()
	{
		if (currentEpisode != null)
			cancelTimer(false, currentEpisode.getProperties());

		firstId = -1;
		currentId = -1;
		currentEpisode = null;
		followingEpisodes = null;
	}

	public static void playAllFollowing(Episode[] array, boolean withSubtitles)
	{
		final Thread thread = new Thread(() ->
		{
			playEpisode(array[0], withSubtitles);
			followingEpisodes = array;
			final String command = "command=in_enqueue&input=";
			for (int i = 1; i < array.length; i++)
			{
				final THashMap<String, String> properties = array[i].getProperties();
				if (sendRequest(command + encodePath(properties.get("path"))) == null)
					break;

				String path = getPath(properties);
				if (withSubtitles)
					getSubtitles(path, true);
			}
		});

		controller.startLoading(thread, "Launching media");
		thread.start();

	}

	private static String getPath(final THashMap<String, String> properties)
	{
		String path = properties.get("path");
		if (utils.Utils.isWindows)
			path = path.replace("/", "\\");
		return path;
	}

	public static void play(final Episode episode, boolean withSubtitles)
	{
		final Thread thread = new Thread(() ->
		{
			playEpisode(episode, withSubtitles);
		});

		controller.startLoading(thread, "Launching media");
		thread.start();
	}

	private static void playEpisode(final Episode episode, boolean withSubtitles)
	{
		init();

		currentEpisode = episode;
		final THashMap<String, String> properties = episode.getProperties();
		String path = getPath(properties);

		if (withSubtitles)
		{
			controller.logLoadingScreen("Downloading subtitles");
			getSubtitles(path, true);
		}

//		If the request is null, we must launch VLC and wait for it to be prepared
		if (sendRequest("command=pl_empty") == null)
		{
			controller.logLoadingScreen("Starting VLC");
			runVLC(path);
		}
		else
			sendRequest("command=in_play&input=" + encodePath(path));

		String sendRequest = null;
		controller.logLoadingScreen("Waiting for VLC");
		while (currentId == -1 && !Thread.currentThread().isInterrupted())
		{
			sendRequest = sendRequest("command=seek&val=" + properties.get("time"));
			final JSONObject jsonObject = getJSONObject(sendRequest);
			if (jsonObject != null)
			{
				final Object get = jsonObject.get("currentplid");
				if (get != null)
				{
					currentId = (long) get;
					firstId = currentId;
				}
			}
		}

		if (Thread.currentThread().isInterrupted())
			return;

		properties.put("seen", "true");

		String filename = (String) getNestedObject(sendRequest, "information", "category", "meta", "filename");
		System.out.println(filename);

		timer = new Timer();
		timerTask = new TimerTaskImpl(properties, filename);
		timer.scheduleAtFixedRate(timerTask, 10000, 10000);

		controller.stopLoading();
	}

	private static String encodePath(String path)
	{
		// encode path
		path = path.replace(" ", "%20")
				.replace("&", "%26")
				.replace("#", "%23")
				.replace("=", "%3D");
		return path;
	}

	private static void getSubtitles(String path, boolean absoluteMode)
	{
		String language = Settings.getInstance().getGeneral().get("language");
		if (language == null)
			language = "en";
		else
			language = language.substring(0, 2).replace("G", "d").toLowerCase();
		try
		{
			File file = new File(path);
			String program = "subliminal";
			final String name = '"' + file.getAbsolutePath() + '"';
			String[] cmd;
			if (absoluteMode)
				cmd = new String[]
				{
					program, "download", "-l", language, "-f", "--", name
				};
			else
				cmd = new String[]
				{
					program, "download", "-l", language, "-f", "-d", file.getParent(), "--", file.getName()
				};

			Logger.getLogger(VLCController.class.getName()).log(Level.INFO, program + " -l " + language + " -f -- " + name);
			ProcessBuilder builder = new ProcessBuilder(cmd);
			builder.redirectErrorStream(true);
			Process process = builder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null)
			{
				line = line.replace("INFO: ", "");
				controller.logLoadingScreen(line);
				Logger.getLogger(VLCController.class.getName()).log(Level.INFO, line);
				if (absoluteMode)
					if (line.equals("No subtitles downloaded"))
						getSubtitles(path, false);
			}
			Utils.callFuncJS(CWebEngine.getWebEngine(), "updateIndicators");
		} catch (IOException ex)
		{
			Logger.getLogger(VLCController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static boolean cancelTimer(boolean checkStatus)
	{
		if (currentEpisode == null)
			return false;

		return cancelTimer(checkStatus, currentEpisode.getProperties());
	}

	public static boolean cancelTimer(boolean checkStatus, THashMap<String, String> properties)
	{
		if (timer != null && timerTask != null)
		{
			System.err.println("Cancelling timer.");
			timerTask.cancel();
			timer.cancel();
			timer = null;
			timerTask = null;

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

	private static JSONObject setEpisodeTime(final String status, final THashMap<String, String> properties)
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

	private static void runVLC(String uri)
	{
		File file = new File(uri);

		Logger.getLogger(VLCController.class.getName()).log(Level.INFO, "Launching VLC and adding file to playlist.");
		try
		{
			Desktop.getDesktop().open(file);
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

		private THashMap<String, String> properties;
		private String filename;

		public TimerTaskImpl(THashMap<String, String> properties, String filename)
		{
			this.properties = properties;
			this.filename = filename;
		}

		@Override
//		On each timer, we'll check for different things
		public void run()
		{
			Logger.getLogger(VLCController.class.getName()).log(Level.INFO, "Timer");

			final String status = sendRequest(null);
//			In case VLC has been closed, we cancel the timer
			if (status == null)
			{
				Logger.getLogger(VLCController.class.getName()).log(Level.INFO, "VLC was closed");
				cancelTimer(false, properties);
				return;
			}
//			else, we get the status json
			JSONObject obj = getJSONObject(status);
			String name = "";
//			from it, we get the name to compare it against the initial one
			if (obj != null)
				name = (String) getNestedObject(obj, "information", "category", "meta", "filename");
//			if they differ
			if (!filename.equals(name))
			{
//				we check if we're in a playlist
				if (obj != null && followingEpisodes != null && followingEpisodes.length > 1)
				{
//					we get the currentID
					Long id = (Long) obj.get("currentplid");
					if (id != null)
					{
						currentId = id;
						final int index = (int) (currentId - firstId);
//						if it is a valid index (VLC ids do not start at 0 but are contiguous
//						so we have to compare it against the first ID of the playlist
						if (followingEpisodes.length > index)
						{
//							we update the timer's data
							final Episode tmpEpisode = followingEpisodes[index];
							final THashMap<String, String> newProperties = tmpEpisode.getProperties();
							this.properties = newProperties;
							this.filename = name;
							this.properties.put("time", Long.toString((long) obj.get("time")));
							currentEpisode = tmpEpisode;
							this.properties.put("seen", "true");
							Utils.callFuncJS(CWebEngine.getWebEngine(), "seenNextEpisode", this.properties.get("name"), String.valueOf(index - 1));
							return;
						}
					}
				}
//				if there was a problem, we cancel
				cancelTimer(false, properties);
			}
//			if there was no probem at all, we just save the time
			properties.put("time", Long.toString((long) obj.get("time")));
		}
	}
}

package utils;

import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;

/**
 *
 * @author Romsahel
 */
public class Utils
{

	public static final String[] DUMP_KEYWORDS =
	{
		"[", "1080p", "720p", "x264", "HDTV", "FASTSUB", "VOSTFR", "MULTI",
		"FINAL", "REPACK", "FRENCH", "COMPLETE", "PROPER", "EXTENDED", "UNRATED", "BrRip", "720."
	};
	public static final String[] EXTENSIONS =
	{
		".avi", ".mkv", ".mp4", ".m4v"
	};

	public static final boolean isWindows = System.getProperty("os.name").contains("Windows");
	private static final String fileSeparator = (isWindows) ? "\\" : "/";
	private static final String sub = (isWindows) ? "/Documents/" : "";
	public static final String APPDATA = System.getProperty("user.home") + sub + "/.cinema/";

	public static void callFuncJS(WebEngine webEngine, String function, String... args)
	{
		Platform.runLater(() ->
		{
			String js = function + "(";
			for (int i = 0; i < args.length; i++)
			{
				final String str = args[i];
				if (str == null || str.length() == 0)
					js += "null";
				else if (str.charAt(0) == '\\')
					js += str.substring(1);
				else
					js += "\"" + str + "\"";

				if (i != args.length - 1)
					js += ", ";
			}

			js += ");";
			Logger.getLogger("").info(js);
			webEngine.executeScript(js);
		});
	}

	public static String callJS(WebEngine webEngine, String code)
	{
		final Object executeScript = webEngine.executeScript(code);
		return (String) executeScript;
	}

	/**
	 * @return the fileSeparator
	 */
	public static String getSeparator()
	{
		return fileSeparator;
	}
}

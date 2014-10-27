/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
	"FINAL", "REPACK", "FRENCH", "COMPLETE", "PROPER", "EXTENDED", "UNRATED"
  };
  public static final String[] EXTENSIONS =
  {
	".avi", ".mkv", ".mp4"
  };

  public static String getPrefix(String src, String... delimiter)
  {
	int i = findIndex(src, delimiter);

	if (i == -1)
	  return src;
	else
	  return src.substring(0, i);
  }

  public static int findIndex(String src, String... delimiter)
  {
	int i = -1;
	for (String s : delimiter)
	{
	  int lastIndexOf = src.toUpperCase().lastIndexOf(s.toUpperCase());
	  if ((lastIndexOf > 0) && (lastIndexOf < (src.length() - 1)))
		i = (i == -1) ? lastIndexOf : Math.min(i, lastIndexOf);
	}
	return i;
  }

  /**
   * Formats the name for in more legible version:
   * <ul>
   * <li>Capitalizes the first letter</li>
   * <li>Capitalizes the 's' and the 'e' of 's01e01'</li>
   * <li>Replaces '_' by spaces</li>
   * <li>Removes the date that might be between parenthese</li>
   * </ul>
   * <p>
   * @param toFormat the string to format
   * <p>
   * @return
   */
  public static String formatName(String toFormat)
  {
	toFormat = toFormat.substring(0, 1).toUpperCase() + toFormat.substring(1);
	toFormat = toFormat.replaceAll("[s]([0-9])", "S$1");
	toFormat = toFormat.replaceAll("[e]([0-9])", "E$1");
	toFormat = toFormat.replaceAll("_", " ");
	toFormat = toFormat.replaceAll("[(]?(19|20)[0-9]{2}[)]?", "");

	return toFormat.trim();
  }

  public static String removeSeason(String toFormat)
  {
	return removeSeason(toFormat, toFormat);
  }

  public static String removeSeason(String toFormat, String folder)
  {
	int index = folder.indexOf("Season");
	if (index < 0)
	  index = folder.indexOf("S0");
	if (index > 0)
	  return toFormat.replace(folder.substring(index), "");
	return toFormat;
  }

  public static String getYear(String name)
  {
	Pattern pattern = Pattern.compile("(19|20)[0-9]{2}");
	Matcher matcher = pattern.matcher(name);
	if (matcher.find())
	  return matcher.group();
	return null;
  }

  public static Object callJS(WebEngine webEngine, String function, String... args)
  {
	String js = function + "('";
	for (int i = 0; i < args.length; i++)
	  if (i != args.length - 1)
		js += args[i] + "', '";
	  else
		js += args[i];

	js += "')";
	System.out.println(js);
	return webEngine.executeScript(js);
  }
}

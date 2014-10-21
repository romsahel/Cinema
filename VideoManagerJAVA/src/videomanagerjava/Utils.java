/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

/**
 *
 * @author Romsahel
 */
public class Utils
{

  public static final String[] DUMP_KEYWORDS =
  {
    "[", "1080p", "720p", "x264", "HDTV", "FASTSUB", "VOSTFR", "MULTI",
    "FINAL", "REPACK", "FRENCH", "COMPLETE", "PROPER"
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

  public static String formatName(String toFormat, String folder)
  {
    toFormat = toFormat.substring(0, 1).toUpperCase() + toFormat.substring(1);
    toFormat = toFormat.replaceAll("[s]([0-9])", "S$1");
    toFormat = toFormat.replaceAll("[e]([0-9])", "E$1");
    toFormat = toFormat.replaceAll("_", " ");

    int index = folder.indexOf("Season");
    if (index < 0)
      index = folder.indexOf("S0");
    if (index > 0)
    {
      String toRemove = folder.substring(0, index);
      toFormat = toFormat.replace(toRemove, "");
    }
    return toFormat.trim();
  }
}

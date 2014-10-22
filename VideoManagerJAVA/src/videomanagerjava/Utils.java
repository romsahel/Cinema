/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
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

  public static String formatName(String toFormat)
  {
    return formatName(toFormat, null);
  }

  public static String formatName(String toFormat, String folder)
  {
    toFormat = toFormat.substring(0, 1).toUpperCase() + toFormat.substring(1);
    toFormat = toFormat.replaceAll("[s]([0-9])", "S$1");
    toFormat = toFormat.replaceAll("[e]([0-9])", "E$1");
    toFormat = toFormat.replaceAll("_", " ");

    if (folder != null)
      toFormat = toFormat.replace(removeSeason(folder), "");

    return toFormat.trim();
  }

  public static String removeSeason(String folder)
  {
    int index = folder.indexOf("Season");
    if (index < 0)
      index = folder.indexOf("S0");
    if (index > 0)
      return folder.substring(0, index);
    return folder;
  }

  public static String download(String urlString)
  {
    StringBuilder builder = new StringBuilder();
    BufferedReader in = null;
    try
    {
      URL url = new URL(urlString);
      in = new BufferedReader(new InputStreamReader(url.openStream()));
      String line;
      while ((line = in.readLine()) != null)
        builder.append(line);
      in.close();
    } catch (IOException ex)
    {
      Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
    } finally
    {
      try
      {
        if (in != null)
          in.close();
      } catch (IOException ex)
      {
        Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return builder.toString();
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

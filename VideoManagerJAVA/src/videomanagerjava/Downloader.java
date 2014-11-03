/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Romsahel
 */
public class Downloader
{

  public static final String POSTER_PATH = "public_html/media/posters/";

  public static String downloadString(String urlString)
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

  public static String downloadImage(String urlString)
  {
    final String hash = String.valueOf(urlString.hashCode());
    final String filename = POSTER_PATH + hash;
    File f = new File(filename);
    if (f.exists())
      return hash;

    try
    {
      URL url = new URL(urlString);
      HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
      httpcon.addRequestProperty("User-Agent", "Mozilla/4.76");

      InputStream in = httpcon.getInputStream();

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int n = 0;
      while (-1 != (n = in.read(buf)))
        out.write(buf, 0, n);
      out.close();
      byte[] response = out.toByteArray();
      FileOutputStream fos = new FileOutputStream(filename);
      fos.write(response);
	  return hash;
    } catch (IOException ex)
    {
      Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
    }

    return hash;
  }
}

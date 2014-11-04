/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Romsahel
 */
public class Media
{

  private final ArrayList<Media> medias;
  private final ArrayList<String> files;
  private final long id;
  private final HashMap<String, String> info;

  public Media(long id)
  {
	this(null, id, null);
  }

  public Media(String name, long id)
  {
	this(name, id, null);
  }

  public Media(String name, long id, String img)
  {
	info = new HashMap<>();
	this.medias = new ArrayList<>();
	this.files = new ArrayList<>();

	this.id = id;
	if (name != null)
	{
	  info.put("year", Utils.getYear(name));
	  info.put("name", Utils.formatName(name));
	}
	info.put("img", img);
  }

  public void downloadInfos()
  {
	final String formattedName = Utils.removeSeason(info.get("name"));
	final int limit = getInfo().get("year") == null ? 1 : 3;
	String url = "http://api.trakt.tv/search/movies.json/5921de65414d60b220c6296761061a3b?query="
				 + formattedName.replace(" ", "+")
				 + "&limit=" + limit;
	if (!formattedName.equals(info.get("name")))
	  url = url.replace("movies.json", "shows.json");

	System.out.println(url);

	JSONParser parser = new JSONParser();
	try
	{
	  final String json = Downloader.downloadString(url);
	  JSONArray array = (JSONArray) parser.parse(json);
	  for (Object obj : array)
	  {
		JSONObject jobj = (JSONObject) obj;
		final String newYear = jobj.get("year").toString();
		if (getInfo().get("year") == null || newYear.equals(getInfo().get("year")))
		{
		  getInfo().put("year", newYear);
		  getInfo().put("overview", jobj.get("overview").toString());
		  getInfo().put("genres", jobj.get("genres").toString().replace("[", "").replace("\"", "").replace("]", "").replace(",", " "));
		  getInfo().put("imdb", jobj.get("imdb_id").toString());
		  getInfo().put("duration", jobj.get("runtime").toString());

		  String imgURL = (String) ((JSONObject) jobj.get("images")).get("poster");
		  imgURL = imgURL.replace(".jpg", "-300.jpg");

		  getInfo().put("img", Downloader.downloadImage(imgURL));

		  break;
		}
	  }
	} catch (ParseException ex)
	{
	  System.out.println(url);
	  Logger
			  .getLogger(VideoManagerJAVA.class
					  .getName()).log(Level.SEVERE, null, ex);
	}
  }

  /**
   * @return the medias
   */
  public ArrayList<Media> getMedias()
  {
	return medias;
  }

  /**
   * @return the files
   */
  public ArrayList<String> getFiles()
  {
	return files;
  }

  /**
   * @return the id
   */
  public long getId()
  {
	return id;
  }

  /**
   * @return the info
   */
  public HashMap<String, String> getInfo()
  {
	return info;
  }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.util.ArrayList;
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

  private final String name;
  private String img;
  private String year;
  private final ArrayList<Media> medias;
  private final ArrayList<String> files;
  private final long id;

  public Media(String name, long id)
  {
	this(name, id, null);
  }

  public Media(String name, long id, String img)
  {
	this.year = Utils.getYear(name);
	this.name = Utils.formatName(name);
	this.id = id;
	this.img = img;
	this.medias = new ArrayList<>();
	this.files = new ArrayList<>();
  }

  public void downloadImg()
  {
	final String formattedName = Utils.removeSeason(name);
	final int limit = year == null ? 1 : 3;
	String url = "http://api.trakt.tv/search/movies.json/5921de65414d60b220c6296761061a3b?query="
				 + formattedName.replace(" ", "+")
				 + "&limit=" + limit;
	if (!formattedName.equals(name))
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
		if (year == null || jobj.get("year").toString().equals(year))
		{
		  String imgURL = (String) ((JSONObject) jobj.get("images")).get("poster");
		  imgURL = imgURL.replace(".jpg", "-300.jpg");
		  setImg(Downloader.downloadImage(imgURL));
		  break;
		}
	  }
	} catch (ParseException ex)
	{
	  System.out.println(url);
	  Logger.getLogger(VideoManagerJAVA.class.getName()).log(Level.SEVERE, null, ex);
	}
  }

  /**
   * @return the name
   */
  public String getName()
  {
	return name;
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
   * @return the img
   */
  public String getImg()
  {
	return img;
  }

  /**
   * @param img the img to set
   */
  public void setImg(String img)
  {
	this.img = img;
  }

  /**
   * @return the id
   */
  public long getId()
  {
	return id;
  }

}

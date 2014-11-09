/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
public class Database
{

  private final HashMap<Long, Media> database;

  private Database()
  {
	database = new HashMap<>();
  }

  public void writeDatabase()
  {
	JSONArray db = new JSONArray();

	for (Map.Entry<Long, Media> media : getDatabase().entrySet())
	  db.add(writeMedia(media.getValue()));

	try (FileWriter file = new FileWriter("db.json"))
	{
	  file.write(db.toJSONString());
	  file.flush();
	} catch (IOException ex)
	{
	  Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
	}
  }

  private JSONObject writeMedia(Media media)
  {
	JSONObject elt = new JSONObject();
	elt.put("id", media.getId());
	final JSONObject info = new JSONObject();
	for (Map.Entry<String, String> entrySet : media.getInfo().entrySet())
	  info.put(entrySet.getKey(), entrySet.getValue());
	elt.put("info", info);

	final HashMap<String, String> files = media.getFiles();
	if (files.size() > 0)
	{
	  final JSONObject obj = new JSONObject();
	  for (Map.Entry<String, String> entrySet : files.entrySet())
		obj.put(entrySet.getKey(), entrySet.getValue());
	  elt.put("files", obj);
	}

	final ArrayList<Media> medias = media.getMedias();
	if (medias.size() > 0)
	{
	  final JSONArray obj = new JSONArray();
	  for (Media m : medias)
		obj.add(writeMedia(m));
	  elt.put("medias", obj);
	}

	return elt;
  }

  public void readDatabase()
  {
	try
	{
	  JSONParser parser = new JSONParser();

	  Object obj = parser.parse(new FileReader("db.json"));

	  JSONArray db = (JSONArray) obj;

	  for (Iterator iterator = db.iterator(); iterator.hasNext();)
	  {
		JSONObject elt = (JSONObject) iterator.next();
		final Media media = readMedia(elt);
		database.put(media.getId(), media);
	  }
	} catch (IOException | ParseException ex)
	{
	  Logger.getLogger(Database.class.getName()).log(Level.INFO, null, ex);
	}

	for (Map.Entry<Long, Media> next : getDatabase().entrySet())
	  System.out.println(next.getKey() + ": " + next.getValue());
  }

  private Media readMedia(JSONObject elt)
  {
	final long id = (long) elt.get("id");
	final Media media = new Media(id);

	JSONObject obj = (JSONObject) elt.get("info");
	for (Iterator it = obj.keySet().iterator(); it.hasNext();)
	{
	  String key = (String) it.next();
	  media.getInfo().put(key, (String) obj.get(key));
	}

	JSONArray array = (JSONArray) elt.get("medias");
	if (array != null)
	  for (Object o : array)
		media.getMedias().add(readMedia((JSONObject) o));

	obj = (JSONObject) elt.get("files");
	if (obj != null)
	  for (Iterator it = obj.keySet().iterator(); it.hasNext();)
	  {
		String key = (String) it.next();
		media.getFiles().put(key, (String) obj.get(key));
	  }

	return media;
  }

  // <editor-fold defaultstate="collapsed" desc="Singleton">
  public static Database getInstance()
  {
	return DatabaseHolder.INSTANCE;
  }

  /**
   * @return the medias
   */
  public HashMap<Long, Media> getDatabase()
  {
	return database;
  }

  private static class DatabaseHolder
  {

	private static final Database INSTANCE = new Database();
  }
  // </editor-fold>
}

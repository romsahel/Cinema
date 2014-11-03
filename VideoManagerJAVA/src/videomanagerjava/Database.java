/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
	for (Map.Entry<String, String> entrySet : media.getInfo().entrySet())
	  elt.put(entrySet.getKey(), entrySet.getValue());

	final JSONArray files = new JSONArray();
	files.addAll(media.getFiles());
	elt.put("files", files);

	JSONArray medias = new JSONArray();
	for (Media m : media.getMedias())
	  medias.add(writeMedia(m));
	elt.put("medias", medias);

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
		final long id = (long) elt.get("id");
		database.put(id, new Media((String) elt.get("name"), id, (String) elt.get("img")));
	  }
	} catch (IOException | ParseException ex)
	{
	  Logger.getLogger(Database.class.getName()).log(Level.INFO, null, ex);
	}

	for (Map.Entry<Long, Media> next : getDatabase().entrySet())
	  System.out.println(next.getKey() + ": " + next.getValue());
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava.files;

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
import videomanagerjava.Media;

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

		writeMap("info", media.getInfo(), elt);
		writeMap("files", media.getFiles(), elt);
		writeMap("seasons", media.getSeasons(), elt);

		return elt;
	}

	private void writeMap(String name, Map map, JSONObject elt)
	{
		if (map.size() > 0)
			elt.put(name, new JSONObject(map));
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
			System.err.println("Could not read database");
//			Logger.getLogger(Database.class.getName()).log(Level.INFO, null, ex);
		}

		for (Map.Entry<Long, Media> next : getDatabase().entrySet())
			System.out.println(next.getKey() + ": " + next.getValue());
	}

	private Media readMedia(JSONObject elt)
	{
		final long id = (long) elt.get("id");
		final Media media = new Media(id);

		readMap("info", elt, media.getInfo());
		readMap("seasons", elt, media.getSeasons());
		readMap("files", elt, media.getFiles());

		return media;
	}

	private void readMap(String name, JSONObject elt, final Map map)
	{
		System.out.println(name);
		JSONObject obj = (JSONObject) elt.get(name);
		if (obj != null)
			for (Iterator it = obj.keySet().iterator(); it.hasNext();)
			{
				String key = (String) it.next();
				map.put(key, obj.get(key));
			}
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

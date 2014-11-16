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
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import videomanagerjava.Episode;
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
			Logger.getLogger(Database.class.getName()).log(Level.INFO, null, ex);
		}

		for (Map.Entry<Long, Media> next : getDatabase().entrySet())
			System.out.println(next.getKey() + ": " + next.getValue());
	}

	private Media readMedia(JSONObject elt)
	{
		final long id = (long) elt.get("id");
		final Media media = new Media(id);

		readMap((JSONObject) elt.get("info"), media.getInfo());
		readMap((JSONObject) elt.get("seasons"), media.getSeasons());

		return media;
	}

	private void readMap(JSONObject obj, final Map map)
	{
		if (obj != null)
			for (Iterator it = obj.keySet().iterator(); it.hasNext();)
			{
				final String key = (String) it.next();
				final Object value = obj.get(key);
				if (value.getClass() == String.class)
					map.put(key, value);
				else
				{
					TreeMap<String, Episode> newMap = new TreeMap<>();
					readEpisodes((JSONObject) value, newMap);
					map.put(key, newMap);
				}
			}
	}

	private void readEpisodes(JSONObject obj, final Map map)
	{
		if (obj != null)
			for (Iterator it = obj.keySet().iterator(); it.hasNext();)
			{
				final String key = (String) it.next();
				final JSONObject value = (JSONObject) obj.get(key);
				Episode episode = new Episode((String) value.get("name"),
											  (String) value.get("path"),
											  (Boolean) value.get("seen"),
											  (Long) value.get("time"));

				map.put((String) value.get("name"), episode);

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

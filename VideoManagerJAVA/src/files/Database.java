/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package files;

import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.Formatter;
import utils.Utils;
import videomanagerjava.Episode;
import videomanagerjava.Media;

/**
 *
 * @author Romsahel
 */
public class Database
{

	private final THashMap<Long, Media> database;
	private final THashMap<Long, ArrayList<String>> deletedMedias;
	private final THashMap<Long, ArrayList<String>> mergedMedias;

	final String filename = Utils.APPDATA + "db.json";

	private Database()
	{
		database = new THashMap<>();
		deletedMedias = new THashMap<>();
		mergedMedias = new THashMap<>();
	}

	@SuppressWarnings("unchecked")
	public void writeDatabase()
	{
		JSONObject db = new JSONObject();
		ArrayList<JSONObject> medias = new ArrayList<>();

		for (Map.Entry<Long, Media> media : getDatabase().entrySet())
			medias.add(writeMedia(media.getKey(), media.getValue()));

		db.put("medias", medias);

		db.put("merged", mergedMedias);

		db.put("deleted", deletedMedias);

		try (FileWriter file = new FileWriter(filename))
		{
			file.write(JSONObject.toJSONString(db));
			file.flush();
		} catch (IOException ex)
		{
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void backupDatabase()
	{
		try
		{
			Path file = Paths.get(filename).toRealPath(); //toRealPath() follows symlinks to their ends
			File backFile = new File(filename + ".backup");
			if (!backFile.exists())
				backFile.createNewFile(); // ensure the backup file exists so we can write to it later
			Path back = Paths.get(filename + ".backup").toRealPath();
			Files.copy(file, back, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex)
		{
			Logger.getLogger(Formatter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private JSONObject writeMedia(Long id, Media media)
	{
		THashMap<String, Object> elt = new THashMap<>();

		elt.put("id", id);

		if (media != null)
		{
			writeMap("info", media.getInfo(), elt);
			writeMap("seasons", media.getSeasons(), elt);
		}

		return new JSONObject(elt);
	}

	private void writeMap(String name, Map<String, ?> map, THashMap<String, Object> elt)
	{
		if (map.size() > 0)
			elt.put(name, new JSONObject(map));
	}

	public void readDatabase()
	{
		try
		{
			File file = new File(filename);
			if (!file.exists())
			{
				file.createNewFile();
				return;
			}

			JSONParser parser = new JSONParser();

			JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(file));

			JSONArray db = (JSONArray) jsonObject.get("medias");

			for (Iterator iterator = db.iterator(); iterator.hasNext();)
			{
				JSONObject elt = (JSONObject) iterator.next();
				final long id = (long) elt.get("id");
				final Media media = readMedia(id, elt);
				database.put(id, media);
			}

			readDeletedMap((JSONObject) jsonObject.get("deleted"), deletedMedias);
			readDeletedMap((JSONObject) jsonObject.get("merged"), mergedMedias);

		} catch (IOException | ParseException ex)
		{
			System.err.println("Could not read database");
			Logger.getLogger(Database.class.getName()).log(Level.INFO, null, ex);
		}
	}

	@SuppressWarnings("unchecked")
	private void readDeletedMap(JSONObject obj, final THashMap<Long, ArrayList<String>> map)
	{
		if (obj != null)
			for (Iterator it = obj.keySet().iterator(); it.hasNext();)
			{
				final String key = (String) it.next();
				final Object get = obj.get(key);

				if (get != null)
				{
					final ArrayList<String> value = (ArrayList<String>) get;
					map.put(Long.valueOf(key), value);
				}
			}
	}

	private Media readMedia(long id, JSONObject elt)
	{
		final Media media = new Media(id);
		readMapInfo((JSONObject) elt.get("info"), media.getInfo());
		if (!readMapSeasons((JSONObject) elt.get("seasons"), media.getSeasons()))
			return null;

		return media;
	}

	private boolean readMapInfo(JSONObject obj, final THashMap<String, String> map)
	{
		final boolean isNotNull = obj != null;
		if (isNotNull)
			for (Iterator it = obj.keySet().iterator(); it.hasNext();)
			{
				final String key = (String) it.next();
				final Object value = obj.get(key);

				map.put(key, (String) value);
			}
		return isNotNull;
	}

	private boolean readMapSeasons(JSONObject obj, final TreeMap<String, TreeMap<String, Episode>> map)
	{
		final boolean isNotNull = (obj != null);
		if (isNotNull)
			for (Iterator it = obj.keySet().iterator(); it.hasNext();)
			{
				final String key = (String) it.next();
				final Object value = obj.get(key);

				TreeMap<String, Episode> newMap = new TreeMap<>();
				readEpisodes((JSONObject) value, newMap);
				map.put(key, newMap);
			}
		return isNotNull;
	}

	private void readEpisodes(JSONObject obj, final TreeMap<String, Episode> map)
	{
		if (obj != null)
			for (Iterator it = obj.keySet().iterator(); it.hasNext();)
			{
				final String key = (String) it.next();
				final JSONObject value = (JSONObject) obj.get(key);
				Episode episode = new Episode((String) value.get("name"),
											  JSONObject.escape((String) value.get("path")),
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
	public THashMap<Long, Media> getDatabase()
	{
		return database;
	}

	/**
	 * @return the deletedMedias
	 */
	public THashMap<Long, ArrayList<String>> getDeletedMedias()
	{
		return deletedMedias;
	}

	/**
	 * @return the mergedMedias
	 */
	public THashMap<Long, ArrayList<String>> getMergedMedias()
	{
		return mergedMedias;
	}

	public ArrayList<String> removeMedia(boolean merged, Media toRemove)
	{
		final long id = toRemove.getId();
		database.remove(id);
		THashMap<Long, ArrayList<String>> map = (merged) ? mergedMedias : deletedMedias;
		ArrayList<String> data = new ArrayList<>();
		final THashMap<String, String> info = toRemove.getInfo();
		data.add(info.get("name"));
		data.add(info.get("location"));
		data.add(info.get("path"));
		data.add(Long.toString(id));
		return map.put(id, data);
	}

	private static class DatabaseHolder
	{

		private static final Database INSTANCE = new Database();
	}
  // </editor-fold>
}

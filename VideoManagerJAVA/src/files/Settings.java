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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.Utils;
import videomanagerjava.Location;

/**
 *
 * @author Romsahel
 */
public class Settings
{

	private static final String filename = Utils.APPDATA + "config.json";
	private final THashMap<String, Location> locations;
	private final THashMap<String, String> general;

	private Settings()
	{
		locations = new THashMap<>();
		general = new THashMap<>();
	}

	public void writeSettings()
	{
		HashMap<String, JSONObject> obj = new HashMap<>();

		writeMap("locations", locations, obj);
		writeMap("general", general, obj);

		try (FileWriter file = new FileWriter(filename))
		{
			file.write(JSONObject.toJSONString(obj));
			file.flush();
		} catch (IOException ex)
		{
			Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void writeMap(String name, final THashMap<String, ?> map, HashMap<String, JSONObject> elt)
	{
		if (map.size() > 0)
			elt.put(name, new JSONObject(map));
	}

	public void readSettings()
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

			Object obj = parser.parse(new FileReader(file));

			JSONObject jsonObject = (JSONObject) obj;

			readLocationsMap((JSONObject) jsonObject.get("locations"), locations);
			readMap((JSONObject) jsonObject.get("general"), general);

		} catch (IOException | ParseException ex)
		{
			Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
		}

		for (Map.Entry<String, Location> next : getLocations().entrySet())
			System.out.println(next.getKey() + ": " + next.getValue());
	}

	private void readMap(JSONObject obj, final THashMap<String, String> map)
	{
		if (obj != null)
			for (Iterator it = obj.keySet().iterator(); it.hasNext();)
			{
				final String key = (String) it.next();
				final Object get = obj.get(key);
				if (get != null)
				{
					final String value = get.toString();
					map.put(key, value);
				}
			}
	}

	private void readLocationsMap(JSONObject obj, final THashMap<String, Location> map)
	{
		if (obj != null)
			for (Iterator it = obj.keySet().iterator(); it.hasNext();)
			{
				final String key = (String) it.next();
				final Object get = obj.get(key);
				if (get != null)
				{
					final JSONObject value = (JSONObject) get;
					map.put(key, new Location((String) value.get("path"), (Boolean) value.get("special")));
				}
			}
	}

	// <editor-fold defaultstate="collapsed" desc="Singleton">
	public static Settings getInstance()
	{
		return SettingsHolder.INSTANCE;
	}

	/**
	 * @return the locations
	 */
	public THashMap<String, Location> getLocations()
	{
		return locations;
	}

	public THashMap<String, String> getGeneral()
	{
		return general;
	}

	private static class SettingsHolder
	{

		private static final Settings INSTANCE = new Settings();
	}
  // </editor-fold>
}

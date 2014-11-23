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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Romsahel
 */
public class Settings
{

	private final HashMap<String, String> locations;
	private final HashMap<String, String> general;

	private Settings()
	{
		locations = new HashMap<>();
		general = new HashMap<>();
	}

	public void writeSettings()
	{
		HashMap<String, JSONObject> obj = new HashMap<>();

		writeMap("locations", locations, obj);
		writeMap("general", general, obj);

		try (FileWriter file = new FileWriter("config.json"))
		{
			file.write(JSONObject.toJSONString(obj));
			file.flush();
		} catch (IOException ex)
		{
			Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void writeMap(String name, final HashMap<String, String> map, HashMap<String, JSONObject> elt)
	{
		if (map.size() > 0)
			elt.put(name, new JSONObject(map));
	}

	public void readSettings()
	{
		try
		{
			JSONParser parser = new JSONParser();

			Object obj = parser.parse(new FileReader("config.json"));

			JSONObject jsonObject = (JSONObject) obj;

			readMap((JSONObject) jsonObject.get("locations"), locations);
			readMap((JSONObject) jsonObject.get("general"), general);

		} catch (IOException | ParseException ex)
		{
			Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
		}

		for (Map.Entry<String, String> next : getLocations().entrySet())
			System.out.println(next.getKey() + ": " + next.getValue());
	}

	private void readMap(JSONObject obj, final HashMap<String, String> map)
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

	// <editor-fold defaultstate="collapsed" desc="Singleton">
	public static Settings getInstance()
	{
		return SettingsHolder.INSTANCE;
	}

	/**
	 * @return the locations
	 */
	public HashMap<String, String> getLocations()
	{
		return locations;
	}

	public HashMap<String, String> getGeneral()
	{
		return general;
	}

	private static class SettingsHolder
	{

		private static final Settings INSTANCE = new Settings();
	}
  // </editor-fold>
}

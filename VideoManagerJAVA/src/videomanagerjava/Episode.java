/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.util.HashMap;
import java.util.Map;
import utils.Utils;

/**
 *
 * @author Romsahel
 */
public class Episode
{

	private final HashMap<String, String> properties;

	/**
	 *
	 * @param name
	 * @param path
	 * @param seen
	 * @param time
	 */
	public Episode(String name, String path, boolean seen, long time)
	{
		properties = new HashMap<>();
		properties.put("name", name);
		properties.put("path", path);
		properties.put("seen", Boolean.toString(seen));
		properties.put("time", Long.toString(time));
	}

	public Episode(String path)
	{
		properties = new HashMap<>();
		properties.put("name", Utils.getSuffix(path, Utils.getSeparator()));
		properties.put("path", path);
		properties.put("seen", "false");
		properties.put("time", "0");
	}

	@Override
	public String toString()
	{

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entrySet : properties.entrySet())
		{
			String key = entrySet.getKey();
			String value = entrySet.getValue();
			String quote = "\"";
			if (key.equals("seen") || key.equals("time"))
				quote = "";
			sb.append(String.format(", \"%s\":%s%s%s", key, quote, value, quote));
		}
		return "{" + sb.toString().substring(2) + "}";
	}

	public HashMap<String, String> getProperties()
	{
		return properties;
	}

	void toggleSeen()
	{
		if (properties.get("seen").equals("true"))
			properties.put("seen", "false");
		else
			properties.put("seen", "true");
	}
}

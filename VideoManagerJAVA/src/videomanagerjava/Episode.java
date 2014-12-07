/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.io.File;
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
		setProperty("name", name);
		setProperty("path", path);
		setProperty("seen", Boolean.toString(seen));
		setProperty("time", Long.toString(time));
	}

	public Episode(File file)
	{
		properties = new HashMap<>();
		setProperty("name", Utils.getSuffix(file.getAbsolutePath(), Utils.getSeparator()));
		setProperty("path", file.toURI().toString().replace('/', '\\').replace("file:\\", ""));
		setProperty("seen", "false");
		setProperty("time", "0");
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

	public void setProperty(String key, String value)
	{
		properties.put(key.intern(), value.intern());
	}

	public HashMap<String, String> getProperties()
	{
		return properties;
	}

	void toggleSeen()
	{
		if (properties.get("seen").equals("true"))
			setProperty("seen", "false");
		else
			setProperty("seen", "true");
	}
}

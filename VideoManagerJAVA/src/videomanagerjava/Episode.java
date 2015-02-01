/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.util.Map;
import utils.Utils;

/**
 *
 * @author Romsahel
 */
public class Episode
{

	private final THashMap<String, String> properties;

	/**
	 *
	 * @param name
	 * @param path
	 * @param seen
	 * @param time
	 */
	public Episode(String name, String path, boolean seen, long time)
	{
		properties = new THashMap<>();
		setProperty("name", name);
		setProperty("path", path);
		setProperty("seen", Boolean.toString(seen));
		setProperty("time", Long.toString(time));
	}

	public Episode(File file)
	{
		properties = new THashMap<>();
		setProperty("name", Utils.getSuffix(file.getAbsolutePath(), Utils.getSeparator()));
		setProperty("path", file.getAbsolutePath().replace("\\", "/"));
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

	public final void setProperty(String key, String value)
	{
		properties.put(key.intern(), value.intern());
	}

	public THashMap<String, String> getProperties()
	{
		return properties;
	}

	void setSeen(boolean seen)
	{
		setProperty("seen", Boolean.toString(seen));
	}
}

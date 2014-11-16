/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import utils.Utils;

/**
 *
 * @author Romsahel
 */
public class Episode
{

	private final String name;
	private final String path;
	private final boolean seen;
	private long time;

	/**
	 *
	 * @param name
	 * @param path
	 * @param seen
	 * @param time
	 */
	public Episode(String name, String path, boolean seen, long time)
	{
		this.name = name;
		this.path = path;
		this.seen = seen;
		this.time = time;
	}

	public Episode(String path)
	{
		this.name = Utils.getSuffix(path, Utils.getSeparator());
		this.path = path;
		this.seen = false;
		this.time = 0;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the path
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * @return the seen
	 */
	public boolean isSeen()
	{
		return seen;
	}

	@Override
	public String toString()
	{
		return "{"
			   + "\"name\": \"" + name + "\", "
			   + "\"path\": \"" + path.replace("\\", "\\\\") + "\", "
			   + "\"seen\": " + seen + ", "
			   + "\"time\": " + time
			   + '}';
	}

	/**
	 * @return the time
	 */
	public long getTime()
	{
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(long time)
	{
		this.time = time;
	}

}

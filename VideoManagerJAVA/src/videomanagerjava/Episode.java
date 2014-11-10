/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

/**
 *
 * @author Romsahel
 */
public class Episode
{

	private final String name;
	private final String path;
	private final boolean seen;

	/**
	 *
	 * @param name
	 * @param path
	 * @param seen
	 */
	public Episode(String name, String path, boolean seen)
	{
		this.name = name;
		this.path = path;
		this.seen = seen;
	}

	public Episode(String path)
	{
		this.name = Utils.getSuffix(path, Utils.getSeparator());
		this.path = path;
		this.seen = false;
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
		return "{" + "\"name\": \"" + name + "\", \"path\": \"" + path.replace("\\", "\\\\") + "\", \"seen\": " + seen + '}';
	}

}

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
public class Location
{

	private final String path;
	private final boolean special;

	public Location(String path, boolean special)
	{
		this.path = path;
		this.special = special;
	}

	@Override
	public String toString()
	{
		return String.format("{\"path\":\"%s\", \"special\": %s}", path, special);
	}

	/**
	 * @return the path
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * @return the special
	 */
	public boolean isSpecial()
	{
		return special;
	}

}

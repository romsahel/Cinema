package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Romsahel
 */
public class Formatter
{

	/**
	 * Formats the name for in more legible version:
	 * <ul>
	 * <li>Capitalizes the first letter</li>
	 * <li>Capitalizes the 's' and the 'e' of 's01e01'</li>
	 * <li>Replaces '_' by spaces</li>
	 * <li>Removes the date that might be between parenthese</li>
	 * </ul>
	 * <p>
	 * @param toFormat the string to format
	 * <p>
	 * @return
	 */
	public static String formatName(String toFormat)
	{
		toFormat = toFormat.substring(0, 1).toUpperCase() + toFormat.substring(1);
		toFormat = toFormat.replaceAll("[s]([0-9])", "S$1");
		toFormat = toFormat.replaceAll("[e]([0-9])", "E$1");
		toFormat = toFormat.replaceAll("_", " ");
		toFormat = toFormat.replaceAll("[(]?(19|20)[0-9]{2}[)]?", "");
		return toFormat.trim();
	}

	public static String getSeason(String folder, String toFormat)
	{
		int index = folder.indexOf("Season");
		if (index < 0)
			index = folder.indexOf("S0");
		if (index > 0)
			return folder.substring(index);
		return null;
	}

	public static String removeSeason(String toFormat)
	{
		return removeSeason(toFormat, toFormat);
	}

	public static String removeSeason(String toFormat, String folder)
	{
		final String season = getSeason(folder, toFormat);
		if (season != null)
			return toFormat.replace(season, "").trim();
		else
			return toFormat;
	}

	public static String getFormattedSeason(String folder, String toFormat)
	{
		String season = Formatter.getSeason(folder, toFormat);
		if (season != null)
		{
			season = season.replaceAll("[^\\d.]", "");
			season = String.format("Season %02d", Integer.parseInt(season));
			return season;
		}
		else
			return toFormat;
	}

	public static String getYear(String name)
	{
		Pattern pattern = Pattern.compile("(19|20)[0-9]{2}");
		Matcher matcher = pattern.matcher(name);
		if (matcher.find())
			return matcher.group();
		return null;
	}

	public static String getSuffix(String src, String... delimiter)
	{
		int i = Formatter.findIndex(src, delimiter);
		if (i == -1)
			return src;
		else
			return new String(src.substring(i + 1));
	}

	public static int findIndex(String src, String... delimiter)
	{
		int i = -1;
		for (String s : delimiter)
		{
			int lastIndexOf = src.toUpperCase().lastIndexOf(s.toUpperCase());
			if ((lastIndexOf > 0) && (lastIndexOf < (src.length() - 1)))
				i = (i == -1) ? lastIndexOf : Math.min(i, lastIndexOf);
		}
		return i;
	}

	public static String getPrefix(String src, String... delimiter)
	{
		int i = findIndex(src, delimiter);
		if (i == -1)
			return src;
		else
			return new String(src.substring(0, i));
	}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import files.Downloader;
import gnu.trove.map.hash.THashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.Formatter;
import utils.Utils;

/**
 *
 * @author Romsahel
 */
public class Media
{

	private final static String DEFAULT_KEY = "Files";
	private TreeMap<String, TreeMap<String, Episode>> seasons;
	private final long id;
	private final THashMap<String, String> info;

	public Media(long id)
	{
		this(null, id, null);
	}

	/*
	 * Constructor that should only be called by the FileWalker since it creates a "Default" season
	 */
	public Media(String path, long id)
	{
		this(path, id, null);
		seasons.put(DEFAULT_KEY, new TreeMap<>());
	}

	public Media(String path, long id, String img)
	{
		info = new THashMap<>();
		this.seasons = new TreeMap<>();

		this.id = id;
		if (path != null)
		{
			String name = Formatter.getCleanName(Formatter.getSuffix(path, Utils.getSeparator()));
			setInfo("year", Formatter.getYear(name));
			setInfo("name", Formatter.formatName(name));
			setInfo("path", path);
		}
		setInfo("img", img);
	}

	private String infoToString(JSONObject jobj, String key)
	{
		return infoToString(jobj, key, "Unknown");
	}

	private String infoToString(JSONObject jobj, String key, String defaultAnswer)
	{
		Object newInfo = jobj.get(key);
		newInfo = (newInfo == null) ? defaultAnswer : newInfo.toString();
		return (String) newInfo;
	}

	public String downloadInfos()
	{
		return downloadInfos(false);
	}

	public String downloadInfos(boolean ignoreYear)
	{
		setInfo("loading", "true");
		final String formattedName = Formatter.removeSeason(info.get("name"));
		final THashMap<String, String> infoList = getInfo();
		final String year = (infoList.get("year") == null
							 || ignoreYear) ? "" : "&y=" + infoList.get("year");

		String url = "http://www.omdbapi.com/?t="
					 + formattedName.replace(" ", "+")
					 + year + "&plot=full&r=json";

		String type = infoList.get("type");
		if (type == null)
		{
			if (!formattedName.equals(info.get("name")))
				url += "&type=series";
			else if (year.length() > 0)
				url += "&type=movie";
		}
		else if (type.equals("show"))
			url += "&type=series";
		else
			url += "&type=movie";

		return downloadInfos(url);
	}

	public String downloadInfos(String url)
	{
		Logger.getLogger(Media.class.getName()).log(Level.INFO, url);
		JSONParser parser = new JSONParser();
		final String json = Downloader.downloadString(url);
		JSONObject jobj;
		try
		{
			jobj = (JSONObject) parser.parse(json);
			if (jobj.get("Title") != null)
			{
				setInfo("year", infoToString(jobj, "Year", null));
				setInfo("overview", infoToString(jobj, "Plot", "There is no plot description."));
				setInfo("imdb", infoToString(jobj, "imdbID", ""));
				setInfo("duration", infoToString(jobj, "Runtime", "0 min"));
				setInfo("imdbRating", infoToString(jobj, "imdbRating", "0.0"));
				setInfo("type", infoToString(jobj, "Type").replace("series", "show"));
				String genres = '"' + infoToString(jobj, "Genre").replace(",", "\", \"") + '"';
				setInfo("genres", '[' + genres + ']');
				String downloadedImage = Downloader.downloadImage(infoToString(jobj, "Poster", null));
				if (downloadedImage != null)
					setInfo("img", downloadedImage);
				info.remove("loading");
				return downloadedImage;
			}
		} catch (ParseException ex)
		{
			System.err.println("====");
			System.err.println(url);
			Logger.getLogger(Media.class.getName()).log(Level.SEVERE, url, ex);
			setInfo("error", "true");
		}

		info.remove("loading");
		return null;
	}

	/**
	 * @return the seasons
	 */
	public TreeMap<String, TreeMap<String, Episode>> getSeasons()
	{
		return seasons;
	}

	/**
	 * @param seasons the seasons to set
	 */
	public void setSeasons(TreeMap<String, TreeMap<String, Episode>> seasons)
	{
		this.seasons = seasons;
	}

	public TreeMap<String, Episode> getDefaultSeason()
	{
		return seasons.get(DEFAULT_KEY);
	}

	public TreeMap<String, Episode> getFirstSeason()
	{
		for (Map.Entry<String, TreeMap<String, Episode>> entrySet : seasons.entrySet())
			return entrySet.getValue();
		return null;
	}

	public void removeEmptySeasons()
	{
		for (Iterator<Map.Entry<String, TreeMap<String, Episode>>> it = seasons.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry<String, TreeMap<String, Episode>> entry = it.next();
			if (entry.getValue() == null || entry.getValue().size() <= 0)
				it.remove();
		}
	}

	public String toJSArray()
	{
		StringBuilder sb = new StringBuilder();
		//	create an array with all the needed data: first general info
		sb.append(String.format("\"%s\": \"%s\",", "id", this.getId()));
		sb.append("\"info\": ").append(JSONValue.toJSONString(this.getInfo()));
		//	appends the season episodes
		sb.append(", \"seasons\": ").append(JSONValue.toJSONString(this.getSeasons()));
		String array = "{" + sb.toString() + "}";
		Pattern pattern = Pattern.compile("genres\":\"\\[(.*?)\\]\"");
		Matcher matcher = pattern.matcher(array);
		if (matcher.find())
		{
			final String genres = matcher.group(1).replace("\\\"", "\"").replace("\" ", "\"");
			array = array.replace(matcher.group(), "genres\": [" + genres + "]");
		}
		return "\\" + array;
	}

	/**
	 * @return the id
	 */
	public long getId()
	{
		return id;
	}

	public final void setInfo(String key, String value)
	{
		if (value != null)
			value = value.intern();
		info.put(key.intern(), value);
	}

	/**
	 * @return the info
	 */
	public THashMap<String, String> getInfo()
	{
		return info;
	}
}

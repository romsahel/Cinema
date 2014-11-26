/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import main.VideoManagerJAVA;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.Utils;
import videomanagerjava.files.Downloader;

/**
 *
 * @author Romsahel
 */
public class Media
{

	private final static String DEFAULT_KEY = "Files";
	private final TreeMap<String, TreeMap<String, Episode>> seasons;
	private final long id;
	private final HashMap<String, String> info;

	public Media(long id)
	{
		this(null, id, null);
	}

	/*
	 * Constructor that should only be called by the FileWalker since it creates a "Default" season
	 */
	public Media(String name, long id)
	{
		this(name, id, null);
		seasons.put(DEFAULT_KEY, new TreeMap<>());
	}

	public Media(String name, long id, String img)
	{
		info = new HashMap<>();
		this.seasons = new TreeMap<>();

		this.id = id;
		if (name != null)
		{
			info.put("year", Utils.getYear(name));
			info.put("name", Utils.formatName(name));
		}
		info.put("img", img);
	}

	public void downloadInfos()
	{
		final String formattedName = Utils.removeSeason(info.get("name"));
		final int limit = getInfo().get("year") == null ? 1 : 3;
		String url = "http://api.trakt.tv/search/movies.json/5921de65414d60b220c6296761061a3b?query="
					 + formattedName.replace(" ", "+")
					 + "&limit=" + limit;
		String type = getInfo().get("type");
		if ((type == null && !formattedName.equals(info.get("name")))
				|| (type != null && type.equals("show")))
		{
			getInfo().put("type", "show");
			url = url.replace("movies.json", "shows.json");
		}
		else
			getInfo().put("type", "movie");

		System.out.println(formattedName.replace(" ", "+"));
		System.out.println(url);

		JSONParser parser = new JSONParser();
		try
		{
			final String json = Downloader.downloadString(url);
			JSONArray array = (JSONArray) parser.parse(json);
			for (Object obj : array)
			{
				JSONObject jobj = (JSONObject) obj;
				final String newYear = jobj.get("year").toString();
				if (getInfo().get("year") == null || newYear.equals(getInfo().get("year")))
					try
					{
						getInfo().put("year", newYear);
						getInfo().put("overview", jobj.get("overview").toString());
						getInfo().put("genres", jobj.get("genres").toString());
						getInfo().put("imdb", jobj.get("imdb_id").toString());
						getInfo().put("duration", jobj.get("runtime").toString());

						final String imdbJson = Downloader.downloadString("http://www.omdbapi.com/?i="
																		  + getInfo().get("imdb")
																		  + "&plot=short&r=json");
						JSONObject jobjImdb = (JSONObject) parser.parse(imdbJson);
						getInfo().put("imdbRating", jobjImdb.get("imdbRating").toString());

						String imgURL = (String) ((JSONObject) jobj.get("images")).get("poster");
						imgURL = imgURL.replace(".jpg", "-300.jpg");

						getInfo().put("img", Downloader.downloadImage(imgURL));
						break;
					} catch (Exception e)
					{
						System.err.println("====");
						System.err.println(url);
					}
			}
		} catch (ParseException ex)
		{
			System.err.println("====");
			System.err.println(url);
			Logger.getLogger(VideoManagerJAVA.class.getName()).log(Level.SEVERE, null, ex);
		} finally
		{
			if (getInfo().get("img") == null)
				getInfo().put("img", "unknown.jpg");
		}
	}

	/**
	 * @return the seasons
	 */
	public TreeMap<String, TreeMap<String, Episode>> getSeasons()
	{
		return seasons;
	}

	public TreeMap<String, Episode> getDefaultSeason()
	{
		return seasons.get(DEFAULT_KEY);
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
		Pattern pattern = Pattern.compile("genres\":\"\\[(.*)\\]\"");
		Matcher matcher = pattern.matcher(array);
		if (matcher.find())
			array = array.replace(matcher.group(), "genres\": [" + matcher.group(1).replace("\\\"", "\"") + "]");
		return "\\" + array;
	}


	/**
	 * @return the id
	 */
	public long getId()
	{
		return id;
	}

	/**
	 * @return the info
	 */
	public HashMap<String, String> getInfo()
	{
		return info;
	}
}

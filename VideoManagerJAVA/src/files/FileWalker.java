/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package files;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import utils.Utils;
import videomanagerjava.Episode;
import videomanagerjava.Location;
import videomanagerjava.Media;

/**
 *
 * @author Romsahel
 */
public class FileWalker
{

	String location = null;
	private static final Map[] databases = new Map[]
	{
		Database.getInstance().getDatabase(),
		Database.getInstance().getDeletedMedias(),
		Database.getInstance().getMergedMedias()
	};

	private FileWalker()
	{
	}

	public ArrayList<Media> walk(String name, Location location)
	{
		File[] files = listFiles(new File(location.getPath()));
		ArrayList<Media> result = new ArrayList<>();
		if (location.isSpecial())
		{
			for (File file : files)
				result.addAll(walk(name, new Location(file.getAbsolutePath(), false)));
			return result;
		}

		if (files != null)
			//	We walk through all the files in the root folder
			for (File file : files)
			{
				final Media subWalk = walkRoot(file, databases);
				if (subWalk != null)
				{
					subWalk.setInfo("location", name);
					result.add(subWalk);
				}
			}

		return result;
	}

	public Media walkRoot(File f, Map[] databases)
	{
		final String path = f.getAbsolutePath();
		final long hashCode = path.hashCode();
//		We start by looking if this folder has already been walked
//		If it has, we return null so that it is not overwritten
		if (databases != null)
			for (Map db : databases)
				if (db.containsKey(hashCode))
					return null;

		String ext;
//		We create the media with a proper name and its id
		Media media;
		if (f.isDirectory())
		{
			media = new Media(path, hashCode);
			walkMedia(f, media, databases);
		}
		else if ((ext = isVideo(path, Utils.EXTENSIONS)) != null)
		{
			media = new Media(path.replace(ext, ""), hashCode);
			addEpisode(f, media);
		}
		else
			return null;

		media.removeEmptySeasons();

//		if media files have been found, we return the media. Otherwise, we destroy it
		if (media.getSeasons().size() > 0)
			return media;
		else
			return null;
	}

	private void addEpisode(final File file, Media media)
	{
		final Episode episode = new Episode(file);
		media.getDefaultSeason().put(episode.getProperties().get("name"), episode);
	}

	private void walkMedia(File f, Media media, Map[] databases)
	{
		File[] files = listFiles(f);
//		We loop through every file of the directory
		for (File file : files)
		{
			String path = file.getAbsolutePath();
			if (file.isDirectory())
			{
//				If it is a directory, we loop walk through it, and we add every found file as a 'season'
				final Media walk = walkRoot(file, databases);
				if (walk != null)
					media.getSeasons().put(walk.getInfo().get("name"), walk.getDefaultSeason());
			}
			else if (isVideo(path, Utils.EXTENSIONS) != null)
				addEpisode(file, media);
		}
	}

	private File[] listFiles(File folder)
	{
		File[] list = folder.listFiles();

		if (list == null)
			return null;
		else
			return list;
	}

	private String isVideo(String path, String... extensions)
	{
		for (String ext : extensions)
			if (path.endsWith(ext))
				return ext;
		return null;
	}

	// <editor-fold defaultstate="collapsed" desc="Singleton">
	public static FileWalker getInstance()
	{
		return FileWalkerHolder.INSTANCE;
	}

	private static class FileWalkerHolder
	{

		private static final FileWalker INSTANCE = new FileWalker();
	}
  // </editor-fold>
}

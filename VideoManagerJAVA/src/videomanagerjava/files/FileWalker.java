/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava.files;

import java.io.File;
import java.util.ArrayList;
import utils.Utils;
import videomanagerjava.Episode;
import videomanagerjava.Media;

/**
 *
 * @author Romsahel
 */
public class FileWalker
{

	private FileWalker()
	{
	}

	public ArrayList<Media> walk(String path)
	{
		File[] files = listFiles(new File(path));
		ArrayList<Media> result = new ArrayList<>();

		if (files != null)
			//	We walk through all the files in the root folder
			for (File file : files)
			{
				final Media subWalk = walkRoot(file);
				if (subWalk != null)
					result.add(subWalk);
			}

		return result;
	}

	private Media walkRoot(File f)
	{
		final String path = f.getAbsolutePath();
		final long hashCode = path.hashCode();
//		We start by looking if this folder has already been walked
		final Media get = Database.getInstance().getDatabase().get(hashCode);
//		If it has, we return the corresponding media in DB
		if (get != null)
			return get;
//		We create the media with a proper name and its id
		Media media = new Media(getCleanName(Utils.getSuffix(path, Utils.getSeparator())), hashCode);

		if (f.isDirectory())
			walkMedia(f, media);
		else if (isVideo(path, Utils.EXTENSIONS))
			addEpisode(f, media);

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

	private void walkMedia(File f, Media media)
	{
		File[] files = listFiles(f);
//		We loop through every file of the directory
		for (File file : files)
		{
			String path = file.getAbsolutePath();
			if (file.isDirectory())
			{
//				If it is a directory, we loop walk through it, and we add every found file as a 'season'
				final Media walk = walkRoot(file);
				if (walk != null)
					media.getSeasons().put(walk.getInfo().get("name"), walk.getDefaultSeason());
			}
			else if (isVideo(path, Utils.EXTENSIONS))
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

	/**
	 * Formats a full absolute path into a name by keeping only what follows the last separator
	 * and by removing everything after specified keywords (Utils.DUMP_KEYWORDS)
	 * <p>
	 * @param string the string to format
	 * <p>
	 * @return
	 */
	private String getCleanName(final String string)
	{
		if (string == null)
			return null;
		String file = Utils.getSuffix(string, Utils.getSeparator());
		file = Utils.getPrefix(file, Utils.DUMP_KEYWORDS);
		file = file.replace('.', ' ').trim();
		return file;
	}

	private boolean isVideo(String path, String... extensions)
	{
		for (String ext : extensions)
			if (path.endsWith(ext))
				return true;
		return false;
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

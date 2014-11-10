/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author Romsahel
 */
public class FileWalker
{

	private final String fileSeparator;

	private FileWalker()
	{
		if (!System.getProperty("os.name").contains("Windows"))
			fileSeparator = "/";
		else
			fileSeparator = "\\";
	}

	public ArrayList<Media> walk(String path)
	{
		File[] files = listFiles(new File(path));
		ArrayList<Media> result = new ArrayList<>();

		for (File file : files)
		{
			final Media subWalk = subWalk(file);
			if (subWalk != null)
				result.add(subWalk);
		}

		return result;
	}

	private Media subWalk(File f)
	{
		final String path = f.getAbsoluteFile().toString();
		final String folder = path.substring(path.lastIndexOf('\\') + 1);
		final long hashCode = path.hashCode();
		final Media get = Database.getInstance().getDatabase().get(hashCode);
		if (get != null)
			return get;

		Media media = new Media(getCleanName(folder), hashCode);
		if (f.isDirectory())
		{
			File[] files = listFiles(f);
			for (File file : files)
			{
				String fullpath = file.getAbsolutePath();
				if (file.isDirectory())
				{
					final Media walk = subWalk(file);
					if (walk != null)
						media.getSeasons().put(walk.getInfo().get("name"), walk.getFiles());
				}
				else if (isVideo(fullpath, Utils.EXTENSIONS))
					media.getFiles().put(getSuffix(fullpath, fileSeparator), fullpath);
			}

		}
		else if (isVideo(path, Utils.EXTENSIONS))
			media.getFiles().put(getSuffix(path, fileSeparator), path);

		if (media.getFiles().size() > 0 || media.getSeasons().size() > 0)
			return media;
		else
			return null;
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
		String file = getSuffix(string, fileSeparator);
		file = Utils.getPrefix(file, Utils.DUMP_KEYWORDS);
		file = file.replace('.', ' ').trim();
		return file;
	}

	private String getSuffix(String src, String... delimiter)
	{
		int i = Utils.findIndex(src, delimiter);

		if (i == -1)
			return src;
		else
			return src.substring(i + 1);
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

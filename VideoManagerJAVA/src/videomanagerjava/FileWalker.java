/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.io.File;

/**
 *
 * @author Romsahel
 */
public class FileWalker
{

  private FileWalker()
  {
  }

  public Media walk(String path)
  {
    File rootFolder = new File(path);
    File[] list = rootFolder.listFiles();

    if (list == null)
      return null;

    String fileSeparator = "\\";
    if (System.getProperty("os.name").equals("Linux"))
      fileSeparator = "/";

    final String folder = path.substring(path.lastIndexOf('\\') + 1);
    Media media = new Media(getCleanName(folder, fileSeparator));
    for (File f : list)
    {
      final String fileToString = f.getAbsoluteFile().toString();
      String file = getCleanName(fileToString, fileSeparator);

      if (f.isDirectory())
      {
        final Media walk = walk(f.getAbsolutePath());
        if (walk != null)
          media.getMedias().add(walk);
      }
      else if ((file = isVideo(fileToString, file, Utils.EXTENSIONS)) != null)
        media.getFiles().add(Utils.removeSeason(Utils.formatName(file), folder));
    }
    if (media.getFiles().size() > 0 || media.getMedias().size() > 0)
      return media;
    else
      return null;
  }

  private String getCleanName(final String fileToString, String fileSeparator)
  {
    if (fileToString == null)
      return null;
    String file = getSuffix(fileToString, fileSeparator);
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

  private String isVideo(String path, String filename, String... extensions)
  {
    for (String ext : extensions)
      if (path.endsWith(ext))
        return filename.replace(ext.substring(1), "");

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

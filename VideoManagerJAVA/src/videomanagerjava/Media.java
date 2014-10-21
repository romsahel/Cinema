/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.util.ArrayList;

/**
 *
 * @author Romsahel
 */
public class Media
{

  private final String name;
  private final ArrayList<Media> medias;
  private final ArrayList<String> files;

  public Media(String name)
  {
    this.name = name;
    this.medias = new ArrayList<>();
    this.files = new ArrayList<>();
  }

  /**
   * @return the name
   */
  public String getName()
  {
    return name;
  }

  /**
   * @return the medias
   */
  public ArrayList<Media> getMedias()
  {
    return medias;
  }

  /**
   * @return the files
   */
  public ArrayList<String> getFiles()
  {
    return files;
  }

  @Override
  public String toString()
  {
    String result = "== Name: " + name + " ==";
    if (files.size() > 0)
    {
      result += System.lineSeparator() + "== Files ==" + System.lineSeparator();
      for (String file : files)
        result += "file: " + file + System.lineSeparator();
    }
    if (medias.size() > 0)
    {
      result += System.lineSeparator() + "== Medias ==" + System.lineSeparator();
      for (Media media : medias)
        result += "media: " + media + System.lineSeparator();
    }
    result += System.lineSeparator();
    return result;
  }

}

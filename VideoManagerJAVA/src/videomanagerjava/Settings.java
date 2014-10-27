/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Romsahel
 */
public class Settings
{

  private final HashMap<String, String> locations;

  private Settings()
  {
    locations = new HashMap<>();
  }

  public void writeSettings()
  {
    JSONObject obj = new JSONObject();
    JSONArray locationsList = new JSONArray();

    for (Map.Entry<String, String> next : getLocations().entrySet())
    {
      JSONObject loc = new JSONObject();
      loc.put("name", next.getKey());
      loc.put("path", next.getValue());

      locationsList.add(loc);
    }

    obj.put("locations", locationsList);

    try (FileWriter file = new FileWriter("config.json"))
    {
      file.write(obj.toJSONString());
      file.flush();
    } catch (IOException ex)
    {
      Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void readSettings()
  {
    try
    {
      JSONParser parser = new JSONParser();

      Object obj = parser.parse(new FileReader("config.json"));

      JSONObject jsonObject = (JSONObject) obj;

      // loop array
      JSONArray locationsList = (JSONArray) jsonObject.get("locations");
      for (Iterator iterator = locationsList.iterator(); iterator.hasNext();)
      {
        JSONObject loc = (JSONObject) iterator.next();
        getLocations().put((String) loc.get("name"), (String) loc.get("path"));
      }
    } catch (IOException | ParseException ex)
    {
      Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
    }

    for (Map.Entry<String, String> next : getLocations().entrySet())
      System.out.println(next.getKey() + ": " + next.getValue());
  }

  // <editor-fold defaultstate="collapsed" desc="Singleton">
  public static Settings getInstance()
  {
    return SettingsHolder.INSTANCE;
  }

  /**
   * @return the locations
   */
  public HashMap<String, String> getLocations()
  {
    return locations;
  }

  private static class SettingsHolder
  {

    private static final Settings INSTANCE = new Settings();
  }
  // </editor-fold>
}

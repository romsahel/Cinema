/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.web.WebEngine;
import videomanagerjava.files.Database;

/**
 *
 * @author Romsahel
 */
public class DatabaseJS
{

	private final WebEngine webEngine;

	public DatabaseJS(WebEngine webEngine)
	{
		this.webEngine = webEngine;
	}

	public void playMedia(String id, String currentSeason, String currentEpisode)
	{
		final Media media = Database.getInstance().getDatabase().get(Long.parseLong(id, 10));
		final TreeMap<String, Episode> season = media.getSeasons().get(currentSeason);
		final Episode episode = season.get(currentEpisode);

		try
		{
			Desktop.getDesktop().open(new File(episode.getPath()));
		} catch (IOException ex)
		{
			Logger.getLogger(DatabaseJS.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void reload()
	{
		webEngine.reload();
	}
}

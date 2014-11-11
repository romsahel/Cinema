/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.web.WebEngine;
import videomanagerjava.files.Database;

/**
 *
 * @author Romsahel
 */
public class JsToJava
{

	private final WebEngine webEngine;

	public JsToJava(WebEngine webEngine)
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
			Logger.getLogger(JsToJava.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void reload()
	{
		webEngine.reload();
	}

	public void openLink(String link)
	{
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE))
			try
			{
				desktop.browse(new URI(link));
			} catch (URISyntaxException | IOException ex)
			{
				Logger.getLogger(JsToJava.class.getName()).log(Level.SEVERE, null, ex);
			}
	}
}

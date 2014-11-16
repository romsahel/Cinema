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
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import utils.Utils;
import videomanagerjava.files.Database;
import videomanagerjava.files.Settings;

/**
 *
 * @author Romsahel
 */
public class JsToJava
{

	private final CWebEngine cWebEngine;

	public JsToJava(CWebEngine cWebEngine)
	{
		this.cWebEngine = cWebEngine;
	}

	public void playMedia(String id, String currentSeason, String currentEpisode)
	{
		final Media media = Database.getInstance().getDatabase().get(Long.parseLong(id, 10));
		final TreeMap<String, Episode> season = media.getSeasons().get(currentSeason);
		final Episode episode = season.get(currentEpisode);

		VLCController.play(episode);
	}

	public void reload()
	{
		cWebEngine.getWebEngine().reload();
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

	public String addNewLocation()
	{
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Add new location");
		File defaultDirectory = new File("/");
		chooser.setInitialDirectory(defaultDirectory);
		File selectedDirectory = chooser.showDialog(VideoManagerJAVA.getStage());
		if (selectedDirectory != null)
		{
			final String folderPath = selectedDirectory.getAbsolutePath();
			final String name = Utils.getSuffix(folderPath, Utils.getSeparator());

			TextInputDialog dialog = new TextInputDialog(name);
			dialog.setHeaderText(null);
			dialog.setTitle("Add new location");
			dialog.setContentText("Name of the location: ");

			Optional<String> result = dialog.showAndWait();
			if (result.isPresent())
			{
				Settings.getInstance().getLocations().put(result.get(), folderPath);
				Settings.getInstance().writeSettings();
				cWebEngine.walkFiles(folderPath);
				cWebEngine.refreshList();
				return result.get();
			}
		}

		return "";
	}
}

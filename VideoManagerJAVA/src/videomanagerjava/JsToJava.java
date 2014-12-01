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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import main.VideoManagerJAVA;
import utils.Utils;
import videomanagerjava.files.Database;
import videomanagerjava.files.Settings;

/**
 *
 * @author Romsahel
 */
public class JsToJava
{

	public void playMedia(String id, String currentSeason, String currentEpisode, String lastEpisode)
	{
		if (lastEpisode == null)
		{
			Episode episode = getEpisode(id, currentSeason, currentEpisode);
			VLCController.play(episode);
		}
		else
		{
			final Media media = Database.getInstance().getDatabase().get(Long.parseLong(id, 10));
			final TreeMap<String, Episode> season = media.getSeasons().get(currentSeason);
			SortedMap<String, Episode> subMap = season.subMap(currentEpisode, true, lastEpisode, true);
			Episode[] array = new Episode[subMap.size()];
			VLCController.playAllFollowing(subMap.values().toArray(array));
		}
	}

	public void toggleSeen(String id, String currentSeason, String currentEpisode, boolean value, boolean reset)
	{
		Episode episode = getEpisode(id, currentSeason, currentEpisode);
		episode.setSeen(value);
		if (reset)
			episode.getProperties().put("time", "0");
	}

	private Episode getEpisode(String id, String currentSeason, String currentEpisode) throws NumberFormatException
	{
		final Media media = Database.getInstance().getDatabase().get(Long.parseLong(id, 10));
		final TreeMap<String, Episode> season = media.getSeasons().get(currentSeason);
		final Episode episode = season.get(currentEpisode);
		return episode;
	}

	public void reload()
	{
		CWebEngine.getWebEngine().reload();
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
				CWebEngine.walkFiles(folderPath);
				CWebEngine.refreshList();
				return result.get();
			}
		}

		return "";
	}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import files.Database;
import files.Settings;
import gnu.trove.map.hash.THashMap;
import java.awt.Desktop;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import main.Main;
import utils.Formatter;
import utils.Utils;
import vlcinterface.VLCController;

/**
 *
 * @author Romsahel
 */
public class JsToJava
{

	private static String currentEpisode = "";
	private static final FilenameFilter filter = (File file, String name)
			-> name.endsWith(".srt") && name.startsWith(getCurrentEpisode());

	/**
	 * @return the currentEpisode
	 */
	private static String getCurrentEpisode()
	{
		return currentEpisode;
	}

	/**
	 * @param aCurrentEpisode the currentEpisode to set
	 */
	private static void setCurrentEpisode(String aCurrentEpisode)
	{
		currentEpisode = aCurrentEpisode;
	}

	public void playMedia(String id, String currentSeason, String currentEpisode, String lastEpisode, boolean withSubtitles)
	{
		if (lastEpisode == null)
		{
			Episode episode = getEpisode(id, currentSeason, currentEpisode);
			VLCController.play(episode, withSubtitles);
		}
		else
		{
			final Media media = Database.getInstance().getDatabase().get(Long.parseLong(id, 10));
			final TreeMap<String, Episode> season = media.getSeasons().get(currentSeason);
			SortedMap<String, Episode> subMap = season.subMap(currentEpisode, true, lastEpisode, true);
			Episode[] array = new Episode[subMap.size()];
			VLCController.playAllFollowing(subMap.values().toArray(array), withSubtitles);
		}
	}

	public void toggleSeen(String id, String currentSeason, String currentEpisode, boolean value, boolean reset)
	{
		Episode episode = getEpisode(id, currentSeason, currentEpisode);
		episode.setSeen(value);
		if (reset)
			episode.setTime(0);
	}

	public void resetMedia(String id)
	{
		final Media media = Database.getInstance().getDatabase().get(Long.parseLong(id, 10));
		final TreeMap<String, TreeMap<String, Episode>> seasons = media.getSeasons();
		for (Map.Entry<String, TreeMap<String, Episode>> season : seasons.entrySet())
		{
			TreeMap<String, Episode> episodes = season.getValue();
			for (Map.Entry<String, Episode> episode : episodes.entrySet())
			{
                                episode.getValue().setTime(0);
                                episode.getValue().setSeen(false);
			}
		}
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
		System.out.println("Reload");
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
		File selectedDirectory = chooser.showDialog(Main.getStage());
		if (selectedDirectory != null)
		{
			final String folderPath = selectedDirectory.getAbsolutePath();
			final String name = Formatter.getSuffix(folderPath, Utils.getSeparator());

			TextInputDialog dialog = new TextInputDialog(name);
			dialog.setHeaderText(null);
			dialog.setTitle("Add new location");
			dialog.setContentText("Name of the location: ");

			GridPane grid = (GridPane) dialog.getDialogPane().getContent();
			CheckBox box = new CheckBox("Add each file as media");
			box.setPadding(new javafx.geometry.Insets(10, 10, 0, 10));
			grid.add(box, 1, 3);

			Optional<String> result = dialog.showAndWait();
			if (result.isPresent())
			{

				final Location location = new Location(folderPath, box.isSelected());
				Settings.getInstance().getLocations().put(result.get(), location);
				Settings.getInstance().writeSettings();
				CWebEngine.walkFiles();
				CWebEngine.refreshList();
				return result.get();
			}
		}

		return "";
	}

	public void debug(String message)
	{
		System.out.println(message);
	}

	public String[] getEpisodeIndicator(String id, String currentSeason, String currentEpisode)
	{
		Episode episode = getEpisode(id, currentSeason, currentEpisode);
		final String path = episode.getPath();

		File file = new File(path);
		if (!file.exists())
			return new String[]
			{
				"Not available", "Not available"
			};

		setCurrentEpisode(Formatter.getPrefix(file.getName(), "."));
		final String[] list = file.getParentFile().list(filter);
		return new String[]
		{
			"Available", list.length + " subtitles"
		};
	}

	public void toggleFavorite(String id)
	{
		final Media media = Database.getInstance().getDatabase().get(Long.parseLong(id, 10));
		String favorite = media.getInfo().get("favorite");
		if (favorite == null || favorite.equals("False"))
			media.setInfo("favorite", "True");
		else
			media.setInfo("favorite", "False");
	}
}

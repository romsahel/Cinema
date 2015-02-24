/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerupdater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Romsahel
 */
public class VideoManagerUpdater extends Application
{

	private static final String CURRENT_VERSION = "2.3.8";
	private static final StringBuilder changelog = new StringBuilder();
	private static String version;

	private static boolean downloadChangelog()
	{
		try
		{
			URL url = new URL("https://raw.githubusercontent.com/romsahel/Cinema/master/release/updates.txt");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream())))
			{
				version = in.readLine();

				if (Long.parseLong(version.replace(".", "")) <= Long.parseLong(CURRENT_VERSION.replace(".", "")))
					return false;

				String inputLine;
				changelog.append(version).append("\n");
				while ((inputLine = in.readLine()) != null)
					changelog.append(inputLine).append("\n");
			}
		} catch (IOException ex)
		{
			Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
		return true;
	}

	/**
	 * @return the changelog
	 */
	static String getChangelog()
	{
		return changelog.toString();
	}

	/**
	 * @return the version
	 */
	static String getVersion()
	{
		return version;
	}

	/**
	 * @return the CURRENT_VERSION
	 */
	public static String getCURRENT_VERSION()
	{
		return CURRENT_VERSION;
	}

	@Override
	public void start(Stage stage) throws Exception
	{
		show(stage);
	}

	public static void show(Stage owner)
	{
		if (!downloadChangelog())
			return;
		Parent root;
		try
		{
			root = FXMLLoader.load(VideoManagerUpdater.class.getResource("FXMLDocument.fxml"));
		} catch (IOException ex)
		{
			Logger.getLogger(VideoManagerUpdater.class.getName()).log(Level.SEVERE, null, ex);
			return;
		}

		Scene scene = new Scene(root);

		Stage stage = new Stage(StageStyle.UNDECORATED);
		stage.initOwner(owner);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setResizable(false);

		stage.setScene(scene);
		stage.show();
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		if (downloadChangelog())
			launch(args);
	}

}

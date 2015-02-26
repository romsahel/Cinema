/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerupdater;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 *
 * @author Romsahel
 */
public class FXMLDocumentController implements Initializable
{

	@FXML // fx:id="changelogLabel"
	private Label changelogLabel; // Value injected by FXMLLoader

	@FXML // fx:id="versionLabel"
	private Label versionLabel; // Value injected by FXMLLoader

	@Override
	public void initialize(URL url, ResourceBundle rb)
	{
		changelogLabel.setText(VideoManagerUpdater.getChangelog());
		final String text = "(Current version: %s; new version: %s)";
		versionLabel.setText(String.format(text,
										   VideoManagerUpdater.getVersion(),
										   VideoManagerUpdater.CURRENT_VERSION));
	}

	@FXML
	public void onUpdateNow(ActionEvent event)
	{
		Node source = (Node) event.getSource();
		Stage stage = (Stage) source.getScene().getWindow();
		stage.hide();
		System.out.println("Downloading update");
		final String pathToInstaller = VideoManagerUpdater.CURRENT_FOLDER + "cinema-installer.exe";
		try
		{
			URL installer = new URL("https://raw.githubusercontent.com/romsahel/Cinema/master/release/cinema-installer.exe");
			HttpURLConnection conn = (HttpURLConnection) installer.openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
			try (FileOutputStream fos = new FileOutputStream(pathToInstaller))
			{
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			}

			new ProcessBuilder("cmd", "/c", pathToInstaller, "/exenoui", "/qb").start();
		} catch (IOException ex)
		{
			System.out.println("Error: " + ex.toString());
			Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
		}

		System.out.println("Installed");
		try
		{
			new ProcessBuilder("java", "-jar", "VideoManagerJAVA.jar").start();
		} catch (IOException ex)
		{
			System.out.println("Error: " + ex.toString());
			Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
		}

		stage.close();
	}

	@FXML
	public void onUpdateLater(ActionEvent event)
	{
		Node source = (Node) event.getSource();
		Stage stage = (Stage) source.getScene().getWindow();
		stage.close();
	}

	@FXML
	public void onMousePressed(MouseEvent mouseEvent)
	{
		final Button source = (Button) mouseEvent.getSource();
		source.setStyle("-fx-text-fill: white;"
						+ "-fx-background-color: #9cb0ce");
	}

	@FXML
	public void onMouseReleased(MouseEvent mouseEvent)
	{
		final Button source = (Button) mouseEvent.getSource();
		source.setStyle("-fx-background-color: initial;"
						+ "-fx-text-fill: initial;");
	}

}

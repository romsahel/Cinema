/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import videomanagerjava.CContextMenu;
import videomanagerjava.CWebEngine;
import videomanagerjava.VLCController;
import videomanagerjava.files.Database;
import videomanagerjava.files.Settings;

/**
 *
 * @author Romsahel
 */
public class VideoManagerJAVA extends Application
{

	private static Stage stage;

	/**
	 * @return the stage
	 */
	public static Stage getStage()
	{
		return stage;
	}

	@Override
	public void start(Stage primaryStage)
	{
		stage = primaryStage;
		stage.initStyle(StageStyle.DECORATED);
		WebView webView = new WebView();

		final AnchorPane anchorPane = new AnchorPane(webView);

		//Set Layout Constraint
		AnchorPane.setTopAnchor(webView, 0.0);
		AnchorPane.setBottomAnchor(webView, 0.0);
		AnchorPane.setLeftAnchor(webView, 0.0);
		AnchorPane.setRightAnchor(webView, 0.0);

		//Create Scene
		final Scene scene = new Scene(anchorPane, 1300, 900);
		scene.getStylesheets().add("utils/main.css");

		Settings.getInstance().readSettings();
		Database.getInstance().readDatabase();
		final WebEngine webEngine = new CWebEngine(webView).getWebEngine();

		stage.setOnCloseRequest((WindowEvent we) ->
		{
			final HashMap<String, String> general = Settings.getInstance().getGeneral();

			general.put("currentMedia", (String) webEngine.executeScript("getCurrentId()"));
			general.put("currentSeason", (String) webEngine.executeScript("$(\"#seasons .selected\").text()"));
			general.put("currentEpisode", (String) webEngine.executeScript("$(\"#episodes > .selected > .selected > div\").text()"));
			Settings.getInstance().writeSettings();

			if (!VLCController.cancelTimer(true))
				Database.getInstance().writeDatabase();
		});

//		adding context menu
		CContextMenu cContextMenu = new CContextMenu(webEngine, webView);
		webView.setContextMenuEnabled(false);
		webView.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent e) ->
						{
							if (e.getButton() == MouseButton.SECONDARY)
								CContextMenu.show(e);
							else
								CContextMenu.hide();
		});

		stage.setTitle("Video Manager");
		stage.setScene(scene);

		stage.show();
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		Handler handler = null;
		try
		{
			handler = new FileHandler("cinema-log.xml");
		} catch (IOException | SecurityException ex)
		{
			Logger.getLogger(VideoManagerJAVA.class.getName()).log(Level.SEVERE, null, ex);
		}
		if (handler != null)
			Logger.getLogger("").addHandler(handler);

		launch(args);
	}
}

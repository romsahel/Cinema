/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import contextmenu.CContextMenu;
import files.Database;
import files.Settings;
import gnu.trove.map.hash.THashMap;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import videomanagerjava.CWebEngine;
import videomanagerjava.VLCController;

/**
 *
 * @author Romsahel
 */
public class Main extends Application
{

	private static Stage stage;
	private static boolean isReady;

	/**
	 * @return the stage
	 */
	public static Stage getStage()
	{
		return stage;
	}

	/**
	 * @param ready the isReady to set
	 */
	public static void setReady(boolean ready)
	{
		isReady = ready;
	}

	@Override
	public void start(Stage primaryStage)
	{

		stage = primaryStage;
		stage.initStyle(StageStyle.UNDECORATED);

		MainController fxml = new MainController(stage); // load an fxml
		stage.initStyle(StageStyle.TRANSPARENT); //undecorated/transparent
		final Scene scene = new Scene(fxml); // create a scene from new CustomDecorator
		scene.setFill(null);

		scene.getStylesheets().add("utils/main.css");

		Settings.getInstance().readSettings();
		Database.getInstance().readDatabase();
		Database.getInstance().backupDatabase();

		final WebView webView = fxml.getWebView();
		final WebEngine webEngine = new CWebEngine(webView).getWebEngine();

		stage.setOnCloseRequest((WindowEvent we) ->
		{
			closeApplication();
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

		ResizeHelper.addResizeListener(stage);
		stage.show();
	}

	public static void closeApplication()
	{
		stage.hide();
		if (isReady)
		{
			final WebEngine webEngine = CWebEngine.getWebEngine();
			final THashMap<String, String> general = Settings.getInstance().getGeneral();
			general.put("currentMedia", (String) webEngine.executeScript("getCurrentId()"));
			general.put("currentSeason", (String) webEngine.executeScript("$(\"#seasons .selected\").text()"));
			general.put("currentEpisode", (String) webEngine.executeScript("$(\"#episodes > .selected > .selected > div\").text()"));
//				general.put("playList", '\\' + (String) webEngine.executeScript("playList.toString()"));
//				general.put("withSubtitles", '\\' + (String) webEngine.executeScript("withSubtitles.toString()"));
			Settings.getInstance().writeSettings();

			if (!VLCController.cancelTimer(true))
				Database.getInstance().writeDatabase();
		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		Handler handler = null;
		try
		{
			handler = new FileHandler(utils.Utils.APPDATA + "log/cinema-log.xml");
			handler.setFormatter(new Formatter()
			{
				@Override
				public String format(LogRecord record)
				{
					if (record.getLevel() == Level.INFO)
						return record.getMessage() + "\r\n";
					else
						return record.toString();
				}
			});
		} catch (IOException | SecurityException ex)
		{
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
		if (handler != null)
			Logger.getLogger("").addHandler(handler);

		launch(args);
	}
}

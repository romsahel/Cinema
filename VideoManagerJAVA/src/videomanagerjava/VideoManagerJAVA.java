/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

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
import utils.Utils;
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
		final Scene scene = new Scene(anchorPane, 1600, 600);
		scene.getStylesheets().add("utils/main.css");

		Settings.getInstance().readSettings();
		Database.getInstance().readDatabase();
		final CWebEngine cWebEngine = new CWebEngine(webView);
		final WebEngine webEngine = cWebEngine.getWebEngine();

		stage.setOnCloseRequest((WindowEvent we) ->
		{
			final String currentId = Utils.callJS(webEngine, "getCurrentId()");
			Settings.getInstance().getGeneral().put("selectedId", currentId);
			Settings.getInstance().writeSettings();
			if (!VLCController.cancelTimer(true))
				Database.getInstance().writeDatabase();
		});

		//adding context menu
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
		launch(args);
	}
}

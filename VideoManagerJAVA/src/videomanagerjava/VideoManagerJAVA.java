/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
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
		final AnchorPane anchorPane = new AnchorPane();

		stage.initStyle(StageStyle.DECORATED);
		WebView webBrowser = new WebView();

		//Set Layout Constraint
		AnchorPane.setTopAnchor(webBrowser, 0.0);
		AnchorPane.setBottomAnchor(webBrowser, 0.0);
		AnchorPane.setLeftAnchor(webBrowser, 0.0);
		AnchorPane.setRightAnchor(webBrowser, 0.0);

		//Add WebView to AnchorPane
		anchorPane.getChildren().add(webBrowser);

		//Create Scene
		final Scene scene = new Scene(anchorPane, 1600, 600);

		Settings.getInstance().readSettings();
		Database.getInstance().readDatabase();
		final CWebEngine cWebEngine = new CWebEngine(webBrowser);

		stage.setOnCloseRequest((WindowEvent we) ->
		{
			final String currentId = Utils.callJS(cWebEngine.getWebEngine(), "getCurrentId()");
			Settings.getInstance().getGeneral().put("selectedId", currentId);
			Settings.getInstance().writeSettings();
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

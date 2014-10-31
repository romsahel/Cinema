/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Romsahel
 */
public class VideoManagerJAVA extends Application
{

  private Media media;
  private ExecutorService executor;

  @Override
  public void start(Stage primaryStage)
  {
	final AnchorPane anchorPane = new AnchorPane();
	primaryStage.initStyle(StageStyle.DECORATED);
	WebView webBrowser = new WebView();

	//Set Layout Constraint
	AnchorPane.setTopAnchor(webBrowser, 0.0);
	AnchorPane.setBottomAnchor(webBrowser, 0.0);
	AnchorPane.setLeftAnchor(webBrowser, 0.0);
	AnchorPane.setRightAnchor(webBrowser, 0.0);

	//Add WebView to AnchorPane
	anchorPane.getChildren().add(webBrowser);

	//Create Scene
	final Scene scene = new Scene(anchorPane);

	// Obtain the webEngine to navigate
	final WebEngine webEngine = webBrowser.getEngine();
	webEngine.load("file:///C:/Users/Romsahel/Documents/NetBeansProjects/VideoManagerHTML/public_html/index.html");
	webEngine.getLoadWorker().stateProperty().addListener(
			new javafx.beans.value.ChangeListener<State>()
			{
			  @Override
			  public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue)
			  {
				if (newValue != State.SUCCEEDED)
				  return;
				pageLoaded(webEngine);
			  }
			}
	);

	Settings.getInstance().readSettings();
	Database.getInstance().readDatabase();
	media = FileWalker.getInstance().walk(Settings.getInstance().getLocations().get("Torrents"));
	getImages();

	primaryStage.setTitle("Video Manager");
	primaryStage.setScene(scene);

	primaryStage.show();
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
	launch(args);
  }

  private void pageLoaded(WebEngine webEngine)
  {
	while (!executor.isTerminated());

	for (Media o : media.getMedias())
	{
	  Database.getInstance().getDatabase().put(o.getId(), o);
	  Utils.callJS(webEngine, "addMedia", o.getName(), "media/posters/" + o.getImg());
	}

	for (Map.Entry<String, String> next : Settings.getInstance().getLocations().entrySet())
	  Utils.callJS(webEngine, "addLocation", next.getKey());

	Database.getInstance().writeDatabase();
  }

  private void getImages()
  {
	executor = java.util.concurrent.Executors.newFixedThreadPool(4);
	for (Media o : media.getMedias())
	{
	  Thread t = new Thread()
	  {
		@Override
		public void run()
		{
		  if (o.getImg() == null)
			o.downloadImg();
		}
	  };
	  executor.execute(t);
	}
	executor.shutdown();
  }
}

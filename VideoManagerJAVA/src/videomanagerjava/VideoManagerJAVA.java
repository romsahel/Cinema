/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

/**
 *
 * @author Romsahel
 */
public class VideoManagerJAVA extends Application
{

  @Override
  public void start(Stage primaryStage)
  {
    final AnchorPane anchorPane = new AnchorPane();
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
                if (newValue == State.SUCCEEDED)
                {
                  Media media = FileWalker.getInstance().walk(Settings.getInstance().getLocations().get("Torrents"));
                  for (Media o : media.getMedias())
                    webEngine.executeScript("addMedia('" + o.getName() + "')");
                  EventListener listener = new EventListener()
                  {
                    public void handleEvent(Event ev)
                    {
                      Platform.exit();
                    }
                  };

                  Document doc = webEngine.getDocument();
                  Element el = doc.getElementById("sortOption");
//                  el.setTextContent("coucou");
                }
              }
            });

    Settings.getInstance().readSettings();

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

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
                  {
                    final String name = o.getName();
                    final String formattedName = Utils.removeSeason(name);
                    String url = "http://api.trakt.tv/search/movies.json/5921de65414d60b220c6296761061a3b?query="
                                 + formattedName.replace(" ", "+")
                                 + "&limit=1";
                    if (!formattedName.equals(name))
                      url = url.replace("movies.json", "shows.json");

                    JSONParser parser = new JSONParser();
                    String img = "";
                    try
                    {
                      final String json = Utils.download(url);
                      JSONObject obj = (JSONObject) parser.parse(json.substring(1, json.length() - 1));
                      img = (String) ((JSONObject) obj.get("images")).get("poster");
                    } catch (ParseException ex)
                    {
                      Logger.getLogger(VideoManagerJAVA.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    Utils.callJS(webEngine, "addMedia", name, img);
                    break;
                  }
                  for (Map.Entry<String, String> next : Settings.getInstance().getLocations().entrySet())
                    Utils.callJS(webEngine, "addLocation", next.getKey());

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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import netscape.javascript.JSObject;

/**
 * FXML Controller class
 *
 * @author Romsahel
 */
public class MainController extends BorderPane
{

	private static boolean shouldBeLoading;

	@FXML
	private WebView webView;
	private static WebView staticWebView;
	@FXML
	private StackPane loadingPane;
	private static StackPane staticLoadingPane;

	@FXML
	private WebView loadingScreen;
	private static WebView staticLoadingScreen;
	private static LoadingScreenInterface loadingScreenInterface;
	@FXML
	private Label loadingLabel;
	private static Label staticLoadingLabel;

	private double dragDeltaX;
	private double dragDeltaY;
	private final Stage stage;
	private final Screen screen;

	public MainController(Stage stage)
	{
		load();

		screen = Screen.getPrimary();

		staticLoadingScreen = loadingScreen;
		staticWebView = webView;
		staticLoadingLabel = loadingLabel;
		staticLoadingPane = loadingPane;

		loadingScreenInterface = new LoadingScreenInterface();
		((JSObject) loadingScreen.getEngine().executeScript("window")).setMember("app", loadingScreenInterface);

		staticLoadingPane.setVisible(true);
		staticWebView.setVisible(false);

		loadingScreen.getEngine().load(getClass().getResource("loadingScreen.html").toExternalForm());
		this.stage = stage;

		int width = 940;
		int height = 640;
		this.setMinWidth(width);
		this.setMinHeight(height);

		Rectangle2D bounds = screen.getVisualBounds();
		final double maxY = bounds.getMaxY() * 0.85;
		final double maxX = bounds.getMaxX() * 0.85;
		while (width < maxX && height < maxY)
		{
			width *= 1.1;
			height *= 1.1;
		}
		this.setPrefWidth(width);
		this.setPrefHeight(height);
	}

	private void load() throws RuntimeException
	{
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try
		{
			fxmlLoader.load();
		} catch (IOException exception)
		{
			throw new RuntimeException(exception);
		}

	}

	@FXML
	protected void onMinimize(ActionEvent event)
	{
		stage.setIconified(true);
	}

	@FXML
	protected void onMaximize(ActionEvent event)
	{
		stage.setMaximized(!stage.maximizedProperty().getValue());
	}

	@FXML
	protected void onClose(ActionEvent event)
	{
		Main.closeApplication();
	}

	@FXML
	protected void onDrag(MouseEvent mouseEvent)
	{
		if (stage.getScene().getCursor() != Cursor.DEFAULT)
			return;
		stage.setX(stage.getX() + (mouseEvent.getScreenX() - dragDeltaX));
		stage.setY(stage.getY() + (mouseEvent.getScreenY() - dragDeltaY));
		updateDelta(mouseEvent);
	}

	@FXML
	protected void onDragStarted(MouseEvent mouseEvent)
	{
		if (stage.getScene().getCursor() != Cursor.DEFAULT)
			return;
		updateDelta(mouseEvent);
		if (stage.maximizedProperty().getValue())
			resizeWindow(dragDeltaX - this.getPrefWidth() / 2, dragDeltaY, this.getPrefWidth(), this.getPrefHeight(), false);
	}

	private void updateDelta(MouseEvent mouseEvent)
	{
		dragDeltaX = mouseEvent.getScreenX();
		dragDeltaY = mouseEvent.getScreenY();
	}

	@FXML
	protected void onDragEnded(MouseEvent mouseEvent)
	{
		double x = stage.getX();
		final double width = stage.getWidth();

		Rectangle2D bounds = screen.getVisualBounds();
		final double minX = bounds.getMinX();
		final double minY = bounds.getMinY();
		final double maxX = bounds.getMaxX();
		if (x + width / 3 < minX)
			resizeWindow(minX, minY, bounds.getWidth() / 2, bounds.getHeight(), true);
		else if (x + width / 2 > maxX)
			resizeWindow(bounds.getWidth() / 2, minY, bounds.getWidth() / 2, bounds.getHeight(), true);
		else if (stage.getY() < minY)
			resizeWindow(minX, minY, bounds.getWidth(), bounds.getHeight(), true);
	}

	private void resizeWindow(final double x, final double y, final double w, final double h, final boolean max)
	{
		stage.setMaximized(max);
		stage.setX(x);
		stage.setY(y);
		stage.setWidth(w);
		stage.setHeight(h);
	}

	public static void logLoadingScreen(String message)
	{
		if (staticLoadingScreen.isVisible())
			Platform.runLater(() ->
			{
				staticLoadingLabel.setText(message + "...");
			});
	}

	public static void startLoading()
	{
		startLoading(null, "Loading");
	}

	public static void startLoading(Thread thread, String log)
	{
		shouldBeLoading = true;
		if (staticWebView.isVisible())
			Platform.runLater(() ->
			{
				staticWebView.setVisible(true);
				staticLoadingScreen.getEngine().executeScript(
						LoadingScreenInterface.HTML_CANCEL + ".classList.remove('canceled');"
				);
				FadeTransition ftOut = new FadeTransition(Duration.millis(500), staticWebView);
				ftOut.setFromValue(1.0);
				ftOut.setToValue(0.0);
				ftOut.play();

				FadeTransition ftIn = new FadeTransition(Duration.millis(500), staticLoadingPane);
				ftIn.setFromValue(0.0);
				ftIn.setToValue(1);
				ftIn.play();
				staticLoadingPane.setVisible(true);

				staticLoadingLabel.setText(log + "...");
				loadingScreenInterface.setThread(thread);
				ftOut.setOnFinished((ActionEvent event) ->
				{
					if (shouldBeLoading)
					{
						shouldBeLoading = false;
						staticWebView.setVisible(false);
					}
				});
			});
	}

	public static void stopLoading()
	{
		shouldBeLoading = false;
		if (staticLoadingPane.isVisible())
			Platform.runLater(() ->
			{
				staticLoadingScreen.getEngine().executeScript(
						LoadingScreenInterface.HTML_CANCEL + ".classList.add('canceled');"
				);
				staticLoadingPane.setVisible(true);
				FadeTransition ftOut = new FadeTransition(Duration.millis(1000), staticLoadingPane);
				ftOut.setFromValue(1.0);
				ftOut.setToValue(0.0);
				ftOut.play();

				FadeTransition ftIn = new FadeTransition(Duration.millis(1000), staticWebView);
				ftIn.setFromValue(0.0);
				ftIn.setToValue(1);
				ftIn.play();
				staticWebView.setVisible(true);
				ftOut.setOnFinished((ActionEvent event) ->
				{
					staticLoadingPane.setVisible(false);
				});
			});
	}

	/**
	 * @return the webView
	 */
	public WebView getWebView()
	{
		return webView;
	}

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Romsahel
 */
public class MainController extends BorderPane
{

	@FXML
	private WebView webView;
	private double dragDeltaX;
	private double dragDeltaY;
	private final Stage stage;

	public MainController(Stage stage)
	{
		load();
		webView.setVisible(true);
		this.stage = stage;
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
	protected void onDrag(MouseEvent mouseEvent)
	{
		stage.setX(stage.getX() + (mouseEvent.getScreenX() - dragDeltaX));
		stage.setY(stage.getY() + (mouseEvent.getScreenY() - dragDeltaY));
		onDragStarted(mouseEvent);
	}

	@FXML
	protected void onDragStarted(MouseEvent mouseEvent)
	{
		dragDeltaX = mouseEvent.getScreenX();
		dragDeltaY = mouseEvent.getScreenY();
		if (stage.maximizedProperty().getValue())
			resizeWindow(dragDeltaX - this.getPrefWidth() / 2, dragDeltaY, this.getPrefWidth(), this.getPrefHeight(), false);
	}

	@FXML
	protected void onDragEnded(MouseEvent mouseEvent)
	{
		double x = stage.getX();
		final double width = stage.getWidth();

		Screen screen = Screen.getPrimary();
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

	/**
	 * @return the webView
	 */
	public WebView getWebView()
	{
		return webView;
	}

}

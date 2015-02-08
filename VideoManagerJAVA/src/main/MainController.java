/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import contextmenu.menus.ContextFiles;
import files.Database;
import files.Settings;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import netscape.javascript.JSObject;
import videomanagerjava.CWebEngine;

/**
 * FXML Controller class
 *
 * @author Romsahel
 */
public class MainController extends BorderPane
{

	@FXML
	private WebView webView, loadingScreen;
	@FXML
	private StackPane loadingPane, settingsPane;
	@FXML
	private Label loadingLabel;
	@FXML
	private TableView<ArrayList<String>> deletedTable;
	@FXML
	private CheckBox mergedCheck, deletedCheck;

	private double dragDeltaX;
	private double dragDeltaY;
	private final Stage stage;
	private final Screen screen;
	private boolean shouldBeLoading;
	private final LoadingScreenInterface loadingScreenInterface;
	private boolean itemsUndeleted;

	private MainController()
	{
		load();

		settingsPane.setVisible(false);

		screen = Screen.getPrimary();

		loadingScreenInterface = new LoadingScreenInterface();
		((JSObject) loadingScreen.getEngine().executeScript("window")).setMember("app", loadingScreenInterface);

		loadingScreen.getEngine().load(getClass().getResource("res/loadingScreen.html").toExternalForm());
		this.stage = Main.getStage();

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

	@SuppressWarnings("unchecked")
	@FXML
	public void initialize()
	{
		final ObservableList<TableColumn<ArrayList<String>, ?>> columns = deletedTable.getColumns();
		for (int i = 0; i < 4; i++)
		{
			final int j = i;
			final TableColumn<ArrayList<String>, String> get = (TableColumn<ArrayList<String>, String>) columns.get(j);
			get.setCellValueFactory((TableColumn.CellDataFeatures<ArrayList<String>, String> p) ->
			{
				return new SimpleStringProperty(p.getValue().get(j));
			});
		}

		MenuItem addMenuItem = new MenuItem("Unmerge / Undelete");
		addMenuItem.setOnAction((ActionEvent event) ->
		{
			itemsUndeleted = true;
			final ArrayList<String> selectedItem = deletedTable.getSelectionModel().getSelectedItem();
			String id = selectedItem.get(3);
			if (Database.getInstance().getDeletedMedias().remove(Long.valueOf(id)) == null)
				Database.getInstance().getMergedMedias().remove(Long.valueOf(id));
			deletedTable.getItems().remove(selectedItem);
		});
		MenuItem deleteMenuItem = new MenuItem("Show in Explorer");
		deleteMenuItem.setOnAction((ActionEvent event) ->
		{
			String path = deletedTable.getSelectionModel().getSelectedItem().get(2);
			try
			{
				Desktop.getDesktop().open(new File(path));
			} catch (IOException ex)
			{
				Logger.getLogger(ContextFiles.class.getName()).log(Level.SEVERE, null, ex);
			}
		});
		deletedTable.setContextMenu(new ContextMenu(addMenuItem, deleteMenuItem));
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
	protected void onSettings(ActionEvent event)
	{
		if (!settingsPane.isVisible())
		{
			itemsUndeleted = false;
			FadeTransition ftOut = new FadeTransition(Duration.millis(250), settingsPane);
			ftOut.setFromValue(0.0);
			ftOut.setToValue(0.98);
			ftOut.play();
			settingsPane.setVisible(true);

			final GaussianBlur gaussianBlur = new GaussianBlur(15);
			loadingPane.setEffect(gaussianBlur);
			webView.setEffect(gaussianBlur);

			populateDeletedTable(true, true);

			ftOut.setOnFinished((ActionEvent event1) ->
			{
				deletedTable.autosize();
			});
		}
		else
		{
			double duration = 250;
			if (itemsUndeleted)
			{
				CWebEngine.walkFiles();
				CWebEngine.refreshList();
				duration = 500;
			}
			else
			{
				loadingPane.setEffect(null);
				webView.setEffect(null);
			}
			FadeTransition ftOut = new FadeTransition(Duration.millis(duration), settingsPane);
			ftOut.setFromValue(0.98);
			ftOut.setToValue(0.0);
			ftOut.play();

			ftOut.setOnFinished((ActionEvent event1) ->
			{
				settingsPane.setVisible(false);
				loadingPane.setEffect(null);
				webView.setEffect(null);
			});

		}

	}

	private void populateDeletedTable(boolean showDeleted, boolean showMerged)
	{
		final ObservableList<ArrayList<String>> items = FXCollections.observableArrayList();
		if (showDeleted)
			items.addAll(Database.getInstance().getDeletedMedias().values());
		if (showMerged)
			items.addAll(Database.getInstance().getMergedMedias().values());
		deletedTable.setItems(items);
	}

	@FXML
	protected void onCheck(ActionEvent event)
	{
		populateDeletedTable(deletedCheck.isSelected(), mergedCheck.isSelected());
	}

	@FXML
	protected void onAutoMergeCheck(ActionEvent event)
	{
		final boolean selected = ((CheckBox) event.getSource()).isSelected();
		Settings.getInstance().getGeneral().put("automerge", (selected) ? "1" : "0");
	}

	@FXML
	protected void onHelp(ActionEvent event)
	{
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

	public void logLoadingScreen(String message)
	{
		if (loadingScreen.isVisible())
			Platform.runLater(() ->
			{
				loadingLabel.setText(message + "...");
			});
	}

	public void startLoading()
	{
		startLoading(null, "Loading");
	}

	public void startLoading(Thread thread, String log)
	{
		shouldBeLoading = true;
		if (webView.isVisible())
			Platform.runLater(() ->
			{
				webView.setVisible(true);
				loadingScreen.getEngine().executeScript(
						LoadingScreenInterface.HTML_CANCEL + ".classList.remove('canceled');"
				);
				FadeTransition ftOut = new FadeTransition(Duration.millis(500), webView);
				ftOut.setFromValue(1.0);
				ftOut.setToValue(0.0);
				ftOut.play();

				FadeTransition ftIn = new FadeTransition(Duration.millis(500), loadingPane);
				ftIn.setFromValue(0.0);
				ftIn.setToValue(1);
				ftIn.play();
				loadingPane.setVisible(true);

				loadingLabel.setText(log + "...");
				loadingScreenInterface.setThread(thread);
				ftOut.setOnFinished((ActionEvent event) ->
				{
					if (shouldBeLoading)
					{
						shouldBeLoading = false;
						webView.setVisible(false);
					}
				});
			});
	}

	public void stopLoading()
	{
		shouldBeLoading = false;
		if (loadingPane.isVisible())
			Platform.runLater(() ->
			{
				loadingScreen.getEngine().executeScript(
						LoadingScreenInterface.HTML_CANCEL + ".classList.add('canceled');"
				);
				loadingPane.setVisible(true);
				FadeTransition ftOut = new FadeTransition(Duration.millis(1000), loadingPane);
				ftOut.setFromValue(1.0);
				ftOut.setToValue(0.0);
				ftOut.play();

				FadeTransition ftIn = new FadeTransition(Duration.millis(1000), webView);
				ftIn.setFromValue(0.0);
				ftIn.setToValue(1);
				ftIn.play();
				webView.setVisible(true);
				ftOut.setOnFinished((ActionEvent event) ->
				{
					loadingPane.setVisible(false);
				});
			});
	}

	@FXML
	protected void onKeyReleased(KeyEvent keyEvent)
	{
		final KeyCode keycode = keyEvent.getCode();
		if (loadingPane.isVisible())
			return;
		if (settingsPane.isVisible())
			if (keycode == KeyCode.ESCAPE)
				onSettings(null);
			else
				return;

		if (keycode == KeyCode.ENTER)
			utils.Utils.callFuncJS(CWebEngine.getWebEngine(), "unfocusSearch", "\\true");
		else if (keycode.isLetterKey() || keycode.isDigitKey() || keycode == KeyCode.SPACE)
		{
			final String text = (keyEvent.isShiftDown()) ? keyEvent.getText().toUpperCase() : keyEvent.getText();
			utils.Utils.callFuncJS(CWebEngine.getWebEngine(), "focusSearch", text);
		}
		else if (keycode == KeyCode.ESCAPE)
			utils.Utils.callFuncJS(CWebEngine.getWebEngine(), "unfocusSearch");
		else if (keycode == KeyCode.DOWN || keycode == KeyCode.UP)
		{
			final String isDown = Boolean.toString(keycode == KeyCode.DOWN);
			utils.Utils.callFuncJS(CWebEngine.getWebEngine(), "selectSiblingMedia", '\\' + isDown);
		}
	}

	/**
	 * @return the webView
	 */
	public WebView getWebView()
	{
		return webView;
	}

	// <editor-fold defaultstate="collapsed" desc="Singleton">
	public static MainController getInstance()
	{
		return MainControllerHolder.INSTANCE;
	}

	private static class MainControllerHolder
	{

		private static final MainController INSTANCE = new MainController();
	}
  // </editor-fold>
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import com.sun.webkit.dom.HTMLDivElementImpl;
import com.sun.webkit.dom.HTMLElementImpl;
import com.sun.webkit.dom.HTMLLIElementImpl;
import editdialog.EditDialog;
import java.util.Optional;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import videomanagerjava.files.Database;
import videomanagerjava.files.Settings;

/**
 *
 * @author Romsahel
 */
public class CContextMenu
{

	private static WebEngine webEngine;
	private static WebView webView;

	private static final ContextMenu locationsMenu = new ContextMenu();
	private static final ContextMenu mediasMenu = new ContextMenu();

	private static ContextMenu currentMenu;
	private static HTMLElementImpl hovered;

	public CContextMenu(WebEngine engine, WebView view)
	{
		webEngine = engine;
		webView = view;

		locationsMenuInit();
		mediasMenuInit();
	}

	private void mediasMenuInit()
	{
		final ObservableList<MenuItem> filesItems = mediasMenu.getItems();

		MenuItem labelItem = new MenuItem("Label");
		labelItem.setDisable(true);

		MenuItem editItem = new MenuItem("Edit");
		editItem.setOnAction((ActionEvent event) ->
		{
			final String id = hovered.getParentElement().getAttribute("id");
			final Media media = Database.getInstance().getDatabase().get(Long.parseLong(id, 10));
			new EditDialog(media).show();
			hide();
		});

		MenuItem removeItem = new MenuItem("Remove");
		removeItem.setOnAction((ActionEvent event) ->
		{
			hide();
		});

		filesItems.add(labelItem);
		filesItems.add(editItem);
		filesItems.add(removeItem);
	}

	private void locationsMenuInit()
	{
		final ObservableList<MenuItem> locationItems = locationsMenu.getItems();

		MenuItem labelItem = new MenuItem("Label");
		labelItem.setDisable(true);

		MenuItem removeItem = new MenuItem("Remove");
		removeItem.setOnAction((ActionEvent event) ->
		{
			final String toRemove = (String) webEngine.executeScript("removeLocation()");
			Settings.getInstance().getLocations().remove(toRemove);
			CWebEngine.walkFiles();
			CWebEngine.refreshList();
			hide();
		});

		MenuItem renameItem = new MenuItem("Rename");
		renameItem.setOnAction((ActionEvent event) ->
		{
			final String toRename = hovered.getInnerText();
			final String location = Settings.getInstance().getLocations().get(toRename);

			TextInputDialog dialog = new TextInputDialog(toRename);
			dialog.setHeaderText(null);
			dialog.setTitle("Rename location");
			dialog.setContentText("Name of the location: ");

			Optional<String> result = dialog.showAndWait();
			if (result.isPresent())
			{
				Settings.getInstance().getLocations().remove(toRename);
				Settings.getInstance().getLocations().put(result.get(), location);
				hovered.setTextContent(result.get());
			}
			hide();
		});

		locationItems.add(labelItem);
		locationItems.add(renameItem);
		locationItems.add(removeItem);
	}

	public static void show(MouseEvent e)
	{
		hide();
		if (locationContext(e))
			return;
		if (mediasContext(e))
			return;
	}

	private static boolean locationContext(MouseEvent e)
	{
		final Object object = webEngine.executeScript("$(\"#locationsList li:hover\")[0]");
		if (object.getClass() == HTMLLIElementImpl.class)
		{
			hovered = (HTMLLIElementImpl) object;
			final String currentText = hovered.getInnerText();
			if (!currentText.equals("All"))
			{
				hovered.setClassName("hover");
				currentMenu = locationsMenu;
				locationsMenu.getItems().get(0).setText(currentText);
				locationsMenu.show(webView, e.getScreenX(), e.getScreenY());
			}
			return true;
		}
		return false;
	}

	private static boolean mediasContext(MouseEvent e)
	{
		final Object object = webEngine.executeScript("$(\".poster:hover\")[0]");
		if (object.getClass() == HTMLDivElementImpl.class)
		{
			hovered = (HTMLDivElementImpl) object;
			String currentText = hovered.getNextElementSibling().getTextContent();

			currentMenu = mediasMenu;
			mediasMenu.getItems().get(0).setText(currentText);
			mediasMenu.show(webView, e.getScreenX(), e.getScreenY());
			return true;
		}
		return false;
	}

	public static void hide()
	{
		if (currentMenu != null)
		{
			currentMenu.hide();
			if (currentMenu == locationsMenu)
				hovered.setClassName("");
		}
		hovered = null;
		currentMenu = null;
	}
}

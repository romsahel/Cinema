/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import com.sun.webkit.dom.HTMLDivElementImpl;
import com.sun.webkit.dom.HTMLElementImpl;
import com.sun.webkit.dom.HTMLLIElementImpl;
import java.util.Optional;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.w3c.dom.Element;
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
	private static final ContextMenu filesMenu = new ContextMenu();

	private static ContextMenu currentMenu;
	private static HTMLLIElementImpl hovered;

	public CContextMenu(WebEngine engine, WebView view)
	{
		webEngine = engine;
		webView = view;

		locationsMenuInit();
		filesMenuInit();
	}

	private void filesMenuInit()
	{
		final ObservableList<MenuItem> filesItems = filesMenu.getItems();

		MenuItem labelItem = new MenuItem("Label");
		labelItem.setDisable(true);

		MenuItem removeItem = new MenuItem("Remove");
		removeItem.setOnAction((ActionEvent event) ->
		{
		});

		MenuItem renameItem = new MenuItem("Rename");
		renameItem.setOnAction((ActionEvent event) ->
		{

		});

		MenuItem seenItem = new MenuItem("Toggle seen");
		seenItem.setOnAction((ActionEvent event) ->
		{
			final Element parent = hovered.getParentElement();
			final String id = parent.getAttribute("id");
			if (id != null && id.equals("seasons"))
			{

			}
			else
			{
				toggle(hovered);
			}
			hide();
		});

		filesItems.add(labelItem);
		filesItems.add(seenItem);
		filesItems.add(renameItem);
		filesItems.add(removeItem);
	}

	private void toggle(HTMLLIElementImpl element)
	{
		final HTMLDivElementImpl div = (HTMLDivElementImpl) element.getElementsByTagName("div").item(0);
		final HTMLElementImpl item = (HTMLElementImpl) div.getElementsByTagName("span").item(0);
		item.click();
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
		if (filesContext(e))
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

	private static boolean filesContext(MouseEvent e)
	{
		final Object object = webEngine.executeScript("$(\"#files li:hover\")[0]");
		if (object.getClass() == HTMLLIElementImpl.class)
		{
			hovered = (HTMLLIElementImpl) object;
			String currentText = hovered.getInnerText();
			final int indexOf = currentText.indexOf("\n");
			currentText = currentText.substring(indexOf > 0 ? indexOf : 0).trim();

			currentMenu = filesMenu;
			filesMenu.getItems().get(0).setText(currentText);
			filesMenu.show(webView, e.getScreenX(), e.getScreenY());
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

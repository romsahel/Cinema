/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package videomanagerjava;

import com.sun.webkit.dom.HTMLLIElementImpl;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
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
	private static ContextMenu currentMenu;
	private static HTMLLIElementImpl hovered;

	public CContextMenu(WebEngine engine, WebView view)
	{
		webEngine = engine;
		webView = view;

		final ObservableList<MenuItem> locationItems = locationsMenu.getItems();

		MenuItem labelItem = new MenuItem("Label");
		labelItem.setDisable(true);
		locationItems.add(labelItem);

		MenuItem removeItem = new MenuItem("Remove");
		removeItem.setOnAction((ActionEvent event) ->
		{
			final String toRemove = (String) webEngine.executeScript("removeLocation()");
			Settings.getInstance().getLocations().remove(toRemove);
			CWebEngine.walkFiles();
			CWebEngine.refreshList();
		});
		locationItems.add(removeItem);
	}

	public static void show(MouseEvent e)
	{
		hide();
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
				return;
			}
		}
	}

	public static void hide()
	{
		if (currentMenu != null)
		{
			currentMenu.hide();
			hovered.setClassName("");
		}
		hovered = null;
		currentMenu = null;
	}
}

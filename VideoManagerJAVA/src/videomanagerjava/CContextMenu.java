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
import gnu.trove.map.hash.THashMap;
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
	private static final ContextMenu filesMenu = new ContextMenu();

	private static ContextMenu currentMenu;
	private static HTMLElementImpl hovered;
	private static boolean isSeason;

	public CContextMenu(WebEngine engine, WebView view)
	{
		webEngine = engine;
		webView = view;

		locationsMenuInit();
		mediasMenuInit();
		filesMenuInit();
	}

	private void filesMenuInit()
	{
		final ObservableList<MenuItem> filesItems = filesMenu.getItems();

		MenuItem labelItem = new MenuItem("Label");
		labelItem.setDisable(true);

		MenuItem seenItem = new MenuItem("Toggle seen");
		seenItem.setOnAction((ActionEvent event) ->
		{
			if (isSeason)
				webEngine.executeScript("toggleSeenSeason()");
			else
				toggle(hovered);
			hide();
		});

		MenuItem seenUntilItem = new MenuItem("Toggle seen until there");
		seenUntilItem.setOnAction((ActionEvent event) ->
		{
			hide();
			webEngine.executeScript("toggleEpisodesUntilThere()");
		});

		MenuItem resetItem = new MenuItem("Reset");
		resetItem.setOnAction((ActionEvent event) ->
		{
			if (isSeason)
				webEngine.executeScript("toggleSeenSeason(true)");
			else
			{
				final HTMLDivElementImpl div = (HTMLDivElementImpl) hovered.getElementsByTagName("div").item(0);
				final HTMLElementImpl item = (HTMLElementImpl) div.getElementsByTagName("span").item(0);

				webEngine.executeScript("toggleSeen(null, false, false, true)");
			}
			hide();
		});

		filesItems.add(labelItem);
		filesItems.add(seenItem);
		filesItems.add(seenUntilItem);
		filesItems.add(resetItem);
	}

	private static boolean isSeason()
	{
		final Element parent = hovered.getParentElement();
		final String id = parent.getAttribute("id");
		isSeason = (id != null && id.equals("seasons"));
		return isSeason;
	}

	private void toggle(HTMLElementImpl element)
	{
		final HTMLDivElementImpl div = (HTMLDivElementImpl) element.getElementsByTagName("div").item(0);
		final HTMLElementImpl item = (HTMLElementImpl) div.getElementsByTagName("span").item(0);
		item.click();
	}

	private void mediasMenuInit()
	{
		final ObservableList<MenuItem> filesItems = mediasMenu.getItems();

		MenuItem labelItem = new MenuItem("Label");
		labelItem.setDisable(true);

		MenuItem editItem = new MenuItem("Edit");
		final THashMap<Long, Media> database = Database.getInstance().getDatabase();
		editItem.setOnAction((ActionEvent event) ->
		{
			final String id = hovered.getParentElement().getAttribute("id");
			hide();
			final Media media = database.get(Long.parseLong(id, 10));
			new EditDialog(media).show();
		});

		MenuItem removeItem = new MenuItem("Remove");
		removeItem.setOnAction((ActionEvent event) ->
		{
			final String id = hovered.getParentElement().getAttribute("id");
			hide();
			database.put(Long.valueOf(id), null);
			webEngine.executeScript("$('#" + id + "').fadeOut(200)");
			Database.getInstance().writeDatabase();
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
		if (filesContext(e))
			return;
	}

	private static boolean filesContext(MouseEvent e)
	{
		final Object object = webEngine.executeScript("$(\"#files li:hover\")[0]");
		if (object.getClass() == HTMLLIElementImpl.class)
		{
			hovered = (HTMLLIElementImpl) object;
			hovered.click();

			String currentText = hovered.getInnerText();
			final int indexOf = currentText.indexOf("\n");
			currentText = currentText.substring(indexOf > 0 ? indexOf : 0).trim();

			currentMenu = filesMenu;
			final ObservableList<MenuItem> items = filesMenu.getItems();
			items.get(0).setText(currentText);
			filesMenu.show(webView, e.getScreenX(), e.getScreenY());
			items.get(2).setVisible(!isSeason());
			return true;
		}
		return false;
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
			hovered.click();
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

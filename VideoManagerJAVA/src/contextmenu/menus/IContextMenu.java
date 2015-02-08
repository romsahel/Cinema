/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contextmenu.menus;

import contextmenu.CContextMenu;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.web.WebEngine;

/**
 *
 * @author Romsahel
 */
public abstract class IContextMenu
{

	private final ContextMenu menu = new ContextMenu();
	final ObservableList<MenuItem> items = menu.getItems();

	final CContextMenu parent;
	final WebEngine webEngine;

	public IContextMenu(CContextMenu parent, WebEngine webEngine)
	{
		this.parent = parent;
		this.webEngine = webEngine;
	}

	public void init()
	{
		MenuItem labelItem = new MenuItem("Label");
		labelItem.setDisable(true);
		items.add(labelItem);
	}

	public MenuItem addItem(String label)
	{
		return addItem(label, null);
	}

	public MenuItem addItem(String label, EventHandler<ActionEvent> onAction)
	{
		MenuItem item = new MenuItem(label);
		if (onAction != null)
			item.setOnAction(onAction);
		items.add(item);

		return item;
	}

	public abstract boolean invoke();

	/**
	 * @return the menu
	 */
	public ContextMenu getMenu()
	{
		return menu;
	}
}

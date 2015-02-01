/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contextmenu;

import com.sun.webkit.dom.HTMLElementImpl;
import contextmenu.menus.ContextFiles;
import contextmenu.menus.ContextLocations;
import contextmenu.menus.ContextMedias;
import contextmenu.menus.ContextSubtitles;
import contextmenu.menus.IContextMenu;
import java.util.ArrayList;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 *
 * @author Romsahel
 */
public class CContextMenu
{

	private static WebEngine webEngine;
	private static WebView webView;

	private static IContextMenu currentMenu;
	private static HTMLElementImpl hovered;
	private static final ArrayList<IContextMenu> menus = new ArrayList<>();

	public CContextMenu(WebEngine engine, WebView view)
	{
		webEngine = engine;
		webView = view;
		menus.add(new ContextFiles(this, webEngine));
		menus.add(new ContextMedias(this, webEngine));
		menus.add(new ContextLocations(this, webEngine));
		menus.add(new ContextSubtitles(this, webEngine));
	}

	public static void show(MouseEvent e)
	{
		hide();
		for (IContextMenu menu : menus)
			if (menu.invoke())
			{
				currentMenu.getMenu().show(webView, e.getScreenX(), e.getScreenY());
				break;
			}
	}

	public static void hide()
	{
		if (currentMenu != null)
		{
			currentMenu.getMenu().hide();
			if (currentMenu.getClass() == ContextLocations.class)
				hovered.setClassName("");
		}

		hovered = null;
		currentMenu = null;
	}

	/**
	 * @param aCurrentMenu the currentMenu to set
	 */
	public void setCurrentMenu(IContextMenu aCurrentMenu)
	{
		currentMenu = aCurrentMenu;
	}

	/**
	 * @return the hovered
	 */
	public HTMLElementImpl getHovered()
	{
		return hovered;
	}

	/**
	 * @param aHovered the hovered to set
	 */
	public void setHovered(HTMLElementImpl aHovered)
	{
		hovered = aHovered;
	}
}

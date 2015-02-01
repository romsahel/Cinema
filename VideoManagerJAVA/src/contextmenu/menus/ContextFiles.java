/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contextmenu.menus;

import com.sun.webkit.dom.HTMLDivElementImpl;
import com.sun.webkit.dom.HTMLElementImpl;
import com.sun.webkit.dom.HTMLLIElementImpl;
import contextmenu.CContextMenu;
import static contextmenu.CContextMenu.hide;
import javafx.event.ActionEvent;
import javafx.scene.web.WebEngine;
import org.w3c.dom.Element;

/**
 *
 * @author Romsahel
 */
public class ContextFiles extends IContextMenu
{
	private static boolean isSeason;

	public ContextFiles(CContextMenu parent, WebEngine webEngine)
	{
		super(parent, webEngine);
		init();
	}

	@Override
	public final void init()
	{
		super.init();

		final HTMLElementImpl hovered = parent.getHovered();

		// <editor-fold defaultstate="collapsed" desc="Toggle Seen">
		this.addItem("Toggle seen", (ActionEvent event) ->
			 {
				 if (isSeason)
					 webEngine.executeScript("toggleSeenSeason()");
				 else
					 toggle(hovered);
				 hide();
		});
		// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc="Toggle until there">
		this.addItem("Toggle seen until there", (ActionEvent event) ->
			 {
				 hide();
				 webEngine.executeScript("toggleEpisodesUntilThere()");
		});
		// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc="Toggle Season">
		this.addItem("Reset", (ActionEvent event) ->
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
		// </editor-fold>
	}

	private static boolean isSeason(HTMLElementImpl hovered)
	{
		final Element parentElement = hovered.getParentElement();
		final String id = parentElement.getAttribute("id");
		isSeason = (id != null && id.equals("seasons"));
		return isSeason;
	}

	private void toggle(HTMLElementImpl element)
	{
		final HTMLDivElementImpl div = (HTMLDivElementImpl) element.getElementsByTagName("div").item(0);
		final HTMLElementImpl item = (HTMLElementImpl) div.getElementsByTagName("span").item(0);
		item.click();
	}

	@Override
	public boolean invoke()
	{
		final Object object = webEngine.executeScript("$(\"#files li:hover\")[0]");
		if (object.getClass() == HTMLLIElementImpl.class)
		{
			HTMLLIElementImpl hovered = (HTMLLIElementImpl) object;
			parent.setHovered(hovered);
			hovered.click();

			String currentText = hovered.getInnerText();
			final int indexOf = currentText.indexOf("\n");
			currentText = currentText.substring(indexOf > 0 ? indexOf : 0).trim();

			items.get(2).setVisible(!isSeason(parent.getHovered()));

			parent.setCurrentMenu(this);
			items.get(0).setText(currentText);
			return true;
		}
		return false;
	}
}

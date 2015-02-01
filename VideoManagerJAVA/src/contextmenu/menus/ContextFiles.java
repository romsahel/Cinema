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
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.web.WebEngine;
import org.w3c.dom.Element;

/**
 *
 * @author Romsahel
 */
public class ContextFiles extends IContextMenu
{

	private static boolean isSeason;
	private HTMLLIElementImpl hovered;

	public ContextFiles(CContextMenu parent, WebEngine webEngine)
	{
		super(parent, webEngine);
		init();
	}

	@Override
	public final void init()
	{
		super.init();

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

		items.add(new SeparatorMenuItem());

		// <editor-fold defaultstate="collapsed" desc="Show in explorer">
		this.addItem("Show in Explorer", (ActionEvent event) ->
			 {
				 String path = (String) webEngine.executeScript("currentEpisode.value.path");
				 try
				 {
					 if (utils.Utils.isWindows)
						 Runtime.getRuntime().exec("explorer.exe /select," + path.replace("/", "\\"));
					 else
						 Desktop.getDesktop().open(new File(path).getParentFile());
				 } catch (IOException ex)
				 {
					 Logger.getLogger(ContextFiles.class.getName()).log(Level.SEVERE, null, ex);
				 }
				 hide();
		});
		// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc="Reset">
		this.addItem("Reset", (ActionEvent event) ->
			 {
				 if (isSeason)
					 webEngine.executeScript("toggleSeenSeason(true)");
				 else
					 webEngine.executeScript("toggleSeen(null, false, false, true)");
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
			hovered = (HTMLLIElementImpl) object;
			hovered.click();

			String currentText = hovered.getInnerText();
			final int indexOf = currentText.indexOf("\n");
			currentText = currentText.substring(indexOf > 0 ? indexOf : 0).trim();

			items.get(2).setVisible(!isSeason(hovered));

			parent.setCurrentMenu(this);
			items.get(0).setText(currentText);
			return true;
		}
		return false;
	}
}

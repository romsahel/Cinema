/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contextmenu.menus;

import com.sun.webkit.dom.HTMLDivElementImpl;
import contextmenu.CContextMenu;
import static contextmenu.CContextMenu.hide;
import editdialog.EditDialog;
import gnu.trove.map.hash.THashMap;
import javafx.event.ActionEvent;
import javafx.scene.web.WebEngine;
import videomanagerjava.Media;
import videomanagerjava.files.Database;

/**
 *
 * @author Romsahel
 */
public class ContextMedias extends IContextMenu
{

	public ContextMedias(CContextMenu parent, WebEngine webEngine)
	{
		super(parent, webEngine);
		init();
	}

	@Override
	public final void init()
	{
		super.init();

		final THashMap<Long, Media> database = Database.getInstance().getDatabase();
		// <editor-fold defaultstate="collapsed" desc="Edit">
		this.addItem("Edit", (ActionEvent event) ->
			 {
				 final String id = parent.getHovered().getParentElement().getAttribute("id");
				 hide();
				 final Media media = database.get(Long.parseLong(id, 10));
				 new EditDialog(media).show();
		});
		// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc="Remove">
		this.addItem("Remove", (ActionEvent event) ->
			 {
				 final String id = parent.getHovered().getParentElement().getAttribute("id");
				 hide();
				 database.put(Long.valueOf(id), null);
				 webEngine.executeScript("$('#" + id + "').fadeOut(200)");
				 Database.getInstance().writeDatabase();
		});
		// </editor-fold>
	}

	@Override
	public boolean invoke()
	{
		final Object object = webEngine.executeScript("$(\".poster:hover\")[0]");
		if (object.getClass() == HTMLDivElementImpl.class)
		{
			final HTMLDivElementImpl hovered = (HTMLDivElementImpl) object;
			parent.setHovered(hovered);
			hovered.click();
			String currentText = hovered.getNextElementSibling().getTextContent();
			parent.setCurrentMenu(this);
			getMenu().getItems().get(0).setText(currentText);
			return true;
		}
		return false;
	}

}

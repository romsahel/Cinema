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
import files.Database;
import files.Settings;
import gnu.trove.map.hash.THashMap;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.web.WebEngine;
import videomanagerjava.Media;

/**
 *
 * @author Romsahel
 */
public class ContextMedias extends IContextMenu
{

	private HTMLDivElementImpl hovered;

	public ContextMedias(CContextMenu parent, WebEngine webEngine)
	{
		super(parent, webEngine);
		init();
	}

	@Override
	public final void init()
	{
		super.init();

		final EditDialog editDialog = new EditDialog();
		final THashMap<Long, Media> database = Database.getInstance().getDatabase();
		// <editor-fold defaultstate="collapsed" desc="Edit">
		this.addItem("Edit", (ActionEvent event) ->
			 {
				 final String id = hovered.getParentElement().getAttribute("id");
				 hide();
				 final Media media = database.get(Long.parseLong(id, 10));
				 editDialog.show(media);
		});
		// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc="Remove">
		this.addItem("Remove", (ActionEvent event) ->
			 {
				 final String id = hovered.getParentElement().getAttribute("id");
				 hide();
				 Media deleted = database.put(Long.valueOf(id), null);
				 ArrayList<String> data = new ArrayList<>();
				 data.add(deleted.getInfo().get("name"));
				 data.add(deleted.getInfo().get("location"));

				 Settings.getInstance().getDeletedMedias().put(id, data);
				 webEngine.executeScript("$('#" + id + "').fadeOut(200)");
				 Database.getInstance().writeDatabase();
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
				 webEngine.executeScript("toggleSeenSeason(true, $(\"#episodes li > div > span\"))");
				 hide();
		});
		// </editor-fold>
	}

	@Override
	public boolean invoke()
	{
		final Object object = webEngine.executeScript("$(\".poster:hover\")[0]");
		if (object.getClass() == HTMLDivElementImpl.class)
		{
			hovered = (HTMLDivElementImpl) object;
			hovered.click();
			String currentText = hovered.getNextElementSibling().getTextContent();
			parent.setCurrentMenu(this);
			getMenu().getItems().get(0).setText(currentText);
			return true;
		}
		return false;
	}

}

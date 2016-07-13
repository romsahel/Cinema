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
import files.FileWalker;
import gnu.trove.map.hash.THashMap;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Map;
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

		final EditDialog editDialog = new EditDialog(main.Main.getStage());
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
				 Database.getInstance().removeMedia(false, database.get(Long.valueOf(id)));
				 utils.Utils.callFuncJS(webEngine, "deleteMedia", id);
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
					 Logger.getLogger(ContextFiles.class.getName()).log(Level.INFO, null, ex);
				 }
				 hide();
		});
		// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc="Refresh">
		this.addItem("Refresh", (ActionEvent event) ->
			 {
				 final String attribute = hovered.getParentElement().getAttribute("id");
				 final Long id = Long.valueOf(attribute);
				 hide();

				 Media media = database.get(id);
				 Media refreshed = FileWalker.getInstance().walkRoot(new File(java.net.URLDecoder.decode(media.getInfo().get("path"))), null);

				 if (refreshed != null)
				 {
					 final THashMap<String, String> infos = refreshed.getInfo();
					 for (Map.Entry<String, String> entrySet : media.getInfo().entrySet())
                                             if (!infos.containsKey(entrySet.getKey())
                                                 || infos.get(entrySet.getKey()) == null)
						 infos.put(entrySet.getKey(), entrySet.getValue());

					 database.put(id, refreshed);
					 utils.Utils.callFuncJS(webEngine, "updateMedia", attribute, refreshed.toJSArray());
				 }
				 else
				 {
					 Database.getInstance().getDatabase().remove(id);
					 utils.Utils.callFuncJS(webEngine, "deleteMedia", id.toString());
				 }
				 Database.getInstance().writeDatabase();
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

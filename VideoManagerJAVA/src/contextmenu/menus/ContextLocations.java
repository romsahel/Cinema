/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contextmenu.menus;

import com.sun.webkit.dom.HTMLLIElementImpl;
import contextmenu.CContextMenu;
import static contextmenu.CContextMenu.hide;
import files.Database;
import files.Settings;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.scene.control.TextInputDialog;
import javafx.scene.web.WebEngine;
import utils.Utils;
import videomanagerjava.Location;
import videomanagerjava.Media;

/**
 *
 * @author Romsahel
 */
public class ContextLocations extends IContextMenu
{

	private HTMLLIElementImpl hovered;

	public ContextLocations(CContextMenu parent, WebEngine webEngine)
	{
		super(parent, webEngine);
		init();
	}

	@Override
	public final void init()
	{
		super.init();

		// <editor-fold defaultstate="collapsed" desc="Remove">
		this.addItem("Remove", (ActionEvent event) ->
			 {
				 final String locationName = hovered.getInnerText();
				 final String toRemove = (String) webEngine.executeScript("removeLocation()");
				 Settings.getInstance().getLocations().remove(toRemove);

				 final THashMap<Long, Media> database = Database.getInstance().getDatabase();
				 final THashMap<Long, ArrayList<String>> merged = Database.getInstance().getMergedMedias();
				 final THashMap<Long, ArrayList<String>> removed = Database.getInstance().getDeletedMedias();

				 ArrayList<Media> deleted = new ArrayList<>();
				 for (Iterator<Map.Entry<Long, Media>> it = database.entrySet().iterator(); it.hasNext();)
				 {
					 Media media = it.next().getValue();
					 if (media != null && locationName.equals(media.getInfo().get("location")))
					 {
						 it.remove();
						 deleted.add(media);
					 }
				 }
				 undeleteMedia(locationName, merged, database);
				 undeleteMedia(locationName, removed, database);

				 StringBuilder builder = new StringBuilder();
				 builder.append("\\[");
				 for (Media m : deleted)
					 builder.append('"').append(m.getId()).append('"').append(',');
				 builder.append("], [ ]");
				 Utils.callFuncJS(webEngine, "mergeAndUpdate", builder.toString());

				 hide();
		});
		// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc="Rename">
		this.addItem("Rename", (ActionEvent event) ->
			 {
				 final String toRename = hovered.getInnerText();
				 final Location location = Settings.getInstance().getLocations().get(toRename);

				 TextInputDialog dialog = new TextInputDialog(toRename);
				 dialog.setHeaderText(null);
				 dialog.setTitle("Rename location");
				 dialog.setContentText("Name of the location: ");

				 Optional<String> result = dialog.showAndWait();
				 if (result.isPresent())
					 renameLocation(toRename, result, location);
		});
		// </editor-fold>
	}

	private void undeleteMedia(String location, THashMap<Long, ArrayList<String>> map, THashMap<Long, Media> database)
	{
		for (Iterator<Map.Entry<Long, ArrayList<String>>> it = map.entrySet().iterator(); it.hasNext();)
		{
			final Map.Entry<Long, ArrayList<String>> next = it.next();
			ArrayList<String> list = next.getValue();
			if (list.contains(location))
				it.remove();
		}
	}

	private void renameLocation(final String toRename, Optional<String> result, final Location location)
	{
		Settings.getInstance().getLocations().remove(toRename);
		Settings.getInstance().getLocations().put(result.get(), location);
		hovered.setTextContent(result.get());
		hide();

		final THashMap<Long, Media> database = Database.getInstance().getDatabase();
		for (Map.Entry<Long, Media> entrySet : database.entrySet())
		{
			Media value = entrySet.getValue();
			if (value != null && value.getInfo().get("location").equals(toRename))
				value.getInfo().put("location", result.get());
		}
		Database.getInstance().writeDatabase();
		Settings.getInstance().writeSettings();
	}

	@Override
	public boolean invoke()
	{
		final Object object = webEngine.executeScript("$(\"#locationsList li:hover\")[0]");
		if (object.getClass() == HTMLLIElementImpl.class)
		{
			hovered = (HTMLLIElementImpl) object;
			final String currentText = hovered.getInnerText();
			System.out.println(currentText);
			if (!currentText.equals("All"))
			{
				hovered.setClassName("hover");
				parent.setCurrentMenu(this);
				getMenu().getItems().get(0).setText(currentText);
				return true;
			}
		}
		return false;
	}

}

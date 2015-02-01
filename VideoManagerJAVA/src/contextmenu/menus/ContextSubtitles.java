/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contextmenu.menus;

import contextmenu.CContextMenu;
import contextmenu.ComboBoxMenuItem;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.web.WebEngine;
import files.Settings;

/**
 *
 * @author Romsahel
 */
public class ContextSubtitles extends IContextMenu
{

	public ContextSubtitles(CContextMenu parent, WebEngine webEngine)
	{
		super(parent, webEngine);
		init();
	}

	@Override
	public final void init()
	{
		MenuItem labelItem = new MenuItem("Subtitles languages:");
		labelItem.setDisable(true);
		items.add(labelItem);

		final ComboBox<String> combo = new ComboBox<>();
		final ObservableList<String> comboItems = combo.getItems();
		comboItems.add("English");
		comboItems.add("French");
		comboItems.add("German");
		comboItems.add("Italian");

		String language = Settings.getInstance().getGeneral().get("language");
		combo.getSelectionModel().select(language);

		combo.valueProperty().addListener((ObservableValue<? extends String> obs, String old, String newValue) ->
		{
			Settings.getInstance().getGeneral().put("language", newValue);
		});

		MenuItem languageItem = new ComboBoxMenuItem((combo));

		items.add(languageItem);
	}

	@Override
	public boolean invoke()
	{
		final String javascript = "$(\"#watch-buttons > div > div:hover\").length > 0";
		final Boolean isHovered = (Boolean) webEngine.executeScript(javascript);
		if (isHovered)
		{
			parent.setCurrentMenu(this);
			return true;
		}
		return false;
	}

}

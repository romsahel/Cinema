/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contextmenu;

import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Romsahel
 */
public class ComboBoxMenuItem extends CustomMenuItem
{

	final ComboBox comboBox;

	public ComboBoxMenuItem(ComboBox comboBox)
	{
		this.comboBox = comboBox;
		comboBox.setPrefWidth(Math.max(144, comboBox.getPrefWidth())); // 144px is approx the size of our TextFieldMenuItem
		comboBox.setMaxWidth(Double.MAX_VALUE);
		comboBox.setFocusTraversable(false);

		final BorderPane pane = new BorderPane();
		pane.setCenter(comboBox);
		pane.setPadding(new Insets(0, 2, 0, 2)); // setting padding in css doesn't work great; do it here
		pane.getStyleClass().add("container");
		pane.setMaxWidth(Double.MAX_VALUE);

		setContent(pane);
		setHideOnClick(false);
		getStyleClass().add("combobox-menu-item");
	}

	public ComboBox getComboBox()
	{
		return comboBox;
	}
}

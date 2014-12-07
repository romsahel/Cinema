/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package editdialog;

import java.util.TreeMap;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import videomanagerjava.Episode;
import videomanagerjava.Media;

/**
 *
 * @author Romsahel
 */
public final class EditableTreeCell extends TreeCell<String>
{

	private TextField textField;
	private final Media media;

	public EditableTreeCell(Media media)
	{
		MenuItem addMenuItem = new MenuItem("Rename");
		addMenuItem.setOnAction((ActionEvent event) ->
		{
			startEdit();
		});
		setContextMenu(new ContextMenu(addMenuItem));
		this.media = media;
	}

	@Override
	public void startEdit()
	{
		super.startEdit();

		if (textField == null)
			createTextField();
		else
			textField.setText((String) getItem());
		setText(null);
		setGraphic(textField);
		textField.requestFocus();
		textField.selectAll();
	}

	@Override
	public void cancelEdit()
	{
		super.cancelEdit();
		setText((String) getItem());
		if (getTreeItem() != null)
			setGraphic(getTreeItem().getGraphic());
	}

	@Override
	public void updateItem(String item, boolean empty)
	{
		super.updateItem(item, empty);

		if (empty)
		{
			setText(null);
			setGraphic(null);
		}
		else if (isEditing())
		{
			if (textField != null)
				textField.setText(getString());
			setText(null);
			setGraphic(textField);
		}
		else
		{
			setText(getString());
			setGraphic(getTreeItem().getGraphic());
		}
	}

	private void createTextField()
	{
		textField = new TextField(this.getString());
		textField.setOnKeyReleased((KeyEvent t) ->
		{
			final String string = this.getString();
			final String text = textField.getText();
			if (t.getCode() == KeyCode.ENTER && text.length() > 0)
			{
				final TreeItem<String> treeItem = this.getTreeItem();
				if (treeItem.isLeaf())
				{
					TreeMap<String, Episode> season = media.getSeasons().get(treeItem.getParent().getValue());
					final Episode get = season.get(string);
					season.put(text, get);
					get.setProperty("name", text);
					season.remove(string);
				}
				else
				{
					TreeMap<String, TreeMap<String, Episode>> seasons = media.getSeasons();
					seasons.put(text, seasons.get(string));
					seasons.remove(string);
				}
				commitEdit(text);
			}
			else if (t.getCode() == KeyCode.ESCAPE)
				cancelEdit();
		});

	}

	private String getString()
	{
		return getItem() == null ? "" : getItem();
	}
}

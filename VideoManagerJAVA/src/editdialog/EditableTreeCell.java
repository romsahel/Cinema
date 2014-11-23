/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package editdialog;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author Romsahel
 */
public final class EditableTreeCell extends TreeCell<String>
{

	private TextField textField;

	public EditableTreeCell()
	{
		MenuItem addMenuItem = new MenuItem("Rename");
		addMenuItem.setOnAction((ActionEvent event) ->
		{
			startEdit();
		});
		setContextMenu(new ContextMenu(addMenuItem));
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
		textField = new TextField(getString());
		textField.setOnKeyReleased((KeyEvent t) ->
		{
			if (t.getCode() == KeyCode.ENTER && textField.getText().length() > 0)
				commitEdit(textField.getText());
			else if (t.getCode() == KeyCode.ESCAPE)
				cancelEdit();
		});

	}

	private String getString()
	{
		return getItem() == null ? "" : getItem();
	}
}

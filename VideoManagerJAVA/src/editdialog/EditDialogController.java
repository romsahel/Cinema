/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package editdialog;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import videomanagerjava.Episode;
import videomanagerjava.Media;

/**
 * FXML Controller class
 *
 * @author Romsahel
 */
public class EditDialogController extends AnchorPane
{

	@FXML
	private TextField titleField;
	@FXML
	private TextField urlField;
	@FXML
	private ComboBox<String> typeCombo;
	@FXML
	private ImageView imageView;
	@FXML
	private TreeView<String> tree;
	@FXML
	private Label windowTitle;

	private final EditDialog parent;
	private final Stage stage;
	private final Delta dragDelta = new Delta();
	private final Media media;

	public EditDialogController(EditDialog parent, Media media)
	{
		load();

		final HashMap<String, String> info = media.getInfo();
		final String text = info.get("name");
		titleField.setText(text);
		windowTitle.setText(text);
		typeCombo.getSelectionModel().select(info.get("type").equals("show") ? 1 : 0);

		File f = new File("public_html\\media\\posters\\" + info.get("img"));
		imageView.setImage(new Image(f.toURI().toString()));

		initTree(media);

		this.parent = parent;
		this.stage = parent.getStage();
		this.media = media;
	}

	private void initTree(Media media)
	{
		tree.setShowRoot(false);
		tree.setEditable(true);
		tree.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>()
		{
			@Override
			public TreeCell<String> call(TreeView<String> p)
			{
				return new EditableTreeCell();
			}
		});
		TreeItem<String> root = new TreeItem<>("Root Node");
		final TreeMap<String, TreeMap<String, Episode>> seasons = media.getSeasons();
		final boolean shouldExpand = seasons.size() == 1;
		for (Map.Entry<String, TreeMap<String, Episode>> seasonsSet : seasons.entrySet())
		{
			TreeItem<String> season = new TreeItem<>(seasonsSet.getKey());
			season.setExpanded(shouldExpand);

			for (Map.Entry<String, Episode> episodesSet : seasonsSet.getValue().entrySet())
				season.getChildren().add(new TreeItem<>(episodesSet.getKey()));

			root.getChildren().add(season);
		}
		tree.setRoot(root);
	}

	private void load() throws RuntimeException
	{
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("EditDialogFXML.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try
		{
			fxmlLoader.load();
		} catch (IOException exception)
		{
			throw new RuntimeException(exception);
		}
	}

	@FXML
	protected void onOK()
	{
		boolean changed = false;
		final HashMap<String, String> info = media.getInfo();

		final String title = titleField.getText().trim();
		if (!info.get("name").equals(title))
		{
			info.put("name", title);
			changed = true;
		}

		final String type = typeCombo.getSelectionModel().getSelectedItem().toLowerCase();
		if (!info.get("type").equals(type))
		{
			info.put("name", type);
			changed = true;
		}
		if (changed)
			utils.Utils.callFuncJS(videomanagerjava.CWebEngine.getWebEngine(),
								   "updateMedia", Long.toString(media.getId()), media.toJSArray());

		parent.hide();
	}

	@FXML
	protected void onCancel()
	{
		parent.hide();
	}

	@FXML
	protected void onRefresh()
	{
		System.out.println("The button was clicked!");
	}

	@FXML
	protected void onDrag(MouseEvent mouseEvent)
	{
		stage.setX(stage.getX() + (mouseEvent.getScreenX() - dragDelta.x));
		stage.setY(stage.getY() + (mouseEvent.getScreenY() - dragDelta.y));
		onDragStarted(mouseEvent);
	}

	@FXML
	protected void onDragStarted(MouseEvent mouseEvent)
	{
		dragDelta.x = mouseEvent.getScreenX();
		dragDelta.y = mouseEvent.getScreenY();
	}

	@FXML
	protected void onKeyPressed(KeyEvent keyEvent)
	{
		if (keyEvent.getCode() == KeyCode.ESCAPE)
			parent.hide();
	}

	class Delta
	{

		double x, y;
	}

}

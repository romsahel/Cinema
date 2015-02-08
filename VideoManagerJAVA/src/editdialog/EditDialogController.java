/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package editdialog;

import files.Database;
import files.Downloader;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import videomanagerjava.CWebEngine;
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
	private Media media;
	private String newImg;
	private TreeMap<String, TreeMap<String, Episode>> newSeasons;

	public EditDialogController(EditDialog parent)
	{
		load();

		this.parent = parent;
		this.stage = parent.getStage();
	}

	public void init(Media media1)
	{
		final THashMap<String, String> info = media1.getInfo();
		final String text = info.get("name");
		titleField.setText(text);
		windowTitle.setText(text);
		boolean isShow = (info.get("type") != null && info.get("type").equals("show"));
		typeCombo.getSelectionModel().select(isShow ? 1 : 0);
		final String img = info.get("img");
		File f = new File(Downloader.POSTER_PATH + ((img == null) ? "unknown.jpg" : img));
		imageView.setImage(new Image(f.toURI().toString()));
		this.newSeasons = new TreeMap<>();
		initTree(media1);
		this.media = media1;
	}

	@SuppressWarnings("unchecked")
	private void initTree(Media media)
	{
		tree.setShowRoot(false);
		tree.setEditable(true);

		TreeItem<String> root = new TreeItem<>("Root Node");

		final TreeMap<String, TreeMap<String, Episode>> seasons = media.getSeasons();
		final boolean shouldExpand = seasons.size() == 1;

		for (Map.Entry<String, TreeMap<String, Episode>> seasonsSet : seasons.entrySet())
		{
			final String key = seasonsSet.getKey();
			final TreeMap<String, Episode> value = seasonsSet.getValue();

			newSeasons.put(key, (TreeMap<String, Episode>) value.clone());

			TreeItem<String> season = new TreeItem<>(key);
			season.setExpanded(shouldExpand);

			for (Map.Entry<String, Episode> episodesSet : value.entrySet())
				season.getChildren().add(new TreeItem<>(episodesSet.getKey()));

			root.getChildren().add(season);
		}

		tree.setCellFactory((TreeView<String> p) -> new EditableTreeCell(newSeasons));
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
	protected void onOKandForce()
	{
		onOK(true);
	}

	@FXML
	protected void onOK()
	{
		onOK(false);
	}

	private void onOK(boolean force)
	{
		final THashMap<String, String> info = media.getInfo();

		final String title = titleField.getText().trim();
		final String type = typeCombo.getSelectionModel().getSelectedItem().toLowerCase();

		boolean shouldUpdate;
		if ((shouldUpdate = !newSeasons.equals(media.getSeasons())))
			media.setSeasons(newSeasons);

		if (!info.get("name").equals(title) || !info.get("type").equals(type) || newImg != null)
		{
			media.setInfo("type", type);
			media.setInfo("name", title);
			media.setInfo("img", newImg);
			shouldUpdate = true;
		}

		if (force)
		{
			utils.Utils.callFuncJS(CWebEngine.getWebEngine(), "setMediaLoading", Long.toString(media.getId()));
			parent.hide();
			Thread t = new Thread(() ->
			{
				media.downloadInfos(true);
				CWebEngine.mergeByPoster();
				utils.Utils.callFuncJS(videomanagerjava.CWebEngine.getWebEngine(),
									   "updateMedia", Long.toString(media.getId()), media.toJSArray());
				Database.getInstance().writeDatabase();
			});
			t.start();
			return;
		}

		if (shouldUpdate && !force)
			utils.Utils.callFuncJS(videomanagerjava.CWebEngine.getWebEngine(),
								   "updateMedia", Long.toString(media.getId()), media.toJSArray());

		Database.getInstance().writeDatabase();
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
		final String field = urlField.getText();
		if (field.contains("imdb.com") || field.startsWith("tt"))
		{
			String id = field.replace("http://", "");
			id = id.replace("www.imdb.com/title/", "");
			final int index = id.indexOf("/");
			if (index > 0)
				id = id.substring(0, index);
			newImg = media.downloadInfos("http://www.omdbapi.com/?i=" + id + "&plot=full&r=json");
			if (newImg != null)
				onOK(false);
		}
		else
		{
			newImg = Downloader.downloadImage(field);
			if (newImg != null)
				imageView.setImage(new Image(new File(Downloader.POSTER_PATH + newImg).toURI().toString()));
		}
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
		if (keyEvent.getCode() == KeyCode.ENTER)
			onOK(keyEvent.isShiftDown());
	}

	class Delta
	{

		double x, y;
	}

}

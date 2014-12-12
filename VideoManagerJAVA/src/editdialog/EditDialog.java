/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package editdialog;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import videomanagerjava.Media;

/**
 *
 * @author Romsahel
 */
public class EditDialog
{

	private final Stage stage;
	private final Media media;

	public EditDialog(Media media)
	{
		this.media = media;
		stage = new Stage(StageStyle.UNDECORATED);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setResizable(false);
	}

	public void show()
	{
		final Stage parentStage = main.Main.getStage();

		final EditDialogController fxml;
		fxml = new EditDialogController(this, media);

		Scene scene = new Scene(fxml);
		stage.setScene(scene);
		stage.show();
		stage.setX(parentStage.getX() + parentStage.getWidth() / 2 - stage.getWidth() / 2);
		stage.setY(parentStage.getY() + parentStage.getHeight() / 2 - stage.getHeight() / 2);
	}

	public void hide()
	{
		Platform.runLater(new Runnable()
		{

			@Override
			public void run()
			{
				stage.hide();
			}
		});
	}

	/**
	 * @return the stage
	 */
	public Stage getStage()
	{
		return stage;
	}
}

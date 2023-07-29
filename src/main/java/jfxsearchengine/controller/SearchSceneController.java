package jfxsearchengine.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import jfxsearchengine.Scenes;

public class SearchSceneController {

	@FXML private TextField searchTxtBox;
	@FXML private Button searchBtn;
	@FXML private Button indexMangeBtn;
	@FXML private VBox searchResArea;
	@FXML private Label notifLbl;
	
	public void doSearch() {//called when pressing search button or pressing enter in textbox
		if (searchTxtBox.getText().isBlank()) return; //search txt is empty, do nothing
		//TODO: query db based on search text
	}
	
	public void gotoManageScene() {
		SceneManager.getInstance().changeScene(Scenes.MANAGE);
	}
	
	public void notifyIncorrectPassword() {
		notifLbl.setTextFill(Color.RED);
		notifLbl.setText("Incorrect password. Try again.");
	}
	
	public void clearIncorrectPassword() {
		notifLbl.setText("");
	}
}

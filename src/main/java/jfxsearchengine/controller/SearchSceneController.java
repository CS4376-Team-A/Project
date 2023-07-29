package jfxsearchengine.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import jfxsearchengine.Scenes;
import jfxsearchengine.controls.SearchResultControl;
import jfxsearchengine.db.DbManager;
import jfxsearchengine.db.Index;

public class SearchSceneController {

	@FXML private TextField searchTxtBox;
	@FXML private Button searchBtn;
	@FXML private Button indexMangeBtn;
	@FXML private VBox searchResArea;
	@FXML private Label notifLbl;
	
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
	
	public void doSearch() {
		if (searchTxtBox.getText().isBlank()) return;
		ObservableList<Index> res = DbManager.getInstance().findByKeywords(searchTxtBox.getText().split(" "));
		res.forEach(idx -> {
			System.out.println(idx);
			searchResArea.getChildren().add(new SearchResultControl(idx.getTitle(), idx.getUrl(), idx.getDescription()));
		});
	}
}

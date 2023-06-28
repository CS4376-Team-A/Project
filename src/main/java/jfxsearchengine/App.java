package jfxsearchengine;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jfxsearchengine.controller.SceneManager;

public class App extends Application{
    public static void main(String[] args) {
        launch(args);
    }
	
	@Override
    public void start(Stage stage) throws Exception {
		SceneManager sm = SceneManager.initalize(stage);
		sm.addFXML("Search", this.getClass().getResource("SearchScene.fxml"));
		sm.addFXML("Manage", this.getClass().getResource("ManageScene.fxml"));
		sm.changeScene("Search");
        stage.setTitle("CyberMiner");
        stage.show();
    }
}

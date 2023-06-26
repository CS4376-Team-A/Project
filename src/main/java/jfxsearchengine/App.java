package jfxsearchengine;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application{

    public static void main(String[] args) {
        launch(args);
    }
	
	@Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(this.getClass().getResource("App.fxml"));
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(this.getClass().getResource("styles.css").toExternalForm());
        
        stage.setTitle("Cyber Miner");
        stage.setScene(scene);
        stage.show();
    }
}

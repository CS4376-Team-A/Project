package jfxsearchengine;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application{
	public static final boolean DEBUG = true;
	
    public static void main(String[] args) {
        launch(args);
    }
	
	@Override
    public void start(Stage stage) throws Exception {
		SceneManager sm = SceneManager.initalize(stage);
		sm.addFXML(Scenes.SEARCH, this.getClass().getResource(Scenes.SEARCH.getUrl()));
		sm.addFXML(Scenes.MANAGE, this.getClass().getResource(Scenes.MANAGE.getUrl()));
		sm.changeScene(Scenes.SEARCH);
        stage.setTitle("CyberMiner");
        stage.show();
    }
}

/* TODO:
 * - URL verification
 * - autofill as typing
 * - try auto fill keywords in db
 */
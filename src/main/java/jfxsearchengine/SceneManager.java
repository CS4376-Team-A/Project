package jfxsearchengine;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxsearchengine.controller.SearchSceneController;

public class SceneManager {
	
	private static SceneManager inst; //singleton instance
	private final Stage stage;
	private final HashMap<Scenes, Scene> scenes;
	private final HashMap<Scenes, Object> controllers;
	private boolean loggedIn = false;
	
	public static SceneManager initalize(Stage stage) {
		if (inst == null) {
			synchronized (SceneManager.class) { // ensure thread safety
				if (inst == null) {
					inst = new SceneManager(stage);
				}
			}
		}
		return inst;
	}
	
	public static SceneManager getInstance() {
		return inst;
	}
	
	private SceneManager(Stage stage) {
		this.stage = stage;
		scenes = new HashMap<Scenes, Scene>();
		controllers = new HashMap<Scenes, Object>();
	}
	
	public boolean addFXML(Scenes scene, URL url) {
		try {
			FXMLLoader loader = new FXMLLoader(url);
			scenes.put(scene, new Scene(loader.load()));
			controllers.put(scene, loader.getController());
		} catch (IOException e) {
			System.err.println("Failed to load FXML "+scene.getName());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void changeScene(Scenes scene) {
		if (scene.equals(Scenes.MANAGE) && !loggedIn) {
			TextInputDialog diag = new TextInputDialog();
			diag.setTitle("Admin Login");
			diag.setHeaderText("Enter admin password:");
			diag.setGraphic(null);
			diag.initModality(Modality.APPLICATION_MODAL);
			diag.showAndWait().ifPresent(pw -> {
				if (pw.equals("admin")) {
					loggedIn = true;
				} else {
					((SearchSceneController)controllers.get(Scenes.SEARCH)).notifyIncorrectPassword();
				}
			});;
			
		}
		if (loggedIn || !scene.equals(Scenes.MANAGE)) {
			((SearchSceneController)controllers.get(Scenes.SEARCH)).clearIncorrectPassword();
			stage.setScene(scenes.get(scene));
		}
	}
	
	public void logOut() {
		loggedIn = false;
	}
	
}

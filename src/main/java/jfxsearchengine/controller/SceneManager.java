package jfxsearchengine.controller;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
	
	private static SceneManager inst; //singleton instance
	private final Stage stage;
	private final HashMap<String, Scene> scenes;
	
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
		scenes = new HashMap<String, Scene>();
	}
	
	public boolean addFXML(String name, URL url) {
		try {
			scenes.put(name, new Scene(FXMLLoader.load(url)));
		} catch (IOException e) {
			System.err.println("Failed to load FXML "+name);
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void changeScene(String name) {
		stage.setScene(scenes.get(name));
	}
}

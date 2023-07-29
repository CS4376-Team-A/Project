package jfxsearchengine;

public enum Scenes {

	SEARCH("Search","SearchScene.fxml"),
	MANAGE("Manage", "ManageScene.fxml");

	private final String name;
	private final String url;
	
	Scenes(String name, String url) {
		this.name = name;
		this.url = url;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUrl() {
		return url;
	}
	
}

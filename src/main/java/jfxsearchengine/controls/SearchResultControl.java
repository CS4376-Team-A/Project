package jfxsearchengine.controls;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SearchResultControl extends VBox {
	private Label titleLbl;
	private Label urlLbl;
	private Label descLbl;
	
	public SearchResultControl(String title, String url, String desc) {
		titleLbl = new Label(title);
		urlLbl = new Label(url);
		descLbl = new Label(desc);
	}
	
	public Label getTitleLbl() {
		return titleLbl;
	}

	public Label getUrlLbl() {
		return urlLbl;
	}

	public Label getDescLbl() {
		return descLbl;
	}
}

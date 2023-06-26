package jfxsearchengine;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class FXMLController {
    
	@FXML
    private Label label;

    public void initialize() {
        label.setText("fxml controller init");
    }
}

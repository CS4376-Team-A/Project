package jfxsearchengine.controller;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import jfxsearchengine.SceneManager;
import jfxsearchengine.Scenes;
import jfxsearchengine.db.DbManager;
import jfxsearchengine.db.Index;

public class SearchSceneController implements Initializable{

	@FXML private TextField searchTxtBox;
	@FXML private Button searchBtn;
	@FXML private Button indexMangeBtn;
	@FXML private Label notifLbl;
	@FXML private TableView<Index> searchTable;
	@FXML private TableColumn<Index, String> titleCol;
	@FXML private TableColumn<Index, String> urlCol;
	@FXML private TableColumn<Index, String> descCol;
	@FXML private TableColumn<Index, String[]> keyCol;
	@FXML private ComboBox<String> modeComboBx;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		searchTable.setEditable(false);
		titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
		urlCol.setCellValueFactory(new PropertyValueFactory<>("url"));
		descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
		keyCol.setCellValueFactory(new PropertyValueFactory<>("keywords"));
		
		titleCol.setCellFactory(TextFieldTableCell.forTableColumn());
		urlCol.setCellFactory(cell -> new HyperlinkTableCell());
		descCol.setCellFactory(TextFieldTableCell.forTableColumn());
		keyCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<String[]>() {
			@Override
			public String toString(String[] arr) {
				return Arrays.stream(arr).collect(Collectors.joining(", "));
			}

			@Override
			public String[] fromString(String string) {
				return string.replaceAll(",", " ").split(" +");
			}
		}));
		keyCol.setComparator(new Comparator<String[]>() {
			@Override
			public int compare(String[] keywords1, String[] keywords2) {
				String keywordsString1 = String.join(", ", keywords1);
		        String keywordsString2 = String.join(", ", keywords2);
		        return keywordsString1.compareTo(keywordsString2);
			}
		});
		
		modeComboBx.getItems().addAll("OR","AND","NOT");
		modeComboBx.setValue("OR");
		
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
	
	public void doSearch() {
		/*if (true) { //DO NOT UNCOMMENT THIS LOL
			DbManager.getInstance().tryAutoFillKeywords();
			System.out.println("DONE");
			return;
		}*/
		if (searchTxtBox.getText().isBlank()) return;
		ObservableList<Index> res = switch (modeComboBx.getValue())	{
			case "AND" -> DbManager.getInstance().findByAllKeywords(searchTxtBox.getText().replaceAll(",", " ").split(" +"));
			case "NOT" -> DbManager.getInstance().findByNotKeywords(searchTxtBox.getText().replaceAll(",", " ").split(" +"));
			default -> DbManager.getInstance().findByKeywords(searchTxtBox.getText().replaceAll(",", " ").split(" +"));
		};
		searchTable.setItems(res);
		searchTable.refresh();
	}
	
	public static class HyperlinkTableCell extends TableCell<Index, String> {
		private Hyperlink link;
		
		public HyperlinkTableCell() {
			link = new Hyperlink();
			link.setOnAction(e -> {
				String url = getItem();
				if (url != null && !url.isEmpty()) {
					try {
			            Desktop.getDesktop().browse(new URI(url));
			        } catch (Exception ex) {
			            ex.printStackTrace();
			        }
	            }
			});
		}
		
	    @Override
	    protected void updateItem(String item, boolean empty) {
	        super.updateItem(item, empty);
	        if (empty || item == null) {
	            setGraphic(null);
	        } else {
	            link.setText(item);
	            setGraphic(link);
	        }
	    }
	}
}

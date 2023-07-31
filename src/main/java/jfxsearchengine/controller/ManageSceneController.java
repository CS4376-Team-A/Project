package jfxsearchengine.controller;

import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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

public class ManageSceneController implements Initializable {
	
	private static final String keywordsRegex = "(^$)|(^([A-Za-z0-9]+)(,? ?\\s*[A-Za-z0-9]+)*$)";
	
	@FXML private Button searchBtn;
	@FXML private TableView<Index> indexTable;
	@FXML private TableColumn<Index, Integer> idCol;
	@FXML private TableColumn<Index, String> titleCol;
	@FXML private TableColumn<Index, String> urlCol;
	@FXML private TableColumn<Index, String> descCol;
	@FXML private TableColumn<Index, String[]> keyCol;
	@FXML private TableColumn<Index, String> dateCol;
	@FXML private TableColumn<Index, Index> delCol;
	@FXML private Button refreshBtn;
	@FXML private TextField titleTxtBx;
	@FXML private TextField urlTxtBx;
	@FXML private TextField descTxtBx;
	@FXML private TextField keyTxtBx;
	@FXML private Button addBtn;
	@FXML private Label notifLbl;
	
	public void gotoSearchScene() {
		SceneManager.getInstance().logOut();
		SceneManager.getInstance().changeScene(Scenes.SEARCH);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		idCol.setCellValueFactory(new PropertyValueFactory<Index, Integer>("id"));
		titleCol.setCellValueFactory(new PropertyValueFactory<Index, String>("title"));
		urlCol.setCellValueFactory(new PropertyValueFactory<Index, String>("url"));
		descCol.setCellValueFactory(new PropertyValueFactory<Index, String>("description"));
		keyCol.setCellValueFactory(new PropertyValueFactory<Index, String[]>("keywords"));
		dateCol.setCellValueFactory(new PropertyValueFactory<Index, String>("dateCreated"));
		indexTable.setEditable(true);
		
		idCol.setEditable(false);
		dateCol.setEditable(false);
		titleCol.setCellFactory(TextFieldTableCell.forTableColumn());
		titleCol.setOnEditCommit(this::titleCommitEdit);
		urlCol.setCellFactory(TextFieldTableCell.forTableColumn());
		urlCol.setOnEditCommit(this::urlCommitEdit);
		descCol.setCellFactory(TextFieldTableCell.forTableColumn());
		descCol.setOnEditCommit(this::descCommitEdit);
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
		keyCol.setOnEditCommit(this::keyCommitEdit);
		keyCol.setComparator(new Comparator<String[]>() {
			@Override
			public int compare(String[] keywords1, String[] keywords2) {
				String keywordsString1 = String.join(", ", keywords1);
		        String keywordsString2 = String.join(", ", keywords2);
		        return keywordsString1.compareTo(keywordsString2);
			}
		});
		delCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue()));
		delCol.setCellFactory(p -> new TableCell<Index, Index>() {
			private final Button delButton = new Button("Delete");

			@Override
			protected void updateItem(Index index, boolean empty) {
				super.updateItem(index, empty);
				if (index == null) {
					setGraphic(null);
					return;
				}
				setGraphic(delButton);
				delButton.setOnAction(e -> {
					DbManager.getInstance().deleteIndex(index.getId());
					getTableView().getItems().remove(index);
				});
			}
		});
		
		indexTable.setItems(DbManager.getInstance().getAllIndexes());
	}
	
	private void titleCommitEdit(TableColumn.CellEditEvent<Index, String> e) {
		DbManager.getInstance().updateIndexTitle(e.getTableView().getItems().get(e.getTablePosition().getRow()).getId(), e.getNewValue());
		e.getTableView().getItems().get(e.getTablePosition().getRow()).setTitle(e.getNewValue());
	}
	
	private void urlCommitEdit(TableColumn.CellEditEvent<Index, String> e) {
		DbManager.getInstance().updateIndexUrl(e.getTableView().getItems().get(e.getTablePosition().getRow()).getId(), e.getNewValue());
		e.getTableView().getItems().get(e.getTablePosition().getRow()).setUrl(e.getNewValue());
	}
	
	private void descCommitEdit(TableColumn.CellEditEvent<Index, String> e) {
		DbManager.getInstance().updateIndexDescription(e.getTableView().getItems().get(e.getTablePosition().getRow()).getId(), e.getNewValue());
		e.getTableView().getItems().get(e.getTablePosition().getRow()).setDescription(e.getNewValue());
	}
	
	private void keyCommitEdit(TableColumn.CellEditEvent<Index, String[]> e) {
		if (!Arrays.stream(e.getNewValue()).collect(Collectors.joining(", ")).matches(keywordsRegex)) {
			notifyError(true, "Invalid keywords format");
			e.consume();
			indexTable.refresh();
			return;
		}
		DbManager.getInstance().updateIndexKeywords(e.getTableView().getItems().get(e.getTablePosition().getRow()).getId(), e.getNewValue());
		e.getTableView().getItems().get(e.getTablePosition().getRow()).setKeywords(e.getNewValue());
	}
	
	public void refreshTable() {
		indexTable.setItems(DbManager.getInstance().getAllIndexes());
	}
	
	public void addIndex() {
		if (urlTxtBx.getText().isBlank() || titleTxtBx.getText().isBlank() ||  descTxtBx.getText().isBlank() ||  keyTxtBx.getText().isBlank()) return;
		if (!keyTxtBx.getText().matches(keywordsRegex)) {
			notifyError(true, "Invalid keywords format");
			return;
		}
		if (!urlTxtBx.getText().matches("^https?:\\/\\/([^\\-\\.][a-zA-Z0-9\\-]+[^\\-]\\.)+[A-Za-z]+(\\/[a-zA-Z0-9_\\-.!~*'|(),;:@&=+$%]*)*$")) {
			notifyError(true, "Invalid url format");
			return;
		}
		DbManager.getInstance().saveIndex(new Index(urlTxtBx.getText(), titleTxtBx.getText(), descTxtBx.getText(), keyTxtBx.getText().replaceAll(",", " ").split(" +")));
		urlTxtBx.setText("");
		titleTxtBx.setText("");
		descTxtBx.setText("");
		keyTxtBx.setText("");
		notifyError(false, null);
		refreshTable();
	}
	
	public void notifyError(boolean show, @Nullable String msg) {
		if (show) {
			notifLbl.setTextFill(Color.RED);
			notifLbl.setText(msg);
			return;
		}
		notifLbl.setText("");
	}
}

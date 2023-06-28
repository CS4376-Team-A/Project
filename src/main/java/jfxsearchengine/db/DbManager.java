package jfxsearchengine.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DbManager {

	private static final String URL = "jdbc:mysql://server.sarcly.xyz:3306/searchengine";
	private static final String USER = "cyberminer";
	private static final String PASS = "cyberminer";
	private static DbManager inst; // singleton instance
	private Connection con;

	private DbManager() {
		try {
			con = DriverManager.getConnection(URL, USER, PASS);
		} catch (SQLException e) {
			System.err.println("SQL connection failed!");
			e.printStackTrace();
		}
	}

	public static DbManager getInstance() { //
		if (inst == null) {
			synchronized (DbManager.class) { // ensure thread safety
				if (inst == null) {
					inst = new DbManager();
				}
			}
		}
		return inst;
	}

	public boolean saveIndex(Index index) {
		try {
			Statement sql = con.createStatement();
			sql.executeUpdate("INSERT INTO indexes (url, title, description, keywords) VALUES "+index.toString());
		} catch (SQLException e) {
			System.err.println("Failed to insert into DB");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean doesUrlExist(String url) {
		try {
			Statement sql = con.createStatement();
			ResultSet rs = sql.executeQuery("SELECT COUNT(*) FROM indexes WHERE url = \'"+url+"\'");
			return rs.next();
		} catch (SQLException e) {
			System.err.println("Failed to select from DB");
			e.printStackTrace();
			return false;
		}
	}
	
	public ObservableList<Index> getAllIndexes() {
		ObservableList<Index> out = FXCollections.observableArrayList();
		try {
			Statement sql = con.createStatement();
			ResultSet rs = sql.executeQuery("SELECT * FROM indexes");
			while (rs.next()) {
				out.add(new Index(rs.getInt("id"), rs.getString("url"), 
						rs.getString("title"), rs.getString("description"), 
						rs.getString("keywords"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("created_at").toLocalDateTime())));
			}
			return out;
		} catch (SQLException e) {
			System.err.println("Failed to select from DB");
			e.printStackTrace();
			return null;
		}
	}

	public void updateIndexTitle(int id, String title) {
		try {
			Statement sql = con.createStatement();
			sql.executeUpdate("UPDATE indexes SET title = \'"+title+"\' WHERE id = "+id);
		} catch (SQLException e) {
			System.err.println("Failed to update title of ID "+id);
			e.printStackTrace();
		}
	}
	
	public void updateIndexUrl(int id, String url) {
		try {
			Statement sql = con.createStatement();
			sql.executeUpdate("UPDATE indexes SET url = \'"+url+"\' WHERE id = "+id);
		} catch (SQLException e) {
			System.err.println("Failed to update url of ID "+id);
			e.printStackTrace();
		}
	}
	
	public void updateIndexDescription(int id, String desc) {
		try {
			Statement sql = con.createStatement();
			sql.executeUpdate("UPDATE indexes SET description = \'"+desc+"\' WHERE id = "+id);
		} catch (SQLException e) {
			System.err.println("Failed to update description of ID "+id);
			e.printStackTrace();
		}
	}
	
	public void updateIndexKeywords(int id, String keywords) {
		try {
			Statement sql = con.createStatement();
			sql.executeUpdate("UPDATE indexes SET keywords = \'"+keywords+"\' WHERE id = "+id);
		} catch (SQLException e) {
			System.err.println("Failed to update keywords of ID "+id);
			e.printStackTrace();
		}
	}
	
	public void deleteIndex(int id) {
		try {
			Statement sql = con.createStatement();
			sql.executeUpdate("DELETE FROM indexes WHERE id = "+id);
		} catch (SQLException e) {
			System.err.println("Failed to delete index of ID "+id);
			e.printStackTrace();
		}
	}
	
}

package jfxsearchengine.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

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

	public static DbManager getInstance() {
		if (inst == null) {
			synchronized (DbManager.class) { // ensure thread safety
				if (inst == null) {
					inst = new DbManager();
				}
			}
		}
		return inst;
	}

	public ObservableList<Index> findByKeywords(String[] keywords) {
		ObservableList<Index> out = FXCollections.observableArrayList();
		try {
			PreparedStatement sql = con.prepareStatement("SELECT * FROM keywords WHERE keyword IN "+Arrays.stream(keywords).collect(Collectors.joining("\',\'", "(\'", "\')")));
			ResultSet rs = sql.executeQuery();
			while (rs.next()) {
				Statement sql2 = con.createStatement();
				ResultSet rs2 = sql2.executeQuery("SELECT i.id, i.url, i.title, i.description, GROUP_CONCAT(k.keyword) AS keywords FROM indexes i LEFT JOIN keywords k ON i.id = k.id WHERE i.id = "+rs.getInt("id")+" GROUP BY i.id");
				while (rs2.next()) {
					String k = rs2.getString("keywords");
					int id = rs2.getInt("id");
					if (!out.contains(Index.ofId(id))) {
						out.add(new Index(id, rs2.getString("url"), rs2.getString("title"), rs2.getString("description"), k == null || k.isEmpty() ? new String[0] : k.split(", *"), null));
					}
				}
			}
		}catch (SQLException e) {
			System.err.println("Failed to select from DB");
			e.printStackTrace();
			return null;
		}
		return out;
	}
	
	public ObservableList<Index> findByAllKeywords(String[] keywords) {
		ObservableList<Index> out = FXCollections.observableArrayList();
		try {
			Statement sql = con.createStatement();
			ResultSet rs = sql.executeQuery("SELECT id FROM keywords WHERE keyword IN "+Arrays.stream(keywords).collect(Collectors.joining("\',\'", "(\'", "\')"))+" GROUP BY id HAVING COUNT(DISTINCT keyword) = "+keywords.length);
			while (rs.next()) {
				Statement sql2 = con.createStatement();
				ResultSet rs2 = sql2.executeQuery("SELECT i.id, i.url, i.title, i.description, GROUP_CONCAT(k.keyword) AS keywords FROM indexes i LEFT JOIN keywords k ON i.id = k.id WHERE i.id = "+rs.getInt("id")+" GROUP BY i.id");
				while (rs2.next()) {
					String k = rs2.getString("keywords");
					int id = rs2.getInt("id");
					if (!out.contains(Index.ofId(id))) {
						out.add(new Index(id, rs2.getString("url"), rs2.getString("title"), rs2.getString("description"), k == null || k.isEmpty() ? new String[0] : k.split(", *"), null));
					}
				}
			}
		}catch (SQLException e) {
			System.err.println("Failed to select from DB");
			e.printStackTrace();
			return null;
		}
		return out;
	}
	
	public ObservableList<Index> findByNotKeywords(String[] keywords) {
		ObservableList<Index> out = FXCollections.observableArrayList();
		try {
			Statement sql = con.createStatement();	
			ResultSet rs = sql.executeQuery("SELECT id FROM keywords WHERE id NOT IN (SELECT id FROM keywords WHERE keyword IN "+Arrays.stream(keywords).collect(Collectors.joining("\',\'", "(\'", "\')"))+") GROUP BY id");
			while (rs.next()) {
				Statement sql2 = con.createStatement();
				ResultSet rs2 = sql2.executeQuery("SELECT i.id, i.url, i.title, i.description, GROUP_CONCAT(k.keyword) AS keywords FROM indexes i LEFT JOIN keywords k ON i.id = k.id WHERE i.id = "+rs.getInt("id")+" GROUP BY i.id");
				while (rs2.next()) {
					String k = rs2.getString("keywords");
					int id = rs2.getInt("id");
					if (!out.contains(Index.ofId(id))) {
						out.add(new Index(id, rs2.getString("url"), rs2.getString("title"), rs2.getString("description"), k == null || k.isEmpty() ? new String[0] : k.split(", *"), null));
					}
				}
			}
		}catch (SQLException e) {
			System.err.println("Failed to select from DB");
			e.printStackTrace();
			return null;
		}
		return out;
	}
	
	public boolean saveIndex(Index index) {
		try {
			PreparedStatement sql = con.prepareStatement("INSERT INTO indexes (url, title, description) VALUES " + index.toString(), Statement.RETURN_GENERATED_KEYS);
			sql.executeUpdate();
			ResultSet rskey = sql.getGeneratedKeys();
			if (rskey.next()) {
				int id = rskey.getInt(1);
				for (String k : index.getKeywords()) {
					con.createStatement().executeUpdate("INSERT INTO keywords (id, keyword) VALUES ("+id+",\'"+k+"\')");
				}
			} else {
				throw new SQLException("Failed to get autogen ID");
			}
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
			ResultSet rs = sql.executeQuery("SELECT i.id, i.url, i.title, i.description, i.created_at, GROUP_CONCAT(k.keyword) AS keywords FROM indexes i LEFT JOIN keywords k ON i.id = k.id GROUP BY i.id");
			while (rs.next()) {
				String keywords = rs.getString("keywords");
				out.add(new Index(rs.getInt("id"), rs.getString("url"), 
						rs.getString("title"), rs.getString("description"), 
						keywords == null || keywords.isEmpty() ? new String[0] : keywords.split(", *"),
						DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("created_at").toLocalDateTime())));
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
	
	public void updateIndexKeywords(int id, String[] keywords) {
		try {
			Statement sql = con.createStatement();
			sql.executeUpdate("DELETE FROM keywords WHERE id = "+id);
			for (String s : keywords) {
				sql = con.createStatement();
				sql.executeUpdate("INSERT INTO keywords (id, keyword) VALUES ("+id+",\'"+s+"\')");
			}
		} catch (SQLException e) {
			System.err.println("Failed to update keywords of ID "+id);
			e.printStackTrace();
		}
	}
	
	public void deleteIndex(int id) {
		try {
			Statement sql = con.createStatement();
			sql.executeUpdate("DELETE FROM indexes WHERE id = "+id);
			sql = con.createStatement();
			sql.executeUpdate("DELETE FROM keywords WHERE id = "+id);
		} catch (SQLException e) {
			System.err.println("Failed to delete index of ID "+id);
			e.printStackTrace();
		}
	}
	
}

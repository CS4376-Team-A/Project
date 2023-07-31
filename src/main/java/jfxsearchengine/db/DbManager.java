package jfxsearchengine.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jfxsearchengine.App;

public class DbManager {

	private static final String URL = "jdbc:mysql://server.sarcly.xyz:3306/searchengine";
	private static final String USER = "cyberminer";
	private static final String PASS = "cyberminer";
	private static final String indexesTable = App.DEBUG ? "indexes_test" : "indexes";
	private static final String keywordsTable = App.DEBUG ? "keywords_test" : "keywords";
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
		if (keywords == null || keywords.length == 0) return null;
		ObservableList<Index> out = FXCollections.observableArrayList();
		try {
			// Prepare the SQL query with placeholders for the keywords
			String placeholders = String.join(" OR ", Collections.nCopies(keywords.length, "keyword LIKE ?"));
			String sqlQuery = "SELECT i.id, i.url, i.title, i.description, GROUP_CONCAT(k.keyword) AS keywords " +
							"FROM "+indexesTable+" i " +
							"LEFT JOIN "+keywordsTable+" k ON i.id = k.id " +
							"WHERE " + placeholders + " GROUP BY i.id";
			// Create and set up the PreparedStatement
			PreparedStatement sql = con.prepareStatement(sqlQuery);
			int parameterIndex = 1;
			for (String keyword : keywords) {
				sql.setString(parameterIndex++, keyword);
			}
			ResultSet rs = sql.executeQuery(); // Execute the query
			while (rs.next()) {
				out.add(new Index(rs.getInt("id"), rs.getString("url"), rs.getString("title"), rs.getString("description")));
			}
		} catch (SQLException e) {
			System.err.println("Failed to select from DB");
			e.printStackTrace();
			return null;
		}
		return out;
	}
	
	public ObservableList<Index> findByAllKeywords(String[] keywords) {
		if (keywords == null || keywords.length == 0) return null;
		ObservableList<Index> out = FXCollections.observableArrayList();
		try {
			// Build the sql query
			String likeStatements = "";
			String havingStatements = "";
			for (int i=0;i<keywords.length;i++) {
				likeStatements += "k.keyword LIKE \'"+keywords[i]+"\'";
				havingStatements += "SUM(k.keyword LIKE \'"+keywords[i]+"\') > 0";
				if (i < keywords.length-1) {
					likeStatements += " OR ";
					havingStatements += " AND ";
				}
			}
			String sqlQuery = "SELECT i.id, i.url, i.title, i.description, GROUP_CONCAT(k.keyword) AS keywords "
					+ "FROM "+indexesTable+" i LEFT JOIN "+keywordsTable+" k ON i.id = k.id WHERE ("+likeStatements+") GROUP BY i.id HAVING "+havingStatements;
			System.out.println(sqlQuery);
			ResultSet rs = con.createStatement().executeQuery(sqlQuery); //Create and Execute the query
			while (rs.next()) {
				out.add(new Index(rs.getInt("id"), rs.getString("url"), rs.getString("title"), rs.getString("description")));
			}
		} catch (SQLException e) {
			System.err.println("Failed to select from DB");
			e.printStackTrace();
			return null;
		}
		return out;
	}
	
	public ObservableList<Index> findByNotKeywords(String[] keywords) {
		if (keywords == null || keywords.length == 0) return null;
		ObservableList<Index> out = FXCollections.observableArrayList();
		try {
			// Build the sql query
			String likeStatements = "";
			String havingStatements = "";
			for (int i=0;i<keywords.length;i++) {
				likeStatements += "k.keyword NOT LIKE \'"+keywords[i]+"\'";
				havingStatements += "SUM(k.keyword LIKE \'"+keywords[i]+"\') = 0";
				if (i < keywords.length-1) {
					likeStatements += " OR ";
					havingStatements += " AND ";
				}
			}
			String sqlQuery = "SELECT i.id, i.url, i.title, i.description, GROUP_CONCAT(k.keyword) AS keywords "
					+ "FROM "+indexesTable+" i LEFT JOIN "+keywordsTable+" k ON i.id = k.id WHERE ("+likeStatements+") GROUP BY i.id HAVING "+havingStatements;
			ResultSet rs = con.createStatement().executeQuery(sqlQuery); //Create and Execute the query
			while (rs.next()) {
				out.add(new Index(rs.getInt("id"), rs.getString("url"), rs.getString("title"), rs.getString("description")));
			}
		} catch (SQLException e) {
			System.err.println("Failed to select from DB");
			e.printStackTrace();
			return null;
		}
		return out;
	}
	
	public boolean saveIndex(Index index) {
		try {
			PreparedStatement sql = con.prepareStatement("INSERT INTO "+indexesTable+" (url, title, description) VALUES " + index.toString(), Statement.RETURN_GENERATED_KEYS);
			sql.executeUpdate();
			ResultSet rskey = sql.getGeneratedKeys();
			if (rskey.next()) {
				int id = rskey.getInt(1);
				for (String k : index.getKeywords()) {
					con.createStatement().executeUpdate("INSERT INTO "+keywordsTable+" (id, keyword) VALUES ("+id+",\'"+k+"\')");
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
			ResultSet rs = sql.executeQuery("SELECT COUNT(*) FROM "+indexesTable+" WHERE url = \'"+url+"\'");
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
			ResultSet rs = sql.executeQuery("SELECT i.id, i.url, i.title, i.description, i.created_at, GROUP_CONCAT(k.keyword) AS keywords FROM "+indexesTable+" i LEFT JOIN "+keywordsTable+" k ON i.id = k.id GROUP BY i.id");
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
			sql.executeUpdate("UPDATE "+indexesTable+" SET title = \'"+title+"\' WHERE id = "+id);
		} catch (SQLException e) {
			System.err.println("Failed to update title of ID "+id);
			e.printStackTrace();
		}
	}
	
	public void updateIndexUrl(int id, String url) {
		try {
			Statement sql = con.createStatement();
			sql.executeUpdate("UPDATE"+indexesTable+"SET url = \'"+url+"\' WHERE id = "+id);
		} catch (SQLException e) {
			System.err.println("Failed to update url of ID "+id);
			e.printStackTrace();
		}
	}
	
	public void updateIndexDescription(int id, String desc) {
		try {
			Statement sql = con.createStatement();
			sql.executeUpdate("UPDATE "+indexesTable+" SET description = \'"+desc+"\' WHERE id = "+id);
		} catch (SQLException e) {
			System.err.println("Failed to update description of ID "+id);
			e.printStackTrace();
		}
	}
	
	public void updateIndexKeywords(int id, String[] keywords) {
		try {
			Statement sql = con.createStatement();
			sql.executeUpdate("DELETE FROM "+keywordsTable+" WHERE id = "+id);
			for (String s : keywords) {
				sql = con.createStatement();
				sql.executeUpdate("INSERT INTO "+keywordsTable+" (id, keyword) VALUES ("+id+",\'"+s+"\')");
			}
		} catch (SQLException e) {
			System.err.println("Failed to update keywords of ID "+id);
			e.printStackTrace();
		}
	}
	
	public void deleteIndex(int id) {
		try {
			Statement sql = con.createStatement();
			sql.executeUpdate("DELETE FROM "+indexesTable+" WHERE id = "+id);
			sql = con.createStatement();
			sql.executeUpdate("DELETE FROM "+keywordsTable+" WHERE id = "+id);
		} catch (SQLException e) {
			System.err.println("Failed to delete index of ID "+id);
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	public void tryAutoFillKeywords() {
		if (true) return; //dont want this function to run accidentally
		
		try {
			Statement sql = con.createStatement();
			ResultSet rs = sql.executeQuery("SELECT id, title, description FROM "+indexesTable);
			while (rs.next()) {
				String title = rs.getString("title");
				String desc = rs.getString("description");
				int id = rs.getInt("id");
				System.out.println(id);
				for (String s : ((title==null||title.isBlank()?"":title) + (desc==null||desc.isBlank()?"":desc)).replaceAll("[^a-zA-Z0-9 ]", "").split(" +")) {
					Statement sql2 = con.createStatement();
					sql2.executeUpdate("INSERT INTO "+keywordsTable+" (id, keyword) VALUES ("+id+",\'"+s+"\')");
				}
			}
		} catch (SQLException e) {
			System.err.println("Failed");
			e.printStackTrace();
		}
	}
	
}

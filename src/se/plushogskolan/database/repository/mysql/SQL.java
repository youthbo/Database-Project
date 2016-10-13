package se.plushogskolan.database.repository.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class SQL {

	private final static String URL = "jdbc:mysql://localhost:3306/DatabaseProject?useSSL=false";
	private final static String DB_USER = "awesome";
	private final static String DB_PASSWORD = "database";

	private String sql;
	private List<Object> paramList = new ArrayList<>();

	public SQL(String sql) {
		this.sql = sql;
	}

	public SQL param(Object o) {
		paramList.add(o);
		return this;
	}

	public PreparedStatement prepareStatement(Connection conn) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(sql);
		for (int i = 0; i < paramList.size(); i++) {
			statement.setObject(i + 1, paramList.get(i));
		}
		return statement;
	}

	public void update() throws SQLException {
		try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD)) {
			conn.setAutoCommit(false);
			try (PreparedStatement statement = prepareStatement(conn)) {
				statement.executeUpdate();
				conn.commit();
			} catch (SQLException e) {
				conn.rollback();
			}
		}
	}

	public <T> T singleQuery(Mapper<T> mapper) throws SQLException {
		try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
				PreparedStatement statement = prepareStatement(conn)) {
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				return mapper.map(result);
			}
		}
		return null;
	}

	public <T> List<T> multiQuery(Mapper<T> mapper) throws SQLException {
		try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
				PreparedStatement statement = prepareStatement(conn)) {
			ResultSet result = statement.executeQuery();
			List<T> issueList = new ArrayList<>();
			while (result.next()) {
				issueList.add(mapper.map(result));
			}
			return issueList;
		}
	}

	public boolean exists() throws SQLException {
		try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
				PreparedStatement statement = prepareStatement(conn)) {
			ResultSet result = statement.executeQuery();
			return result.next();
		}
	}

}

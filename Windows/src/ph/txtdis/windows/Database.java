package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
	public static String error = "";
	private static Database database = null;
	private Connection connection = null;

	private Database() {
	}

	public static Database getInstance() {
		if (database == null) {
			database = new Database();
		}
		return database;
	}

	public Connection getConnection(String u, String p) {
		if (connection == null) {
			try {
				connection = DriverManager.getConnection(""
						+ "jdbc:postgresql://" +
						// "192.168.1.100" +
						"localhost" +
						// "magnumstamaria.no-ip.org" +
						":5432/txtdis", u, p);
			} catch (SQLException e) {
				e.printStackTrace();
				error = e.toString();
			}
		}
		return connection;
	}

	public Connection getConnection() {
		return connection;
	}

	public void closeConnection() {
		try {
			if (connection != null)
				connection.close();
			connection = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
	public static String error = "";
	//private static String ip = "192.168.1.100";
	private static String ip = "localhost";
	//private static String ip = "magnumsmb.no-ip.biz";
	//private static String ip = "magnumstamaria.no-ip.org";
	private static String dbase = "magnum_sta_maria_";
	private static Database database = null;
	private Connection connection = null;

	private Database() {
		dbase += DIS.BUILD;
	}

	public static Database getInstance() {
		if (database == null) {
			database = new Database();
		}
		return database;
	}

	public Connection getConnection(String userName, String password) {
		if (connection == null) {
			try {
				connection = DriverManager.getConnection(""
						+ "jdbc:postgresql://" + ip
						+ ":5432/" + dbase, userName, password);
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

	public static String getIp() {
		return ip;
	}

	public static String getDbase() {
		return dbase;
	}
}

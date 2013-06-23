package ph.txtdis.windows;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
	public static String error = "";
	private static String ip = "localhost";
	private static String dbase = "magnum_sta_maria_";
	private static Database database = null;
	private Connection connection = null;

	private Database() {
		try {
			if (!InetAddress.getLocalHost().getHostAddress()
					.equals("169.254.61.76")) {
				ip = "192.168.1.100";
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		dbase += View.getBuildNum();
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
						// + "magnumstamaria.no-ip.org"
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

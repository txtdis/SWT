package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
	public static String error = "";
	private static String dbase = "magnum_sta_maria_";
	//private static String dbase = "mgdc_gsm";
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

	public Connection getConnection(String userName, String password, String ip) {
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

	public static String getDbase() {
		return dbase;
	}
}

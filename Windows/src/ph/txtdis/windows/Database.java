package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
	public static String error = "";
	private static String dbase = "";
	private static Database database;
	private Connection connection;

	private Database() {
	}

	public static Database getInstance() {
		if (database == null)
			database = new Database();
		return database;
	}

	public Connection getConnection(String userName, String password, String dbase) {
		closeConnection();
		connection = getConnectionFromLAN(userName, password, dbase);
		return connection;
	}

	public Connection getConnection() {
		return connection;
	}

	public void closeConnection() {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String getDbase() {
		return dbase;
	}

	private Connection getConnectionFromLAN(String userName, String password, String dbase) {
		try {
			return DriverManager.getConnection("jdbc:postgresql://192.168.1.100:5432/" + dbase, userName,
			        password);
		} catch (Exception e) {
			return getConnectionRemotely(userName, password, dbase);
		}
	}

	private Connection getConnectionRemotely(String userName, String password, String dbase) {
		try {
			return DriverManager.getConnection("jdbc:postgresql://" + dbase + ".no-ip.biz:5432/" + dbase,
			        userName, password);
		} catch (Exception e) {
			return getConnectionLocally(userName, password, dbase);
		}
	}

	private Connection getConnectionLocally(String userName, String password, String dbase) {
		try {
			return DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + dbase, userName, password);
		} catch (Exception e) {
			e.printStackTrace();
			error = e.toString();
			return null;
		}
	}
}

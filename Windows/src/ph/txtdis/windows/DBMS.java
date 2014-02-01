package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBMS {
	private static String error = "";
	private static DBMS dbms;
	private Connection conn;

	private DBMS() {
	}

	public static DBMS getInstance() {
		if (dbms == null)
			dbms = new DBMS();
		return dbms;
	}

	public Connection getConn(String user, String pword, String server, String network) {
		closeConnection();
		Login.setUser(user);
		createConnection(user, pword, server, network);
		return conn;
	}

	public Connection getConnection() {
		return conn;
	}

	public void closeConnection() {
		try {
			if (conn != null)
				conn.close();
		} catch (Exception e) {
			error += format(e);
		}
	}

	private void createConnection(String user, String pword, String server, String network) {
		try {
			conn = DriverManager.getConnection("jdbc:postgresql://" + network + ":5432/" + server, user, pword);
		} catch (Exception e) {
			error += format(e);
		}
	}

	public static String error() {
		return error;
	}
	
	public String format(Exception e) {
		return e.toString().replace(":", ":\n").replace(". ", ".\n");
	}
}
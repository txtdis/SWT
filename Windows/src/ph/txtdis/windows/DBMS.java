package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBMS {
	private static DBMS dbms;
	private Connection conn;

	private DBMS() {
	}

	public static DBMS getInstance() {
		if (dbms == null)
			dbms = new DBMS();
		return dbms;
	}

	public Connection getConn(String user, String pword, String server, String network) throws SQLException {
		closeConnection();
		Login.setUser(user);
		createConnection(user, pword, server, network);
		return conn;
	}

	public Connection getConnection() {
		return conn;
	}

	public void closeConnection() {
		if (conn != null)
	        try {
	            conn.close();
            } catch (SQLException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
	}

	private void createConnection(String user, String pword, String server, String network) throws SQLException {
		conn = DriverManager.getConnection("jdbc:postgresql://" + network + ":5432/" + server, user, pword);
	}

	public String format(Exception e) {
		return e.toString().replace(":", ":\n").replace(". ", ".\n");
	}
}
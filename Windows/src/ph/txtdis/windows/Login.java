package ph.txtdis.windows;

import java.sql.SQLException;

public class Login {

	private static String group, user;
	private static String server = "startup";
	private static String network = "pc";
	private static String version = "0.9.8.7"; // should be 91

	public Login(final String user, final String pword, final String server, final String network)
	        {
		final String sql = "SELECT pg_roles.rolname FROM pg_roles\n"
		        + "          INNER JOIN pg_auth_members ON pg_roles.oid = pg_auth_members.roleid\n"
		        + "          INNER JOIN pg_user ON pg_user.usesysid = pg_auth_members.member\n"
		        + "	   WHERE usename = ?";

		try {
	        group = (String) new Query(user, pword, server, network).getDatum(user, sql);
	        if (group != null) {
	        	Login.user = user;
	        	Login.server = server;
	        	Login.network = network;
	        	new DBMSPreConnCheck();
	        	Login.version = DIS.SERVER_VERSION;
	        	new MainMenu();
	        }
        } catch (SQLException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	}

	public static String group() {
		return group;
	}

	public static void setGroup(String group) {
		Login.group = group;
	}

	public static String user() {
		return user;
	}

	public static void setUser(String user) {
		Login.user = user;
	}

	public static String server() {
		return server;
	}

	public static void setServer(String site) {
		Login.server = site;
	}

	public static String network() {
		return network;
	}

	public static void setNetwork(String network) {
		Login.network = network;
	}

	public static String version() {
		return version;
	}

	public static void setVersion(String version) {
		Login.version = version;
	}
}
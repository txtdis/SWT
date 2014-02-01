package ph.txtdis.windows;

public class Login {

	private static String group, user;
	private static String server = "startup";
	private static String network = "pc";
	private static String version = "0.9.9.1";
	private static boolean isOK;

	public Login(final String user, final String pword, final String server, final String network) {
		final String sql = "SELECT pg_roles.rolname FROM pg_roles\n"
		        + "          INNER JOIN pg_auth_members ON pg_roles.oid = pg_auth_members.roleid\n"
		        + "          INNER JOIN pg_user ON pg_user.usesysid = pg_auth_members.member\n"
		        + "	   WHERE usename = ?";

		new ProgressDialog("Connecting to server") {
			@Override
			public void proceed() {
				group = (String) new Query(user, pword, server, network).getDatum(user, sql);
			}
		};
				
		if (group != null) {
			Login.user = user;
			Login.server = server;
			Login.network = network;
			isOK = new DBMSPreConnCheck().isOK();
		} else if (DBMS.error().contains("password authentication failed")) {
			new ErrorDialog("Incorrect username\nand/or password.");
		} else {
			new ErrorDialog("No server connection.\n" + DBMS.error());
		}

		if (!isOK)
			new ErrorDialog(DBMS.error());
		else if (DBMS.error().contains("Update successful"))
			new InfoDialog(DBMS.error());
		else {
			Login.version = DIS.SERVER_VERSION;
			new MainMenu();
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
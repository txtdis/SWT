package ph.txtdis.windows;

public class Login {

	private static String group = "";
	private static String user = "";

	public Login(String u, String p) {
		group = (String) new Data(u, p).getDatum(u, "" +
					"SELECT pg_roles.rolname\n" +
					"  FROM pg_roles\n" +
					"       INNER JOIN pg_auth_members " +
					"			ON pg_roles.oid = pg_auth_members.roleid\n" +
					"       INNER JOIN pg_user " +
					"			ON pg_user.usesysid = pg_auth_members.member\n" +
					"	WHERE usename = ? " +
					"");
		user = u;
		if (group == null) {
			group = "";
			user = "";
		}
	}

	public static String getGroup() {
		return group;
	}

	public static void setGroup(String group) {
		Login.group = group;
	}

	public static String getUser() {
		return user;
	}

	public static void setUser(String user) {
		Login.user = user;
	}
}
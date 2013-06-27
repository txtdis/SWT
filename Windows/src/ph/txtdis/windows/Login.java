package ph.txtdis.windows;

public class Login {

	public static String group = "";
	public static String user = "";

	public Login(String u, String p) {
		group = (String) new SQL(u, p).getDatum(u, "" +
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
}
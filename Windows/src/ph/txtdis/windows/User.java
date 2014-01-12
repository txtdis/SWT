package ph.txtdis.windows;

public class User {

	public static boolean isFinance() {
		return Login.getGroup().contains("finance") || Login.getGroup().contains("sys_admin");
	}
}

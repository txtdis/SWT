package ph.txtdis.windows;

public class User {

	public static boolean isAdmin() {
		return Login.group().equals("sys_admin");
	}

	public static boolean isFinance() {
		return Login.group().contains("finance") || isAdmin();
	}

	public static boolean isSales() {
		return Login.group().contains("sales") || isAdmin();
	}

	public static boolean isSupply() {
		return Login.group().contains("supply") || isAdmin();
    }
}

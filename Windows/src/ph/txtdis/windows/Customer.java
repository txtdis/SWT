package ph.txtdis.windows;

public class Customer {

	public static boolean isShortIdOnFile(String shortId) {
		return (boolean) new Query().getDatum(shortId, "SELECT EXISTS (SELECT 1 FROM customer_header WHERE sms_id = ?);");
	}

	public static boolean isOnFile(int id) {
		return (boolean) new Query().getDatum(id, "SELECT EXISTS (SELECT 1 FROM customer_header WHERE id = ?);");
	}

	public static int getId(String sms) {
		return (int) new Query().getDatum(sms, "SELECT id FROM customer_header WHERE sms_id = ?;");
	}

	public static String getName(int id) {
		return (String) new Query().getDatum(id, "SELECT name FROM customer_header WHERE id = ?;");
	}

	public static String getName(String sms) {
		return (String) new Query().getDatum(sms, "SELECT name FROM customer_header WHERE sms_id = ?;");
	}

	public static String getBankName(int id) {
		return (String) new Query().getDatum(id, ""
		        + "SELECT cm.name FROM customer_header AS cm INNER JOIN channel AS ch ON cm.type_id = ch.id "
		        + " WHERE cm.id = ? AND ch.name = 'BANK' ");
	}
}

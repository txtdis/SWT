package ph.txtdis.windows;

public class CustomerHelper {

	private String name;

	public CustomerHelper() {
	}

	public CustomerHelper(int id) {
		name = (String) new Data().getDatum(id, "" 
// @sql:on
				+ "SELECT name " 
				+ "  FROM customer_master " 
				+ " WHERE id = ?"
// @sql:off
		        );
	}

	public boolean isOnFile() {
		return getName() != null ? true : false;
	}

	public boolean hasSmsId(String smsId) {
		name = (String) new Data().getDatum(smsId, "" 
// @sql:on
				+ "SELECT name " 
				+ "  FROM customer_master " 
				+ " WHERE sms_id = ? "
// @sql:off
		        );
		return name != null ? true : false;
	}

	public int getIdfromSms(String smsId) {
		return Integer.parseInt((String) new Data().getDatum(smsId, "" 
// @sql:on
				+ "SELECT id " 
				+ "  FROM customer_master "
				+ " WHERE sms_id = ?"
// @sql:off
		        ));
	}

	public String getName() {
		return name;
	}

	public String getName(int partnerId) {
		return (String) new Data().getDatum(partnerId, "" 
// @sql:on
				+ "SELECT name " 
				+ "  FROM customer_master " 
				+ " WHERE id = ?"
// @sql:off
		        );
	}

	public String getBankName(int id) {
		return (String) new Data().getDatum(id, "" 
// @sql:on
				+ "SELECT name " 
				+ "  FROM customer_master " 
				+ " WHERE id = ? AND type_id = 10 "
// @sql:off
		        );
	}

	public boolean isVendor(int id) {
		String name = (String) new Data().getDatum(id,"" 
// @sql:on
				+ "SELECT cm.name\n" 
				+ "  FROM customer_master AS cm "
				+ "       INNER JOIN channel AS ch "
				+ "          ON cm.type_id = ch.id\n" 
				+ " WHERE cm.id = ? AND (ch.name = 'VENDOR');\n"
// @sql:off
		        );
		return name != null ? true : false;
	}

	public boolean isExTruck(int id) {
		String name = (String) new Data().getDatum(id, "" 
// @sql:on
				+ "SELECT name "
				+ "  FROM customer_master\n"
				+ " WHERE id = ? AND name like '%EX-TRUCK%';\n"
// @sql:off
		        );
		return name != null ? true : false;
	}

	public boolean isInternalOrOthers(int id) {
		String name = (String) new Data().getDatum(id,"" 
// @sql:on
				+ "SELECT cm.name\n"
				+ "  FROM customer_master AS cm "
				+ "       INNER JOIN channel AS ch ON cm.type_id = ch.id\n"
				+ " WHERE     cm.id = ? "
				+ "       AND (ch.name = 'INTERNAL' OR ch.name = 'OTHERS');\n"
// @sql:off
		        );
		return name != null ? true : false;
	}
}

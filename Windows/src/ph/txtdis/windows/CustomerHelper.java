
package ph.txtdis.windows;


public class CustomerHelper {

	private String name;

	public CustomerHelper() {		
	}
	
	public CustomerHelper(int id)  {
		name = (String) new SQL().getDatum(id, "" +
				"SELECT name " +
				"FROM 	customer_master " +
				"WHERE 	id = ?"
				);
	}

	public boolean isOnFile() {
		return getName() != null ? true : false;
	}

	public boolean hasSmsId(String smsId) {
		name = (String) new SQL().getDatum(smsId, "" +
				"SELECT name " +
				"FROM 	customer_master " +
				"WHERE 	sms_id = ? "
				);	
		return name != null ? true : false;
	}

	public int getIdfromSms(String smsId) {
		return Integer.parseInt((String) new SQL().getDatum(smsId, "" +
				"SELECT id " +
				"FROM 	customer_master " +
				"WHERE 	sms_id = ?"
				));
	}

	public String getName() {
		return name;
	}

	public String getBankName(int id) {
		return (String) new SQL().getDatum(id, "" +
				"SELECT name " +
				"FROM 	customer_master " +
				"WHERE 	id = ? " +
				"	AND type_id = 10 "
				);
	}
	
	public boolean isVendor(int id) {
		String name = (String) new SQL().getDatum(id, "" +
				"SELECT cm.name\n" +
				"  FROM customer_master AS cm INNER JOIN channel AS ch ON cm.type_id = ch.id\n" +
				" WHERE cm.id = ? AND (ch.name = 'VENDOR');\n" 
				);	
		return name != null ? true : false;
	}
	
	public boolean isExTruck(int id) {
		String name = (String) new SQL().getDatum(id, "" +
				"SELECT name\n" +
				"  FROM customer_master\n" +
				" WHERE id = ? AND name like '%EX-TRUCK%';\n" 
				);	
		return name != null ? true : false;
	}
	
	public boolean isInternalOrOthers(int id) {
		String name = (String) new SQL().getDatum(id, "" +
				"SELECT cm.name\n" +
				"  FROM customer_master AS cm INNER JOIN channel AS ch ON cm.type_id = ch.id\n" +
				" WHERE cm.id = ? AND (ch.name = 'INTERNAL' OR ch.name = 'OTHERS');\n" 
				);	
		return name != null ? true : false;
	}
}

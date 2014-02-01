package ph.txtdis.windows;

public class Contact {
	private int id;
	private String name, surname, designation;

	public Contact() {
	}

	public Contact(int customer_id) {
		Object[] contacts = new Query().getList(customer_id,""
				// @sql:on
				+ "SELECT id,\n" 
				+ "		  name,\n" 
				+ "		  CASE WHEN surname IS NULL THEN ' ' ELSE surname END,\n"
		        + "		  CASE WHEN designation IS NULL THEN ' ' ELSE designation END\n" 
				+ "  FROM contact_detail AS cd " 
		        + " WHERE cd.customer_id = ?"
		        + " LIMIT 1;"
				// @sql:off
		        );
		if (contacts == null)
			return;
		id = (int) contacts[0];
		name = (String) contacts[1];
		surname = (String) contacts[2];
		designation = (String) contacts[3];
	}

	public static int getId(String name) {
		return (int) new Query().getDatum(name, "" + "SELECT	id " + "  FROM	contact_detail " + " WHERE name = ? ");
	}

	public static String getName(int id) {
		return (String) new Query().getDatum(id, "" + "SELECT	name " + "  FROM	contact_detail " + " WHERE	id = ? ");
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSurname() {
		return surname;
	}

	public String getDesignation() {
		return designation;
	}

	public String getFullName(int partnerId) {
		Object object = new Query().getDatum(partnerId,"" 
				// @sql:on
				+ "SELECT name || ' ' || surname " 
				+ "		  surname, "
		        + "		  designation " 
				+ "  FROM contact_detail AS cd " 
		        + " WHERE cd.customer_id = ? "
				// @sql:off
		        );
		return object == null ? "" : (String) object;
	}

	public String getFullName() {
		return name + " " + surname;
	}
}

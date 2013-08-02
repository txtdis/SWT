package ph.txtdis.windows;

public class Contact {
	private int id;
	private String name, surname, designation;
	private Object[] contacts;

	public Contact(int customer_id) {
		contacts = new Data().getData(customer_id, "" 
				+ "SELECT id, " 
				+ "		  name, " 
				+ "		  surname, "
		        + "		  designation " 
				+ "  FROM contact_detail AS cd " 
		        + " WHERE cd.customer_id = ? " + "");
		setContact();
	}

	public Contact() {
		// @sql:on
		contacts = new Data().getData("" 
				+ "SELECT cd.id, " 
				+ "		  cd.name, " 
				+ "		  surname, "
		        + "		  designation " 
		        + "  FROM contact_detail AS cd " 
				+ "       INNER JOIN customer_master AS cm "
		        + "          ON cd.customer_id = cm.id " 
				+ "       INNER JOIN channel AS ch ON cm.type_id = ch.id "
		        + " WHERE     cd.name = upper(current_user) " 
				+ "	      AND ch.name = 'SELF';");
				;
		// @sql:off
		setContact();
	}

	private void setContact() {
		contacts = contacts == null ? new Object[4] : contacts;
		id = contacts[0] == null ? 0 : (int) contacts[0];
		name = contacts[1] == null ? "" : (String) contacts[1];
		surname = contacts[2] == null ? "" : (String) contacts[2];
		designation = contacts[3] == null ? "" : (String) contacts[3];
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
	
	public String getFullName() {
		return name + " " + surname;
	}
}

package ph.txtdis.windows;

public class ContactDetail {
	private int id;
	private String name, surname, designation;

	public ContactDetail(int customer_id) {
		Object[] ao = new SQL().getData(customer_id, "" +
				"SELECT	cd.id, " +
				"		cd.name, " +
				"		cd.surname, " +
				"		cd.designation " +
				"FROM	contact_detail AS cd " +
				"WHERE	cd.customer_id = ? " +
				""
				);
		if (ao == null) ao = new Object[4];
		id 			= ao[0] == null ? 0 : (int) ao[0];
		name 		= ao[1] == null ? "" : (String) ao[1];
		surname 	= ao[2] == null ? "" : (String) ao[2];
		designation = ao[3] == null ? "" : (String) ao[3];
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

}

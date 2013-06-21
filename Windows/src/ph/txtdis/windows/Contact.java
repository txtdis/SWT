package ph.txtdis.windows;

public class Contact {

	public Contact() {
	}

	public String getName(int outletId) {
		return (String) new SQL().getDatum(outletId, "" +
				"SELECT name || ' ' || surname " +
				"FROM 	contact_detail " +
				"WHERE 	customer_id = ?"
				);
	}

	public String[] getSalesReps() {
		return (String[]) new SQL().getData("" +
				"SELECT name || ' ' || surname " +
				"FROM contact_detail " +
				"WHERE designation = 'DSP' " +
				"	AND customer_id = 0");
	}
}

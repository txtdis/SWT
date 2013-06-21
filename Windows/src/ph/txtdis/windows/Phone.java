package ph.txtdis.windows;

public class Phone {
	private long phone;

	public Phone(int id) {
		Object o = new SQL().getDatum(id, "" +
				"SELECT	p.number " +
				"FROM	phone_number AS p " +
				"WHERE	p.contact_id = ? " +
				""  
				);
		phone = o == null ? 0L : (long) o;
	}

	public long getNumber() {
		return phone;
	}
}

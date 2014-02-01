package ph.txtdis.windows;

public class Phone {
	private long phone;

	public Phone(int contactId) {
		// @sql:on
		Object object = new Query().getDatum(contactId, "" 
				+ "SELECT p.number " 
				+ "  FROM phone_number AS p "
		        + " WHERE p.contact_id = ? ");
		// @sql:off
		phone = object == null ? 0L : (long) object;
	}

	public long getNumber() {
		return phone;
	}
}

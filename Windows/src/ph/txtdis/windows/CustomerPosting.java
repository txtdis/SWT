package ph.txtdis.windows;

import java.sql.SQLException;

public class CustomerPosting extends Posting {
	private boolean wasUpdated;
	private int contactId;
	private Customer customer;

	public CustomerPosting(Order order) {
		super(order);
		customer = (Customer) order;
	}

	@Override
	protected void postData() throws SQLException {
		id = customer.getId();
		wasUpdated = id != 0 ? true : false;
		if (wasUpdated) {
			// @sql:on
			ps = conn.prepareStatement(""
					+ "UPDATE customer_master "
					+ "	  SET sms_id = ?,"
					+ "		  name = ?,"
					+ "		  type_id = ? "
					+ " WHERE id = ? ; ");				
			// @sql:off
		} else {
			// @sql:on
			ps = conn.prepareStatement(""
					+ "INSERT INTO customer_master (sms_id, name, type_id) "
					+ "    VALUES (?, ?, ?) RETURNING id; ");
			// @sql:off
		}

		ps.setString(1, customer.getSmsId());
		ps.setString(2, customer.getName());
		ps.setInt(3, new Channel(customer.getChannel()).getId());
		if (wasUpdated) {
			ps.setInt(4, id);
			ps.executeUpdate();
		} else {
			rs = ps.executeQuery();
			if (rs.next())
				id = rs.getInt(1);
		}

		if (!wasUpdated) {
			// @sql:on
			ps = conn.prepareStatement(""
					+ "INSERT INTO address (customer_id, street, district, city, province) " 
					+ "    VALUES (?, ?, ?, ?, ?)");
			// @sql:off
			ps.setInt(1, id);
			ps.setString(2, customer.getStreet());
			ps.setInt(3, new Area(customer.getDistrict()).getId());
			ps.setInt(4, new Area(customer.getCity()).getId());
			ps.setInt(5, new Area(customer.getProvince()).getId());
			ps.executeUpdate();
		}

		String firstName = customer.getFirstName();
		boolean isThereAContactInput = !firstName.isEmpty();
		boolean isCustomerContactOnFile = customer.isCustomerContactOnFile(id);
		if (isThereAContactInput) {
			if (isCustomerContactOnFile) {
				// @sql:on
				ps = conn.prepareStatement(""
						+ "UPDATE contact_detail "
						+ "	  SET name = ?, "
						+ "		  surname = ?, "
						+ "       designation = ? "
						+ " WHERE customer_id = ?  " );
				// @sql:off
			} else {
				// @sql:on
				ps = conn.prepareStatement(""
						+ "INSERT INTO contact_detail (name, surname, designation, customer_id) " 
						+ "    VALUES (?, ?, ?, ?) RETURNING id; ");
				// @sql:off					
			}
			ps.setString(1, firstName);
			ps.setString(2, customer.getSurname());
			ps.setString(3, customer.getDesignation());
			ps.setInt(4, id);
			if (isCustomerContactOnFile) {
				ps.executeUpdate();
			} else {
				rs = ps.executeQuery();
				if (rs.next())
					contactId = rs.getInt(1);
			}

			long phone = customer.getPhone();
			boolean isThereAPhoneInput = phone > 0L;
			if (!isThereAPhoneInput) {
				// @sql:on
				ps = conn.prepareStatement(""
						+ "INSERT INTO phone_number (contact_id, number) "
				        + "	VALUES (?, ?); ");
				// @sql:off
				ps.setInt(1, contactId);
				ps.setLong(2, phone);
				ps.executeUpdate();
			}
		} else if (customer.isContactPhoneOnFile(contactId)) {
			// @sql:on
			ps = conn.prepareStatement(""
					+ "DELETE FROM contact_detail "
					+ " WHERE customer_id = ? ");
			// @sql:off
			ps.setInt(1, id);
			ps.executeUpdate();
		}

		if (customer.isRouteChanged()) {
			int routeId = new Route().getId(customer.getRoute());
			// @sql:on
			ps = conn.prepareStatement("" 
					+ "INSERT INTO account (customer_id, route_id) " 
					+ "	VALUES (?, ?)");
			// @sql:off
			ps.setInt(1, id);
			ps.setInt(2, routeId);
			ps.executeUpdate();
		}

		if (customer.isCreditChanged()) {
			// @sql:on
			ps = conn.prepareStatement("" 
				   + "INSERT INTO credit_detail (customer_id, credit_limit, term, grace_period, start_date) "
			       + "    VALUES (?, ?, ?, ?, ?)");
			// @sql:off
			ps.setInt(1, id);
			ps.setBigDecimal(2, customer.getCreditLimit());
			ps.setInt(3, customer.getCreditTerm());
			ps.setInt(4, customer.getGracePeriod());
			ps.setDate(5, customer.getCreditStartDate());
			ps.executeUpdate();
		}

		if (customer.isDiscountChanged()) {
			// @sql:on
			ps = conn.prepareStatement(""
					+ "INSERT INTO discount (customer_id, family_id, level_1, level_2, start_date) " 
					+ "    VALUES (?, ?, ?, ?, ?)");
			// @sql:off
			ps.setInt(1, id);
			ps.setInt(2, customer.getFamilyId());
			ps.setBigDecimal(3, customer.getFirstLevelDiscountRate());
			ps.setBigDecimal(4, customer.getSecondLevelDiscountRate());
			ps.setDate(5, customer.getDiscountStartDate());
			ps.executeUpdate();
		}
	}
}

package ph.txtdis.windows;

import java.sql.SQLException;

public class CustomerPosting extends Posting {
	private boolean wasUpdated;
	private int contactId;
	private CustomerData customer;
	private String firstName;

	public CustomerPosting(OrderData order) {
		super(order);
		customer = (CustomerData) order;
	}

	@Override
	protected void postData() throws SQLException {
		id = customer.getId();
		wasUpdated = id != 0 ? true : false;

		if (wasUpdated)
			ps = conn.prepareStatement("UPDATE customer_header SET sms_id = ?, name = ?, type_id = ? WHERE id = ?;");				
		else
			ps = conn.prepareStatement("INSERT INTO customer_header (sms_id, name, type_id VALUES (?, ?, ?) RETURNING id; ");

		ps.setString(1, customer.getSmsId());
		ps.setString(2, customer.getName());
		ps.setInt(3, Channel.getId(customer.getChannel()));
		if (wasUpdated) {
			ps.setInt(4, id);
			ps.executeUpdate();
		} else {
			rs = ps.executeQuery();
			if (rs.next())
				id = rs.getInt(1);
		}

		if (!wasUpdated) {
			ps = conn.prepareStatement("INSERT INTO address (customer_id, street, district, city, province) " 
					+ " VALUES (?, ?, ?, ?, ?)");
			ps.setInt(1, id);
			ps.setString(2, customer.getStreet());
			ps.setInt(3, new Area(customer.getDistrict()).getId());
			ps.setInt(4, new Area(customer.getCity()).getId());
			ps.setInt(5, new Area(customer.getProvince()).getId());
			ps.executeUpdate();
		}

		String firstName = customer.getFirstName();
		if (isThereContactDetailEntry()) {
			if (doesCustomerHaveContact(id))
				ps = conn.prepareStatement("UPDATE contact_detail SET name = ?, surname = ?, designation = ? "
						+ " WHERE customer_id = ?;");
			else
				ps = conn.prepareStatement("INSERT INTO contact_detail (name, surname, designation, customer_id) " 
						+ " VALUES (?, ?, ?, ?) RETURNING id;");
			ps.setString(1, firstName);
			ps.setString(2, customer.getSurname());
			ps.setString(3, customer.getDesignation());
			ps.setInt(4, id);
			if (doesCustomerHaveContact(id)) {
				ps.executeUpdate();
			} else {
				rs = ps.executeQuery();
				if (rs.next())
					contactId = rs.getInt(1);
			}

			long phone = customer.getPhone();
			boolean isThereAPhoneInput = phone > 0L;
			if (isThereAPhoneInput) {
				ps = conn.prepareStatement("INSERT INTO phone_number (contact_id, number) VALUES (?, ?);");
				ps.setInt(1, contactId);
				ps.setLong(2, phone);
				ps.executeUpdate();
			}
		} else if (doesCustomerHavePhoneOnFile(id)) {
			ps = conn.prepareStatement("DELETE FROM contact_detail WHERE customer_id = ? ");
			ps.setInt(1, id);
			ps.executeUpdate();
		}

		if (customer.isRouteChanged()) {
			int routeId = Route.getId(customer.getRoute());
			ps = conn.prepareStatement("INSERT INTO account (customer_id, route_id) VALUES (?, ?);");
			ps.setInt(1, id);
			ps.setInt(2, routeId);
			ps.executeUpdate();
		}

		if (customer.isCreditChanged()) {
			ps = conn.prepareStatement("INSERT INTO credit (customer_id, credit_limit, term, grace_period, start_date) "
			       + " VALUES (?, ?, ?, ?, ?)");
			ps.setInt(1, id);
			ps.setBigDecimal(2, customer.getCreditLimit());
			ps.setInt(3, customer.getCreditTerm());
			ps.setInt(4, customer.getGracePeriod());
			ps.setDate(5, customer.getCreditStartDate());
			ps.executeUpdate();
		}

		if (customer.isDiscountChanged()) {
			ps = conn.prepareStatement("INSERT INTO discount (customer_id, family_id, level_1, level_2, start_date) " 
					+ " VALUES (?, ?, ?, ?, ?)");
			ps.setInt(1, id);
			ps.setInt(2, customer.getFamilyId());
			ps.setBigDecimal(3, customer.getDiscount1Percent());
			ps.setBigDecimal(4, customer.getDiscount2Percent());
			ps.setDate(5, customer.getDiscountStartDate());
			ps.executeUpdate();
		}
	}

	private boolean doesCustomerHavePhoneOnFile(int customerId) {
		try {
	        new Phone(customerId).getNumber();
	        return true;
        } catch (Exception e) {
    	    return false;
        }
    }

	private boolean doesCustomerHaveContact(int customerId) {
		try {
	        new Contact(customerId).getName();
	        return true;
        } catch (Exception e) {
    	    return false;
        }
    }

	private boolean isThereContactDetailEntry() {
		return (firstName != null && !firstName.isEmpty()) ? true : false;
    }
}

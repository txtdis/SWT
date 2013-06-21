package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

public class CustomerPosting extends SQL {

	public CustomerPosting() {
		super();
	}

	public boolean set(CustomerMaster cm) {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		int id = 0;
		int contactId = 0;
		Date date;
		Date today = new Date(Calendar.getInstance().getTimeInMillis());

		try {
			conn = Database.getInstance().getConnection();
			conn.setAutoCommit(false);
			// Customer Master
			ps = conn.prepareStatement("INSERT INTO customer_master "
					+ "	(sms_id, name, type_id) VALUES (?, ?, ?) "
					+ "	RETURNING id " + "");
			ps.setString(1, cm.getSmsId());
			ps.setString(2, cm.getName());
			ps.setInt(3, new Channel(cm.getChannel()).getId());
			// Get Customer ID
			rs = ps.executeQuery();
			if (rs.next())
				id = rs.getInt(1);
			// Address
			int provinceId = new Area(cm.getProvince()).getId();
			if (provinceId > 0) {
				ps = conn.prepareStatement("" + "INSERT INTO address "
						+ "	(customer_id, street, district, city, province) "
						+ "	VALUES (?, ?, ?, ?, ?)");
				ps.setInt(1, id);
				ps.setString(2, cm.getStreet());
				ps.setInt(3, new Area(cm.getDistrict()).getId());
				ps.setInt(4, new Area(cm.getCity()).getId());
				ps.setInt(5, provinceId);
				ps.executeUpdate();
			}
			// Contact Details
			String firstName = cm.getFirstName();
			if (!firstName.isEmpty()) {
				ps = conn.prepareStatement("INSERT INTO contact_detail "
						+ "	(name, surname, designation, customer_id) "
						+ "	VALUES (?, ?, ?, ?) RETURNING id " + "");
				ps.setString(1, firstName);
				ps.setString(2, cm.getSurname());
				ps.setString(3, cm.getDesignation());
				ps.setInt(4, id);
				// Get Contact ID
				rs = ps.executeQuery();
				if (rs.next())
					contactId = rs.getInt(1);
				// Phone Number
				long phone = cm.getPhone();
				if (phone > 0L) {
					ps = conn
							.prepareStatement("INSERT INTO phone_number "
									+ "	(contact_id, number) "
									+ "	VALUES (?, ?)");
					ps.setInt(1, contactId);
					ps.setLong(2, phone);
					ps.executeUpdate();
				}
			}
			// Route
			int routeId = new Route(cm.getRoute()).getId();
			if (routeId > 0) {
				ps = conn.prepareStatement("" + "INSERT INTO account "
						+ "	(customer_id, route_id) " + "	VALUES (?, ?)");
				ps.setInt(1, id);
				ps.setInt(2, routeId);
				ps.executeUpdate();
			}
			// Credit Details
			ArrayList<Credit> creditList = cm.getCreditList();
			for (int i = 0; i < creditList.size(); i++) {
				date = creditList.get(i).getDate();
				if (date == null)
					date = today;
				ps = conn
						.prepareStatement(""
								+ "INSERT INTO credit_detail "
								+ "	(customer_id, credit_limit, term, grace_period, "
								+ " start_date) VALUES (?, ?, ?, ?, ?)");
				ps.setInt(1, id);
				ps.setBigDecimal(2, creditList.get(i).getCreditLimit());
				ps.setInt(3, creditList.get(i).getTerm());
				ps.setInt(4, creditList.get(i).getGracePeriod());
				ps.setDate(5, date);
				ps.executeUpdate();
			}
			// Discount
			ps = conn
					.prepareStatement(""
							+ "INSERT INTO discount "
							+ "	(customer_id, family_id, level_1, level_2, start_date) "
							+ "	VALUES (?, ?, ?, ?, ?)");
			ArrayList<PartnerDiscount> discountList = cm.getDiscountList();
			for (int i = 0; i < discountList.size(); i++) {
				date = discountList.get(i).getDate();
				if (date == null)
					date = today;
				ps.setInt(1, id);
				ps.setInt(2, discountList.get(i).getItemFamilyId());
				ps.setBigDecimal(3, discountList.get(i).getRate1());
				ps.setBigDecimal(4, discountList.get(i).getRate2());
				ps.setDate(5, date);
				ps.executeUpdate();
			}
			cm.setId(id);
			conn.commit();
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException er) {
					er.printStackTrace();
					new ErrorDialog(er);
					return false;
				}
			}
			e.printStackTrace();
			new ErrorDialog(e);
			return false;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				new ErrorDialog(e);
				return false;
			}
		}
		return true;
	}
}

package ph.txtdis.windows;

import java.sql.SQLException;

public class OrderPosting extends DeliveryPosting {

	public OrderPosting(OrderData order) {
		super(order);
	}

	@Override
	protected void postData() throws SQLException {

		postHeader();
		postDetails();
	}

	protected void postHeader() throws SQLException {
	    ps = conn.prepareStatement("" 
				//  @sql:on
				+ "INSERT INTO " + type + "_header " 
				+ "	(" + type + "_date, customer_id) "
		        + "	VALUES (?, ?) " 
		        + "	RETURNING " + type + "_id "
				//  @sql:off
		        );
		ps.setDate(1, data.getDate());
		ps.setInt(2, data.getPartnerId());
    }
}

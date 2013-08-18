package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

public class DeliveryPosting extends Posting {

	public DeliveryPosting(Order order) {
		super(order);
	}

	@Override
	protected void postData() throws SQLException {

		ps = conn.prepareStatement("" 
				//  @sql:on
				+ "INSERT INTO delivery_header " 
				+ "	(delivery_date, customer_id, ref_id, actual) "
		        + "	VALUES (?, ?, ?, ?) " 
				//  @sql:off
		        );
		ps.setDate(1, order.getDate());
		ps.setInt(2, order.getPartnerId());
		ps.setInt(3, order.getReferenceId());
		ps.setBigDecimal(4, order.getEnteredTotal());

		postDetails();
	}

	protected void postDetails() throws SQLException {
		rs = ps.executeQuery();
		if (rs.next())
			id = rs.getInt(1);

		ps = conn.prepareStatement("" 
				// @sql:on
				+ "INSERT INTO " + type + "_detail " 
				+ "	(" + type + "_id, line_id, item_id, uom, qty) " 
				+ "	VALUES (?, ?, ?, ?, ?); "
				// @sql:off
		        );
		ArrayList<BigDecimal> qtys = order.getQtys();
		ArrayList<Integer> itemIds = order.getItemIds();
		ArrayList<Integer> uomIds = order.getUomIds();
		for (int i = 0, size = itemIds.size(); i < size; i++) {
			ps.setInt(1, id);
			ps.setInt(2, i + 1);
			ps.setInt(3, itemIds.get(i));
			ps.setInt(4, uomIds.get(i));
			ps.setBigDecimal(5, qtys.get(i));
			ps.executeUpdate();
		}
    }
}

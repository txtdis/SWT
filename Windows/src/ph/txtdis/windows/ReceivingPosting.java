package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

public class ReceivingPosting extends Posting {
	private Receiving receiving;

	public ReceivingPosting(Order order) {
		super(order);
		receiving = (Receiving) order;
	}

	@Override
	protected void postData() throws SQLException {

		ps = conn.prepareStatement("" 
				//  @sql:on
				+ "INSERT INTO receiving_header " 
				+ "	(receiving_date, partner_id, ref_id) "
		        + "	VALUES (?, ?, ?) " 
				+ "	RETURNING receiving_id "
				//  @sql:off
		        );
		ps.setDate(1, receiving.getDate());
		ps.setInt(2, receiving.getPartnerId());
		ps.setInt(3, receiving.getReferenceId());
		postDetails(receiving);

	}

	protected void postDetails(Receiving receiving) throws SQLException {
		rs = ps.executeQuery();
		if (rs.next())
			id = rs.getInt(1);

	    ps = conn.prepareStatement("" 
				// @sql:on
				+ "INSERT INTO " + type + "_detail " 
				+ "	(" + type + "_id, line_id, item_id, uom, qty, qc_id, expiry) " 
				+ "	VALUES (?, ?, ?, ?, ?, ?, ?); "
				// @sql:off
		        );
		ArrayList<BigDecimal> qtys = receiving.getQtys();
		ArrayList<Integer> itemIds = receiving.getItemIds();
		ArrayList<Integer> uomIds = receiving.getUomIds();
		ArrayList<Date> expiries = receiving.getExpiries();
		ArrayList<String> qualityStates = receiving.getQualityStates();
		for (int i = 0, size = itemIds.size(); i < size; i++) {
			ps.setInt(1, id);
			ps.setInt(2, i + 1);
			ps.setInt(3, itemIds.get(i));
			ps.setInt(4, uomIds.get(i));
			ps.setBigDecimal(5, qtys.get(i));
			ps.setInt(6, new Quality(qualityStates.get(i)).getId());
			ps.setDate(7, expiries.get(i));
			ps.executeUpdate();
		}
    }
}

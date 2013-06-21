package ph.txtdis.windows;

import java.sql.SQLException;
import java.util.ArrayList;

public class StockTakePosting extends OrderPosting {

	public StockTakePosting() {
		super();
	}

	@Override
	protected void insertData() throws SQLException {
		String h = "INSERT INTO count_header " +
				"	(count_date, location_id, taker_id, checker_id) " +
				"	VALUES (?, ?, ?, ?)" +
				"	RETURNING count_id ";
		String d = "INSERT INTO count_detail " +
				"	(count_id, line_id, item_id, qc_id, uom, qty, expiry) " +
				"	VALUES (?, ?, ?, ?, ?, ?, ?)";
		conn = Database.getInstance().getConnection();
		conn.setAutoCommit(false);
		pssh = conn.prepareStatement(h);
		pssd = conn.prepareStatement(d);
		// StockTake Header
		StockTake st = (StockTake) order;
		pssh.setDate(1, st.getPostDate());
		pssh.setInt(2, st.getLocationId());
		pssh.setInt(3, st.getTakerId());
		pssh.setInt(4, st.getCheckerId());
		rs = pssh.executeQuery();
		if(rs.next()) id = rs.getInt(1);
		// StockTake Details
		ArrayList<ItemCount> lineItems = st.getItemCount();
		for (int i = 0; i < lineItems.size(); i++) {
			ItemCount itemCount = lineItems.get(i);
			pssd.setInt(1, id);
			pssd.setInt(2, i + 1);
			pssd.setInt(3, itemCount.getId());
			pssd.setInt(4, itemCount.getQc());
			pssd.setInt(5, itemCount.getUom());
			pssd.setBigDecimal(6, itemCount.getQty());
			pssd.setDate(7, itemCount.getDate());
			pssd.executeUpdate();
		}
	}
}

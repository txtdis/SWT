package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

public class ReceivingPosting extends Posting {
	private ReceivingData receiving;

	public ReceivingPosting(ReceivingData data) {
		super(data);
		receiving = data;
	}

	@Override
	protected void postData() throws SQLException {

		ps = conn.prepareStatement("INSERT INTO receiving_header (receiving_date, partner_id, ref_id) "
		        + "	VALUES (?, ?, ?) RETURNING receiving_id;");
		ps.setDate(1, receiving.getDate());
		ps.setInt(2, receiving.getPartnerId());
		ps.setInt(3, receiving.getReferenceId());
		postDetails(receiving);

	}

	protected void postDetails(ReceivingData receiving) throws SQLException {
		rs = ps.executeQuery();
		if (rs.next())
			id = rs.getInt(1);

		ps = conn.prepareStatement("INSERT INTO " + type + "_detail (" + type
		        + "_id, line_id, item_id, uom, qty, qc_id, expiry) VALUES (?, ?, ?, ?, ?, ?, ?);");
		ArrayList<BigDecimal> qtys = receiving.getQtys();
		ArrayList<Integer> itemIds = receiving.getItemIds();
		ArrayList<Type> uoms = receiving.getUoms();
		ArrayList<Date> expiries = receiving.getExpiries();
		ArrayList<Type> qualities = receiving.getQualities();
		for (int i = 0, size = itemIds.size(); i < size; i++) {
			ps.setInt(1, id);
			ps.setInt(2, i + 1);
			ps.setInt(3, itemIds.get(i));
			ps.setInt(4, UOM.getId(uoms.get(i)));
			ps.setBigDecimal(5, qtys.get(i));
			ps.setInt(6, new Quality(qualities.get(i)).getId());
			ps.setDate(7, expiries.get(i));
			ps.executeUpdate();
		}
	}
}

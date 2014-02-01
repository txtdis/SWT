package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

public class DeliveryPosting extends Posting {

	public DeliveryPosting(OrderData order) {
		super(order);
	}

	@Override
	protected void postData() throws SQLException {

		ps = conn.prepareStatement(""
				// @sql:on
				+ "INSERT INTO delivery_header (delivery_date, customer_id, ref_id, actual)\n"
				+ "VALUES (?, ?, ?, ?) RETURNING delivery_id;\n"
				// @sql:off
		        );
		ps.setDate(1, data.getDate());
		ps.setInt(2, data.getPartnerId());
		ps.setInt(3, data.getReferenceId());
		ps.setBigDecimal(4, data.getEnteredTotal());

		postDetails();
	}

	protected void postDetails() throws SQLException {
		rs = ps.executeQuery();
		if (rs.next())
			id = rs.getInt(1);

		ps = conn.prepareStatement("INSERT INTO " + type + "_detail (" + type + "_id, line_id, item_id, uom, qty) "
		        + "VALUES (?, ?, ?, ?, ?); ");
		ArrayList<BigDecimal> qtys = data.getQtys();
		ArrayList<Integer> itemIds = data.getItemIds();
		ArrayList<Type> uoms = data.getUoms();
		for (int i = 0, size = itemIds.size(); i < size; i++) {
			ps.setInt(1, id);
			ps.setInt(2, i + 1);
			ps.setInt(3, itemIds.get(i));
			ps.setInt(4, UOM.getId(uoms.get(i)));
			ps.setBigDecimal(5, qtys.get(i));
			ps.executeUpdate();
		}
	}
}

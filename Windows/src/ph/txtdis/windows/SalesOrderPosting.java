package ph.txtdis.windows;

import java.sql.SQLException;

public class SalesOrderPosting extends OrderPosting {

	public SalesOrderPosting() {
		super();
	}

	@Override
	protected void setType() {
		type = "sales";
	}

	@Override
	protected void insertData() throws SQLException {
		pssh = conn.prepareStatement("" +
				"INSERT INTO " + type + "_header " +
				"	(" + type + "_date, customer_id) " +
				"	VALUES (?, ?) " +
				"	RETURNING " + type + "_id "
				);
		pssh.setDate(1, order.getPostDate());
		pssh.setInt(2, order.getPartnerId());
		rs = pssh.executeQuery();
		if (rs.next()) id = rs.getInt(1);

		pssd = conn.prepareStatement("" +
				"INSERT INTO " + type + "_detail " +
				"(" + type + "_id, line_id, item_id, uom, qty) " +
				"VALUES (?, ?, ?, ?, ?)"
				);
		for (int i = 0; i < order.getItemIds().size(); i++) {
			pssd.setInt(1, id);
			pssd.setInt(2, i + 1);
			pssd.setInt(3, order.getItemIds().get(i));
			pssd.setInt(4, order.getUomIds().get(i));
			pssd.setBigDecimal(5, order.getQtys().get(i));
			pssd.executeUpdate();
		}
	}
}

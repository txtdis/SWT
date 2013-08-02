package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public abstract class OrderPosting extends Data {
	protected String type;
	protected Connection conn;
	protected PreparedStatement pssh, pssd;
	protected ResultSet rs;
	protected int id;
	protected Order order;

	public OrderPosting() {
		super();
	}

	public boolean set(Order order){
		this.order = order;
		id = order.getId();
		setType();
		try {
			conn = Database.getInstance().getConnection();
			conn.setAutoCommit(false);
			insertData();
			conn.commit();
			order.setId(id);
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
			new ErrorDialog(e);
			return false;
		} finally {
			try {
				if (pssh != null ) pssh.close();
				if (pssd != null ) pssd.close();
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
				new ErrorDialog(e);
				return false;
			}
		}
		return true;
	}

	protected void setType() {
	}

	protected void insertData() throws SQLException {
		pssh = conn.prepareStatement("" +
				"INSERT INTO " + type + "_header " +
				"	(ref_id, " + type + "_date, customer_id, actual) " +
				"	VALUES (?, ?, ?, ?) " +
				"	RETURNING " + type + "_id "
				);
		pssh.setInt(1, order.getSoId());
		pssh.setDate(2, order.getPostDate());
		pssh.setInt(3, order.getPartnerId());
		pssh.setBigDecimal(4, order.getEnteredTotal());
		rs = pssh.executeQuery();
		if (rs.next()) id = rs.getInt(1);

		pssd = conn.prepareStatement("" +
				"INSERT INTO " + type + "_detail " +
				"(" + type + "_id, line_id, item_id, uom, qty) " +
				"VALUES (?, ?, ?, ?, ?)"
				);
		ArrayList<Integer> itemIds = order.getItemIds();
		ArrayList<Integer> uomIds = order.getUomIds();
		ArrayList<BigDecimal> qtys = order.getQtys();
		int listSize = order.getItemIds().size();
		for (int i = 0; i < listSize; i++) {
			pssd.setInt(1, id);
			pssd.setInt(2, i + 1);
			pssd.setInt(3, itemIds.get(i));
			pssd.setInt(4, uomIds.get(i));
			pssd.setBigDecimal(5, qtys.get(i));
			pssd.executeUpdate();
		}
	}
}

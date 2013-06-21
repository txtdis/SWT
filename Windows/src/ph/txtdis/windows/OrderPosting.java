package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class OrderPosting extends SQL {
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
		pssh.setBigDecimal(4, order.getActual());
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
			pssd.setInt(4, order.getUoms().get(i));
			pssd.setBigDecimal(5, order.getQtys().get(i));
			pssd.executeUpdate();
		}
	}
}

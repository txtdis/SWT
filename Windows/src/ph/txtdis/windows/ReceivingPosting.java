package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReceivingPosting extends Data {

	public ReceivingPosting() {
		super();
	}

	public boolean set(Receiving order) {
		Connection conn = null;
		PreparedStatement psh = null;
		PreparedStatement psd = null;
		try {
			conn = Database.getInstance().getConnection();
			conn.setAutoCommit(false);
			// Receiving Header
			int id = 0;
			// @sql:on
			String h = "INSERT INTO receiving_header (receiving_date, partner_id, ref_id) " 
					+ "						  VALUES (?, ?, ?) "
			        + "	RETURNING receiving_id ";
			String d = "INSERT INTO receiving_detail (receiving_id, line_id, item_id, qc_id, uom, qty) "
			        + "	                      VALUES (?, ?, ?, ?, ?, ?)";
			// @sql:off
			psh = conn.prepareStatement(h);
			psh.setDate(1, order.getPostDate());
			psh.setInt(2, order.getPartnerId());
			psh.setInt(3, order.getRefId());
			ResultSet rs = psh.executeQuery();
			if (rs.next())
				id = rs.getInt(1);
			// Receiving Details
			psd = conn.prepareStatement(d);
			int listSize = order.getItemIds().size();
			for (int i = 0; i < listSize; i++) {
				psd.setInt(1, id);
				psd.setInt(2, i + 1);
				psd.setInt(3, order.getItemIds().get(i));
				psd.setInt(4, new Quality(order.getQualityStates().get(i)).getId());
				psd.setInt(5, order.getUomIds().get(i));
				psd.setBigDecimal(6, order.getQtys().get(i));
				psd.executeUpdate();
			}
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
			e.printStackTrace();
			new ErrorDialog(e);
			return false;
		} finally {
			try {
				if (psh != null)
					psh.close();
				if (psd != null)
					psd.close();
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
				new ErrorDialog(e);
				return false;
			}
		}
		return true;
	}
}

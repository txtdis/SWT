package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReceivingPosting extends SQL {

	public ReceivingPosting() {
		super();
	}

	public boolean set(Receiving order){
		Connection conn = null;
		PreparedStatement psh = null;
		PreparedStatement psd = null;
		int id = 0;
		
		String h = "INSERT INTO receiving_header " +
				"	(rr_date, partner_id, ref_id) " +
				"	VALUES (?, ?, ?) " +
				"	RETURNING rr_id " +
				"";
		String d = "INSERT INTO receiving_detail " +
				"	(rr_id, line_id, item_id, qc_id, uom, qty) " +
				"	VALUES (?, ?, ?, ?, ?, ?)";
		try {
			conn = Database.getInstance().getConnection();
			conn.setAutoCommit(false);
			psh = conn.prepareStatement(h);
			psd = conn.prepareStatement(d);
			// Receiving Header
			psh.setDate(1, order.getDate());
			psh.setInt(2, order.getPartnerId());
			psh.setInt(3, order.getRefId());
			ResultSet rs = psh.executeQuery();
			if (rs.next())
				id = rs.getInt(1);
			// Receiving Details
			for (int i = 0; i < order.getItemIds().size(); i++) {
				psd.setInt(1, id);
				psd.setInt(2, i + 1);
				psd.setInt(3, order.getItemIds().get(i));
				psd.setInt(4, order.getQcs().get(i));
				psd.setInt(5, order.getUoms().get(i));
				psd.setBigDecimal(6, order.getQtys().get(i));
				psd.executeUpdate();
			}
			conn.commit();
			order.setRrId(id);
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
				if (psh != null ) psh.close();
				if (psd != null ) psd.close();
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

package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RemittancePosting extends SQL {

	public RemittancePosting() {
		super();
	}

	public boolean set(Remittance order){
		Connection conn = null;
		PreparedStatement psrh = null;
		PreparedStatement psrd = null;
		PreparedStatement psu = null;
		ResultSet rs = null;

		String h = "INSERT INTO remittance_header " +
				"	(bank_id, remit_date, remit_time, ref_id, total, or_id) " +
				"	VALUES (?, ?, ?, ?, ?, ?) " +
				"	RETURNING remit_id " +
				"";
		try {
			conn = Database.getInstance().getConnection();
			conn.setAutoCommit(false);
			psrh = conn.prepareStatement(h);
			// Remittance Header
			psrh.setInt(1, order.getPartnerId());
			psrh.setDate(2, order.getPostDate());
			psrh.setTime(3, order.getPostTime());
			psrh.setInt(4, order.getRefId());
			psrh.setBigDecimal(5, order.getTotalPayment());
			psrh.setInt(6, order.getOrId());
			// Get Customer ID
			int id = 0;
			rs = psrh.executeQuery();		
			if (rs.next()) id = rs.getInt(1);
			// Remittance Details
			psrd = conn.prepareStatement("" +
					"INSERT INTO remittance_detail " +
					"	(remit_id, line_id, order_id, series, payment) " +
					"	VALUES (?, ?, ?, ?, ?) " +
					"");
			for (int i = 0; i < order.getOrderIds().size(); i++) {
				psrd.setInt(1, id);
				psrd.setInt(2, i + 1);
				psrd.setInt(3, order.getOrderIds().get(i));
				psrd.setString(4, order.getSeriesList().get(i));
				psrd.setBigDecimal(5, order.getPayments().get(i));
				psrd.executeUpdate();
			}
			conn.commit();
			order.setRemitId(id);
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException er) {
					e.printStackTrace();
					new ErrorDialog(er);
					return false;
				}
			}
			e.printStackTrace();
			new ErrorDialog(e);
			return false;
		} finally {
			try {
				if (rs != null ) rs.close();
				if (psrh != null ) psrh.close();
				if (psrd != null ) psrd.close();
				if (psu != null ) psu.close();
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

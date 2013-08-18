package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RemittanceCancellationPosting extends Data {

	public RemittanceCancellationPosting() {
		super();
	}

	public boolean set(Remittance remit) {
		Connection conn = null;
		PreparedStatement update = null;
		PreparedStatement insert = null;
		int remitId = remit.getId();
		try {
			conn = Database.getInstance().getConnection();
			conn.setAutoCommit(false);
			update = conn.prepareStatement("UPDATE remittance_detail "
					+ "SET payment = NULL WHERE remit_id = " + remitId);
			update.execute();
			// Remittance Details
			insert = conn
					.prepareStatement("INSERT INTO remittance_cancellation "
							+ "(remit_id) VALUES (" + remitId + ");");
			insert.execute();
			conn.commit();
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
				if (update != null)
					update.close();
				if (insert != null)
					insert.close();
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

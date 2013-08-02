package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SalesOrderPrintOut extends Data {
	private int salesId;

	public SalesOrderPrintOut(int salesId) {
		super();
		this.salesId = salesId;
	}
	
	public boolean set() {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = Database.getInstance().getConnection();
			conn.setAutoCommit(false);
			ps = conn.prepareStatement("" +
					"INSERT INTO sales_print_out (sales_id) VALUES (?)");
			ps.setInt(1, salesId);
			ps.execute();
			conn.commit();
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
				if (ps != null ) ps.close();
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				new ErrorDialog(e);
				return false;
			}
		}
		return true;
	}

	public boolean wasPrinted() {
		Object o = getDatum(salesId, "" +
				"SELECT	user_id, " +
				"		time_stamp " +
				"FROM	sales_print_out " +
				"WHERE	sales_id = ? " +
				"");
		if(o == null) {
			return false;
		} else {
			return true;
		}
	}
}

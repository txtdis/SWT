package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Posting extends Data {
	protected int id;
	protected Connection conn;
	protected Order order;
	protected PreparedStatement ps;
	protected ResultSet rs;
	protected String type;

	public Posting(Order order) {
		super();
		this.order = order;
		type = order.getType();
	}
	
	public boolean wasCompleted() {
		if (!new DatabasePreConnectionChecklist().isOK())
			return false;
		return wasCompletedWithoutCheck();
	}

	public boolean wasCompletedWithoutCheck() {
		try {
			conn = Database.getInstance().getConnection();
			conn.setAutoCommit(false);
			postData();
			order.setId(id);
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
			e.printStackTrace();
			new ErrorDialog(e);
			return false;
		} finally {
			try {
				if (ps != null)
					ps.close();
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
				new ErrorDialog(e);
				return false;
			}
		}
		return true;
	}

	protected void postData() throws SQLException {
	}
}

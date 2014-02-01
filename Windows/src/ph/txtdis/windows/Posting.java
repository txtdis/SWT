package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Posting extends Query {
	protected int id;
	protected Connection conn;
	protected OrderData data;
	protected View view;
	protected PreparedStatement ps;
	protected ResultSet rs;
	protected Type type;

	private boolean isOK;
	private boolean isChecklistOK;

	public Posting() {
		super();
    }

	public Posting(OrderData data) {
		super();
		this.data = data;
		type = data.getType();
	}
	
	public Posting(View view) {
		super();
		this.view = view;
		type = view.getType();
    }

	public void patch() {
		isChecklistOK = true;
		getTableData();
	}
	
	public void save() {
		isChecklistOK = new DBMSPreConnCheck().isOK();
		getTableData();		
	}

	private void getTableData() {
		if (isChecklistOK) {
			try {
				conn = getConn();
				conn.setAutoCommit(false);
				postData();
				if (data != null)
					data.setId(id);
				conn.commit();
				isOK = true;
			} catch (SQLException e) {
				if (conn != null) {
					try {
						conn.rollback();
					} catch (SQLException er) {
						er.printStackTrace();
						new ErrorDialog(er);
					}
				}
				e.printStackTrace();
				new ErrorDialog(e);
			} finally {
				try {
					if (ps != null)
						ps.close();
					conn.setAutoCommit(true);
				} catch (SQLException e) {
					e.printStackTrace();
					new ErrorDialog(e);
				}
			}
		}
	}

	protected Connection getConn() {
	    return DBMS.getInstance().getConnection();
    }

	public boolean isOK() {
		return isOK;
	}

	public int getId() {
		return id;
	}

	protected abstract void postData() throws SQLException;
}

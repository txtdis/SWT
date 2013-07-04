
package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ProgramPosting extends SQL {
	private PreparedStatement ps;

	public ProgramPosting() {
		super();
	}

	public boolean set(Program p){
		Connection conn = null;
		ResultSet rs = null;
		int id = 0; 

		try {
			conn = Database.getInstance().getConnection();
			conn.setAutoCommit(false);
			// Program 
			ps = conn.prepareStatement("" +
					"INSERT INTO target_header " +
					"	(type_id, category_id, start_date, end_date) " +
					"	VALUES (?, ?, ?, ?) " +
					"	RETURNING target_id " +
					"");
			ps.setInt(1, p.getTypeId());
			ps.setInt(2, p.getCategoryId());
			ps.setDate(3, p.getStartDate());
			ps.setDate(4, p.getEndDate());
			// Get Program ID
			rs = ps.executeQuery();		
			if (rs.next()) id = rs.getInt(1);
			// Rebates
			ps = conn.prepareStatement("" +
					"INSERT INTO target_rebate " +
					"	(target_id, product_line_id, value) " +
					"	VALUES (?, ?, ?) " +
					"");
			ArrayList<Rebate> rlist = p.getRebateList();
			for (int i = 0; i < rlist.size(); i++) {
				ps.setInt(1, id);
				ps.setInt(2, rlist.get(i).getProductLineId());
				ps.setBigDecimal(3, rlist.get(i).getValue());
				ps.executeUpdate();
			}
			// Targets
			ps = conn.prepareStatement("" +
					"INSERT INTO target_outlet " +
					"	(target_id, outlet_id, product_line_id, qty) " +
					"	VALUES (?, ?, ?, ?) " +
					"");
			ArrayList<Target> tlist = p.getTargetList();
			for (int i = 0; i < tlist.size(); i++) {
				ps.setInt(1, id);
				ps.setInt(2, tlist.get(i).getOutletId());
				ps.setInt(3, tlist.get(i).getProductLineId());
				ps.setBigDecimal(4, tlist.get(i).getQty());
				ps.executeUpdate();
			}
			// Set Program ID
			p.setProgramId(id);
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
				if (rs != null ) rs.close();
				if (ps != null ) ps.close();
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				new ErrorDialog(e);
				return false;
			}
		}
		return true;
	}
}

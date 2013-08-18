package ph.txtdis.windows;

import java.sql.SQLException;

public class SalesTargetPosting extends Posting {
	private SalesTarget salesTarget;

	public SalesTargetPosting(Order order) {
		super(order);
		salesTarget = (SalesTarget) order;
	}

	@Override
	protected void postData() throws SQLException {
		ps = conn.prepareStatement("" 
				// @sql:on
				+ "INSERT INTO target_header " 
				+ "	(type_id, category_id, start_date, end_date) "
		        + "	VALUES (?, ?, ?, ?) " 
				+ "	RETURNING target_id "
				// @sql:off
		        );
		ps.setInt(1, salesTarget.getTargetTypeId());
		ps.setInt(2, salesTarget.getCategoryId());
		ps.setDate(3, salesTarget.getStartDate());
		ps.setDate(4, salesTarget.getEndDate());

		rs = ps.executeQuery();
		if (rs.next())
			id = rs.getInt(1);

		ps = conn.prepareStatement("" 
				// @sql:on
				+ "INSERT INTO target_rebate " 
				+ "	(target_id, product_line_id, value) "
		        + "	VALUES (?, ?, ?) "
				// @sql:off
				);
		for (Rebate rebate : salesTarget.getRebates()) {
			ps.setInt(1, id);
			ps.setInt(2, rebate.getProductLineId());
			ps.setBigDecimal(3, rebate.getValue());
			ps.executeUpdate();
		}

		ps = conn.prepareStatement("" 
				// @sql:on
				+ "INSERT INTO target_outlet " 
				+ "	(target_id, outlet_id, product_line_id, qty) "
		        + "	VALUES (?, ?, ?, ?) "
				// @sql:off
				);
		for (Target target : salesTarget.getTargets()) {
			ps.setInt(1, id);
			ps.setInt(2, target.getOutletId());
			ps.setInt(3, target.getProductLineId());
			ps.setBigDecimal(4, target.getQty());
			ps.executeUpdate();
		}
	}
}

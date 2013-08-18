package ph.txtdis.windows;

import java.sql.SQLException;

public class StockTakePosting extends ReceivingPosting {
	private StockTake stockTake;

	public StockTakePosting(Order order) {
		super(order);
		stockTake = (StockTake) order;
	}

	@Override
	protected void postData() throws SQLException {

		ps = conn.prepareStatement("" 
				//  @sql:on
				+ "INSERT INTO count_header " 
				+ "	(count_date, location_id, taker_id, checker_id) "
		        + "	VALUES (?, ?, ?, ?) " 
				+ "	RETURNING count_id "
				//  @sql:off
		        );
		ps.setDate(1, stockTake.getDate());
		ps.setInt(2, stockTake.getLocationId());
		ps.setInt(3, stockTake.getTakerId());
		ps.setInt(4, stockTake.getCheckerId());
		postDetails(stockTake);
	}
}

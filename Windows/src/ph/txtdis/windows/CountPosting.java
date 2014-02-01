package ph.txtdis.windows;

import java.sql.SQLException;

public class CountPosting extends ReceivingPosting {
	private CountData count;

	public CountPosting(CountData data) {
		super(data);
		count = data;
	}

	@Override
	protected void postData() throws SQLException {

		ps = conn.prepareStatement("INSERT INTO count_header (count_date, location_id, taker_id, checker_id) "
				+ "VALUES (?, ?, ?, ?) RETURNING count_id ");
		ps.setDate(1, count.getDate());
		ps.setInt(2, count.getLocationId());
		ps.setInt(3, count.getTakerId());
		ps.setInt(4, count.getCheckerId());
		postDetails(count);
	}
}

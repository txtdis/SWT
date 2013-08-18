package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

public class RemittancePosting extends Posting {
	private Remittance remit;

	public RemittancePosting(Order order) {
		super(order);
		remit = (Remittance) order;
	}

	protected void postData() throws SQLException {
		ps = conn.prepareStatement("" 
				+ "INSERT INTO remittance_header "
		        + "	(bank_id, remit_date, remit_time, ref_id, total, or_id) " 
				+ "	VALUES (?, ?, ?, ?, ?, ?) "
		        + "	RETURNING remit_id "
				);
		ps.setInt(1, order.getPartnerId());
		ps.setDate(2, order.getDate());
		ps.setTime(3, remit.getTime());
		ps.setInt(4, order.getReferenceId());
		ps.setBigDecimal(5, order.getEnteredTotal());
		ps.setInt(6, remit.getReceiptId());
		
		rs = ps.executeQuery();
		if (rs.next())
			id = rs.getInt(1);

		ps = conn.prepareStatement("" 
				+ "INSERT INTO remittance_detail "
		        + "	(remit_id, line_id, order_id, series, payment) " 
				+ "	VALUES (?, ?, ?, ?, ?); " 
		        );

		ArrayList<Integer> orderIds = remit.getOrderIds();
		ArrayList<String> seriesList = remit.getSeriesList();
		ArrayList<BigDecimal> payments = remit.getPayments();
		for (int i = 0, size = orderIds.size(); i < size; i++) {
			ps.setInt(1, id);
			ps.setInt(2, i + 1);
			ps.setInt(3, orderIds.get(i));
			ps.setString(4, seriesList.get(i));
			ps.setBigDecimal(5, payments.get(i));
			ps.executeUpdate();
		}

	}
}

package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

public class InvoicePosting extends Posting {
	private Invoice invoice;

	public InvoicePosting(Order order) {
		super(order);
		invoice = (Invoice) order;
	}

	@Override
	protected void postData() throws SQLException {

		ps = conn.prepareStatement("" 
				//  @sql:on
				+ "INSERT INTO invoice_header " 
				+ "	(invoice_date, customer_id, ref_id, actual, series) "
		        + "	VALUES (?, ?, ?, ?, ?) " 
				//  @sql:off
		        );
		ps.setDate(1, order.getDate());
		ps.setInt(2, order.getPartnerId());
		ps.setInt(3, order.getReferenceId());
		ps.setBigDecimal(4, order.getEnteredTotal());
		ps.setString(5, order.getSeries());

		postDetails(invoice);
	}

	protected void postDetails(Invoice invoice) throws SQLException {
		ps.executeUpdate();
	    ps = conn.prepareStatement("" 
				// @sql:on
				+ "INSERT INTO invoice_detail " 
				+ "	(invoice_id, line_id, item_id, uom, qty, series) " 
				+ "	VALUES (?, ?, ?, ?, ?, ?); "
				// @sql:off
		        );
		ArrayList<BigDecimal> qtys = invoice.getQtys();
		ArrayList<Integer> itemIds = invoice.getItemIds();
		ArrayList<Integer> uomIds = invoice.getUomIds();
		String series = invoice.getSeries();
		for (int i = 0, size = itemIds.size(); i < size; i++) {
			ps.setInt(1, id);
			ps.setInt(2, i + 1);
			ps.setInt(3, itemIds.get(i));
			ps.setInt(4, uomIds.get(i));
			ps.setBigDecimal(5, qtys.get(i));
			ps.setString(6, series);
			ps.executeUpdate();
		}
    }
}

package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

public class InvoicePosting extends OrderPosting {

	public InvoicePosting() {
		super();
	}

	@Override
	protected void setType() {
		type = "invoice";
	}

	@Override
	protected void insertData() throws SQLException {
		pssh = conn.prepareStatement("" +
				"INSERT INTO invoice_header " +
				"	(invoice_id, series, invoice_date, customer_id, actual, ref_id) " +
				"	VALUES (?, ?, ?, ?, ?, ?) " 
				);
		pssh.setInt(1, id);
		pssh.setString(2, order.getSeries());
		pssh.setDate(3, order.getPostDate());
		pssh.setInt(4, order.getPartnerId());
		pssh.setBigDecimal(5, order.getEnteredTotal());
		pssh.setInt(6, order.getSoId());
		pssh.execute();
		pssd = conn.prepareStatement("" +
				"INSERT INTO invoice_detail " +
				"(invoice_id, series, line_id, item_id, uom, qty) " +
				"VALUES (?, ?, ?, ?, ?, ?)"
				);
		ArrayList<Integer> itemIds = order.getItemIds();
		ArrayList<Integer> uomIds = order.getUomIds();
		ArrayList<BigDecimal> qtys = order.getQtys();
		String series = order.getSeries();
		int listSize = order.getItemIds().size();
		for (int i = 0; i < listSize; i++) {
			System.out.println("itemId: " + itemIds.get(i));
			System.out.println("uomId: " + uomIds.get(i));
			System.out.println("qty: " + qtys.get(i));
        }
		for (int i = 0; i < listSize; i++) {
			pssd.setInt(1, id);
			pssd.setString(2, series);
			pssd.setInt(3, i + 1);
			pssd.setInt(4, itemIds.get(i));
			pssd.setInt(5, uomIds.get(i));
			pssd.setBigDecimal(6, qtys.get(i));
			pssd.execute();
		}
	}
}
	
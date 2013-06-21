package ph.txtdis.windows;

import java.sql.SQLException;

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
		pssh.setBigDecimal(5, order.getActual());
		pssh.setInt(6, order.getSoId());
		pssh.execute();
		pssd = conn.prepareStatement("" +
				"INSERT INTO invoice_detail " +
				"(invoice_id, series, line_id, item_id, uom, qty) " +
				"VALUES (?, ?, ?, ?, ?, ?)"
				);
		for (int i = 0; i < order.getItemIds().size(); i++) {
			pssd.setInt(1, id);
			pssd.setString(2, order.getSeries());
			pssd.setInt(3, i + 1);
			pssd.setInt(4, order.getItemIds().get(i));
			pssd.setInt(5, order.getUoms().get(i));
			pssd.setBigDecimal(6, order.getQtys().get(i));
			pssd.execute();
		}
	}
}

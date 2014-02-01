package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

public class InvoicePosting extends Posting {
	private InvoiceData data;

	public InvoicePosting(OrderData data) {
		super(data);
		this.data = (InvoiceData) data;
	}

	@Override
	protected void postData() throws SQLException {
		id = data.getId();
		ps = conn.prepareStatement("INSERT INTO invoice_header "
		        + "(invoice_date, customer_id, ref_id, actual, invoice_id, series) VALUES (?, ?, ?, ?, ?, ?);");
		ps.setDate(1, data.getDate());
		ps.setInt(2, data.getPartnerId());
		ps.setInt(3, data.getReferenceId());
		ps.setBigDecimal(4, data.getEnteredTotal());
		ps.setInt(5, id);
		ps.setString(6, data.getSeries());
		ps.executeUpdate();
		ps = conn.prepareStatement("INSERT INTO invoice_detail (invoice_id, line_id, item_id, uom, qty, series) "
		        + "VALUES (?, ?, ?, ?, ?, ?); ");
		ArrayList<BigDecimal> qtys = data.getQtys();
		ArrayList<Integer> itemIds = data.getItemIds();
		ArrayList<Type> uoms = data.getUoms();
		String series = data.getSeries();
		for (int i = 0, size = itemIds.size(); i < size; i++) {
			ps.setInt(1, id);
			ps.setInt(2, i + 1);
			ps.setInt(3, itemIds.get(i) * (data.isAnRMA() ? -1 : 1));
			ps.setInt(4, UOM.getId(uoms.get(i)));
			ps.setBigDecimal(5, qtys.get(i));
			ps.setString(6, series);
			ps.executeUpdate();
		}
	}
}

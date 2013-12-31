package ph.txtdis.windows;

import java.sql.SQLException;


public class ItemInventory {
	private double rate;
	private int id;

	public ItemInventory(int id) throws SQLException {
		String s = (String) (new Data().getDatum(id, 
				"select sum(d.qty) rate from invoice_detail d, " +
				"invoice_header h where d.invoice_id = h.invoice_id and " +
				"h.invoice_date > (current_date - 30) and d.item_id = ? group by d.item_id"
				));
		rate = (s == null ? 0 : Double.valueOf(s));
	}
	
	public double getRate() {
		return rate;
	}
	
	public boolean isItemNew() throws NumberFormatException, SQLException {
		int i = Integer.parseInt((String) new Data().getDatum(id,
				"select extract(day from (current_timestamp - time_stamp)) from item_master " +
				"where id = ?"));
		return i >= 0 && i < 3 ? true : false;
	}	
}

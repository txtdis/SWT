package ph.txtdis.windows;

import java.sql.SQLException;


public class ItemInventory {
	private double rate;
	private int id;

	public ItemInventory(int id) throws SQLException {
		String s = (String) (new SQL().getDatum(id, 
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
		int i = Integer.parseInt((String) new SQL().getDatum(id,
				"select extract(day from (current_timestamp - time_stamp)) from item_master " +
				"where id = ?"));
		System.out.println(i);
		return i >= 0 && i < 3 ? true : false;
	}
	
	private int getAvailable() throws NumberFormatException, SQLException {
		return Integer.parseInt((String) new SQL().getDatum(id,
				"select (i.on_hand - i.booked + i.inbound - i.outbound + " +
				"i.requisitioned) available from inventory i where i.item_id = ?"
				));
	}
	
	public int getCurrentDays() throws NumberFormatException, SQLException {
		if (rate < 0.01) 
			return 999999;
		double days = Math.ceil(getAvailable()/rate);
		if(days > 999999)
			return 999999;
		return (int) days;
	}
	
	public int getNewDays(int addnlQty) throws NumberFormatException, SQLException {
		int days = (int) (Math.ceil((getAvailable() + addnlQty)/rate));
		return days > 999999 ? 999999 : days;
	}	

}

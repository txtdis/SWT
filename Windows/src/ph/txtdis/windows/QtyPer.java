package ph.txtdis.windows;

import java.math.BigDecimal;

public class QtyPer {
	private BigDecimal qty;
	private int uom;
	private boolean bought, sold, reported;

	public QtyPer() {
	}

	public QtyPer(
			BigDecimal qty, int uom, boolean bought, boolean sold, boolean reported) {
		super();
		this.qty = qty;
		this.uom = uom;
		this.bought = bought;
		this.sold = sold;
		this.reported = reported;
	}

	public BigDecimal getQty() {
		return qty;
	}

	public int getUom() {
		return uom;
	}

	public boolean isBought() {
		return bought;
	}

	public boolean isSold() {
		return sold;
	}

	public boolean isReported() {
		return reported;
	}

	public BigDecimal get(int itemId, int uom)  {
		Object o =  new SQL().getDatum(new Object[] {itemId, uom}, "" +
				"SELECT qty " +
				"FROM 	qty_per " +
				"WHERE 	item_id = ? " +
				"	AND uom = ? " 
				);
		return o != null ? (BigDecimal) o : BigDecimal.ZERO;
	}
}

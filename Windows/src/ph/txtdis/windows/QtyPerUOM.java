package ph.txtdis.windows;

import java.math.BigDecimal;

public class QtyPerUOM {
	private BigDecimal qty;
	private int uomId;
	private boolean isBought, isSold, isReported;

	public QtyPerUOM() {
	}

	public QtyPerUOM(BigDecimal qty, int uomId, boolean isBought, boolean isSold, boolean isReported) {
		super();
		this.qty = qty;
		this.uomId = uomId;
		this.isBought = isBought;
		this.isSold = isSold;
		this.isReported = isReported;
	}

	public BigDecimal getQty() {
		return qty;
	}

	public int getUom() {
		return uomId;
	}

	public boolean isBought() {
		return isBought;
	}

	public boolean isSold() {
		return isSold;
	}

	public boolean isReported() {
		return isReported;
	}

	public BigDecimal getQty(int itemId, int uomId)  {
		Object o =  new Data().getDatum(new Object[] {itemId, uomId}, "" +
				"SELECT qty " +
				"FROM 	qty_per " +
				"WHERE 	item_id = ? " +
				"	AND uom = ? " 
				);
		return o != null ? (BigDecimal) o : BigDecimal.ZERO;
	}

	public BigDecimal getQty(int itemId, String uom)  {
		Object o =  new Data().getDatum(new Object[] {itemId, uom}, "" 
				+ "SELECT qty " 
				+ "  FROM qty_per "
				+ "		  INNER JOIN uom "
				+ "			 ON qty_per.uom = uom.id"
				+ " WHERE     item_id = ? " 
				+ "	      AND uom.unit = ? " 
				);
		return o != null ? (BigDecimal) o : BigDecimal.ZERO;
	}
}

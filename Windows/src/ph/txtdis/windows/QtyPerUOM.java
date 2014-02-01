package ph.txtdis.windows;

import java.math.BigDecimal;

public class QtyPerUOM {
	private BigDecimal qty;
	private Type uom;
	private boolean isBought, isSold, isReported;

	public QtyPerUOM(BigDecimal qty, Type uom, boolean isBought, boolean isSold, boolean isReported) {
		super();
		this.qty = qty;
		this.uom = uom;
		this.isBought = isBought;
		this.isSold = isSold;
		this.isReported = isReported;
	}

	public BigDecimal getQty() {
		return qty;
	}

	public Type getUom() {
		return uom;
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

	public static BigDecimal getQty(int itemId, int uomId)  {
		Object o =  new Query().getDatum(new Object[] {itemId, uomId}, "" +
				"SELECT qty " +
				"FROM 	qty_per " +
				"WHERE 	item_id = ? " +
				"	AND uom = ? " 
				);
		return o != null ? (BigDecimal) o : BigDecimal.ZERO;
	}

	public static BigDecimal getQty(int itemId, Type uom)  {
		Object o =  new Query().getDatum(new Object[] {itemId, uom.toString()}, "" 
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

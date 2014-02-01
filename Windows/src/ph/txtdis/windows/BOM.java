package ph.txtdis.windows;

import java.math.BigDecimal;

public class BOM {
	private int itemId;
	private BigDecimal qty; 
	private Type uom;

	public BOM(int itemId, Type uom, BigDecimal qty) {
		super();
		this.itemId = itemId;
		this.uom = uom;
		this.qty = qty;
	}

	public int getItemId() {
		return itemId;
	}

	public Type getUom() {
		return uom;
	}

	public BigDecimal getQty() {
		return qty;
	}
}

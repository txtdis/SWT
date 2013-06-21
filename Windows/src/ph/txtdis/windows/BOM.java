package ph.txtdis.windows;

import java.math.BigDecimal;

public class BOM {
	private int itemId, uom;
	private BigDecimal qty; 

	public BOM() {
	}

	public BOM(int itemId, int uom, BigDecimal qty) {
		super();
		this.itemId = itemId;
		this.uom = uom;
		this.qty = qty;
	}

	public int getItemId() {
		return itemId;
	}

	public int getUom() {
		return uom;
	}

	public BigDecimal getQty() {
		return qty;
	}
}

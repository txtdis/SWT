package ph.txtdis.windows;

import java.math.BigDecimal;

public class LineItem {
	
 	private int itemId, uom, qty;
	private BigDecimal price;
	private String reason; 

	public LineItem(int itemId, int uom, int qty, BigDecimal price) {
		this(itemId, uom, qty, price, null);
	}
	
	public LineItem(int itemId, int uom, int qty, BigDecimal price, String reason) {
		this.itemId = itemId;
		this.uom = uom;
		this.qty = qty;
		this.price = price;
		this.reason = reason;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public int getUom() {
		return uom;
	}

	public void setUom(int uom) {
		this.uom = uom;
	}


}

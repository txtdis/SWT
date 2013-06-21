package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

public class InvoiceLineItem extends TableLineItem {
	private Text txtItemId, txtQty;
	private Combo cmbUom;
	private Button btnItemId;
	private boolean isReturnedMaterial;
	private int itemId, uom, volumeDiscountUom;
	private String itemName;
	private String[] uoms;
	private BigDecimal qty, unitPrice, price, perQty, volumeDiscount, subTotal;
	
	public InvoiceLineItem(OrderView view, Order order, int row) {
		super(view, order, row);
		subTotal = BigDecimal.ZERO; 
		// Item ID Search Button
		btnItemId = new TableButton(tableItem, row, 0, "Item List").getButton();
		// Item ID Input
		txtItemId = new TableInput(tableItem, row, 1, 0).getText();
		// UOM Input
		cmbUom = new TableSelection(tableItem, row, 3).getCombo();
		// QTY Input
		txtQty = new TableInput(tableItem, row, 4, BigDecimal.ZERO).getText();
		// Set Focus on Item ID
		txtItemId.setTouchEnabled(true);
		txtItemId.setFocus();
		
		//Listeners
		new OrderItemIdEntry(view, this, order);
		new OrderItemUom(this, order);
		new OrderItemQtyEntry(view, this, order, row);
	}
	
	public Text getTxtItemId() {
		return txtItemId;
	}

	public Text getTxtQty() {
		return txtQty;
	}

	public Combo getCmbUnit() {
		return cmbUom;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public BigDecimal getQty() {
		return qty;
	}

	public void setQty(BigDecimal qty) {
		this.qty = qty;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public int getUom() {
		return uom;
	}

	public void setUom(int uom) {
		this.uom = uom;
	}

	public String[] getUoms() {
		return uoms;
	}

	public void setUoms(String[] uoms) {
		this.uoms = uoms;
	}

	public Button getBtnItemId() {
		return btnItemId;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public BigDecimal getPerQty() {
		return perQty;
	}

	public void setPerQty(BigDecimal perQty) {
		this.perQty = perQty;
	}

	public BigDecimal getVolumeDiscount() {
		return volumeDiscount;
	}

	public void setVolumeDiscount(BigDecimal volumeDiscount) {
		this.volumeDiscount = volumeDiscount;
	}

	public int getVolumeDiscountUom() {
		return volumeDiscountUom;
	}

	public void setVolumeDiscountUom(int volumeDiscountUom) {
		this.volumeDiscountUom = volumeDiscountUom;
	}
	
	public BigDecimal getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(BigDecimal subTotal) {
		this.subTotal = subTotal;
	}

	public boolean isReturnedMaterial() {
		return isReturnedMaterial;
	}

	public void setReturnedMaterial(boolean isReturnedMaterial) {
		this.isReturnedMaterial = isReturnedMaterial;
	}

}

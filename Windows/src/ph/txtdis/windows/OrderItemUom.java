package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class OrderItemUom {
	protected Invoice order;
	protected InvoiceLineItem tableLineItem;
	protected Combo cmbUnit;
	protected Text txtQty;
	protected ArrayList<Integer> itemIds;

	private BigDecimal unitPrice, price, qtyPer, perQty, volumeDiscountValue;
	private VolumeDiscount volumeDiscount;
	private int uom, itemId;
	private Date date;

	public OrderItemUom(final InvoiceLineItem tableLineItem, final Order order) {

		txtQty = tableLineItem.getTxtQty();
		cmbUnit = tableLineItem.getCmbUnit();
		Listener cmbListener = new Listener () {
			@Override
			public void handleEvent (Event ev) {
				volumeDiscount = new VolumeDiscount();
				date = order.getPostDate();
				itemId = tableLineItem.getItemId();
				unitPrice = tableLineItem.getUnitPrice();
				switch (ev.type) {
					case SWT.FocusIn:
						cmbUnit.setBackground(View.yellow());
						break;
					case SWT.FocusOut:
						cmbUnit.setBackground(View.white());
						break;
					case SWT.DefaultSelection:
					case SWT.Selection:
						price = unitPrice;
						uom = new UOM(cmbUnit.getText()).getId();
						perQty 	= new BigDecimal(volumeDiscount.getPerQty(itemId, date));
						if (!perQty.equals(new BigDecimal(999_999))) {
							volumeDiscountValue = volumeDiscount.get(itemId, date);
						} else {
							volumeDiscountValue = BigDecimal.ZERO;
						}
						qtyPer = new QtyPer().get(itemId, uom);
						if(new ItemHelper().isMonetaryType(itemId)) {
							price = new BigDecimal(-1);
						} else {
							price = unitPrice.multiply(qtyPer).subtract(
									volumeDiscountValue.multiply(
											qtyPer.divide(perQty, 0, BigDecimal.ROUND_DOWN)));
						}
						tableLineItem.setPrice(price);
						tableLineItem.setUom(uom);
						tableLineItem.setPerQty(perQty);
						tableLineItem.setVolumeDiscount(volumeDiscountValue);
						tableLineItem.getTableItem().setText(5, DIS.LNF.format(price));
						tableLineItem.getTxtQty().setTouchEnabled(true);
						tableLineItem.getTxtQty().setFocus();
				}
			}
		};
		cmbUnit.addListener (SWT.FocusOut, cmbListener);
		cmbUnit.addListener (SWT.FocusIn, cmbListener);
		cmbUnit.addListener (SWT.DefaultSelection, cmbListener);
		cmbUnit.addListener (SWT.Selection, cmbListener);
	}
}


package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

public class OrderItemUom {

	public OrderItemUom(final OrderView view, final Order order) {
		final TableItem tableItem = view.getTableItem(order.getRowIdx());
		final Combo cmbUom = view.getCmbUom();
		cmbUom.setEnabled(true);
		cmbUom.setFocus();
		cmbUom.setItems(order.getUoms());
		cmbUom.select(0);

		Listener cmbListener = new Listener() {
			@Override
			public void handleEvent(Event ev) {
				BigDecimal qtyPerUOM, volumeDiscountQty, volumeDiscountValue;

				switch (ev.type) {
					case SWT.FocusIn:
						cmbUom.setBackground(DIS.YELLOW);
						break;
					case SWT.FocusOut:
						cmbUom.setBackground(DIS.WHITE);
						break;
					case SWT.DefaultSelection:
					case SWT.Selection:
						Date date = order.getPostDate();
						int itemId = order.getItemId();
						
						VolumeDiscount volumeDiscount = new VolumeDiscount();
						volumeDiscountQty = volumeDiscount.getQty(itemId, date);
						volumeDiscountValue = volumeDiscount.getValue(itemId, date);
						qtyPerUOM = new QtyPerUOM().get(itemId, new UOM(cmbUom.getText()).getId());
						
						BigDecimal priceLessVolumeDiscount = order.getPrice().multiply(qtyPerUOM).subtract(
						        volumeDiscountValue.multiply(qtyPerUOM.divide(volumeDiscountQty, 0,
						                BigDecimal.ROUND_DOWN)));
						order.setPrice(priceLessVolumeDiscount);
						order.setVolumeDiscountQty(volumeDiscountQty);
						order.setVolumeDiscountValue(volumeDiscountValue);
						// column 3 is UOM
						tableItem.setText(3, cmbUom.getText());
						cmbUom.dispose();
						// column 5 is price
						tableItem.setText(5, DIS.TWO_PLACE_DECIMAL.format(priceLessVolumeDiscount));
						// column 4 is qty
						new OrderItemQtyEntry(view, order);		
					}
			}
		};
		cmbUom.addListener(SWT.FocusOut, cmbListener);
		cmbUom.addListener(SWT.FocusIn, cmbListener);
		cmbUom.addListener(SWT.DefaultSelection, cmbListener);
		cmbUom.addListener(SWT.Selection, cmbListener);
	}
}

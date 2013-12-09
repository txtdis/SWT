package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class OrderItemUomCombo {

	private Combo uomCombo;
	private OrderView view;
	private Order order;
	private TableItem tableItem;
	private Text qtyInput;

	public OrderItemUomCombo(OrderView orderView, Order report) {
		order = report;
		view = orderView;
		tableItem = view.getTableItem();
		uomCombo = new TableCombo(tableItem, Order.UOM_COLUMN, order.getUoms()).getCombo();
		view.setUomCombo(uomCombo);
		uomCombo.setFocus();
		new ComboSelector(uomCombo, qtyInput) { //view.getPostButton()) {
			@Override
			protected void doAfterSelection() {
				String type = order.getType();
				tableItem.setText(Order.UOM_COLUMN, selection);
				uomCombo.dispose();
				order.setUomId(new UOM(selection).getId());
				if (type.equals("receiving")) {
					String qualityState = "GOOD";
					if (order.isReferenceAnSO()) {
						String partner = order.getPartner();
						if (new OrderHelper().isRMA(order.getReferenceId()) || partner.equals("ITEM REJECTION")) {
							qualityState = "BAD";
						} else if (partner.equals("ITEM ON-HOLD")) {
							qualityState = "ON-HOLD";
						}
					}
					tableItem.setText(4, qualityState);
					((Receiving) order).setQualityState(qualityState);
					new OrderItemExpiryInput((ReceivingView) view, (Receiving) order);
				} else if (type.equals("count")) {
					setNext(((ReceivingView) view).getQualityCombo());
					new OrderItemQualitySelector((ReceivingView) view, (Receiving) order);
				} else {
					int itemId = Math.abs(order.getItemId());
					Date date = order.getDate();

					VolumeDiscount volumeDiscount = new VolumeDiscount();
					BigDecimal volumeDiscountQty = volumeDiscount.getQty(itemId, date);
					BigDecimal volumeDiscountValue = volumeDiscount.getValue(itemId, date);
					BigDecimal qtyPerUOM = new QtyPerUOM().getQty(itemId, selection);

					BigDecimal priceLessVolumeDiscount = order
					        .getPrice()
					        .multiply(qtyPerUOM)
					        .subtract(
					                volumeDiscountValue.multiply(qtyPerUOM.divide(volumeDiscountQty, 0,
					                        BigDecimal.ROUND_DOWN)));
					order.setPrice(priceLessVolumeDiscount);
					order.setVolumeDiscountQty(volumeDiscountQty);
					order.setVolumeDiscountValue(volumeDiscountValue);
					tableItem.setText(Order.PRICE_COLUMN, DIS.TWO_PLACE_DECIMAL.format(priceLessVolumeDiscount));
					new OrderItemQtyInput(view, order);
				}
			}
		};
	}
}

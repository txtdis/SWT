package ph.txtdis.windows;

import java.math.BigDecimal;

public class DeliveryItemIdEntry extends OrderItemIdEntry {
	protected boolean isMonetaryTransaction, isDR;
	protected String monetaryItem;

	public DeliveryItemIdEntry(OrderView orderView, Order report) {
		super(orderView, report);
	}
	
	@Override
	protected boolean doesItemPassOrderNeeds() {
		setMonetaryItem();
		if (isMonetaryTransaction = item.isMonetaryType(itemId, order.getType())) {
			if (!isEnteredTotalNegative) {
				clearEntry("Entered total for a\n" + monetaryItem + " transaction\nmust be negative");
				return false;
			}
			order.setMonetary(isMonetaryTransaction);
			price = BigDecimal.ONE.negate();
		} else if (isEnteredTotalNegative) {
			clearEntry("A negative entered total is for a\n" + monetaryItem + " transaction only");
			return false;
		}
		return true;
	}

	@Override
    protected boolean isItemDiscountSameAsFromSameDayOrders() {
		return false;
    }

	@Override
    protected boolean isItemDiscountSameAsPrevious() {
		return true;
    }

	@Override
	protected void doNext() {
		if (isMonetaryTransaction) {
			tableItem.setText(3, "₱");
			new OrderItemQtyEntry(view, order);
		}
	}
	
	protected void setMonetaryItem() {
		isDR = true;
		monetaryItem = "PCV, EWT or O/R";
	}
}
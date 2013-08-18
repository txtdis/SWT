package ph.txtdis.windows;

import java.math.BigDecimal;

public class DeliveryItemIdEntry extends ItemIdInput {
	protected String monetaryItem;

	public DeliveryItemIdEntry(OrderView orderView, Order report) {
		super(orderView, report);
	}
	
	@Override
	protected boolean isItemMonetaryAndTransactionValid() {
		setMonetaryItem();
		if (isAMonetaryTransaction = item.isMonetaryType(itemId, order.getType())) {
			if (!isEnteredTotalNegative) {
				clearTableItemEntry("Entered total for a\n" + monetaryItem + " transaction\nmust be negative");
				return false;
			}
			order.setAMonetaryTransaction(isAMonetaryTransaction);
			order.setPrice(BigDecimal.ONE.negate());
		} else if (isEnteredTotalNegative) {
			clearTableItemEntry("A negative entered total is for a\n" + monetaryItem + " transaction only");
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
	
	protected void setMonetaryItem() {
		monetaryItem = "PCV, EWT or O/R";
	}
}
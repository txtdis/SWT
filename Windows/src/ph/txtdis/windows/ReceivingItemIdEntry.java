package ph.txtdis.windows;

import java.math.BigDecimal;

public class ReceivingItemIdEntry extends ItemIdInput {

	public ReceivingItemIdEntry(OrderView orderView, Order report) {
		super(orderView, report);
	}
	
	@Override
    protected boolean hasItemBeenEnteredBefore() {
		return false;
    }

	@Override
    protected boolean isItemBizUnitSameAsPrevious() {
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
    protected boolean doesItemHavePrice() {
	    return true;
    }

	@Override
    protected void setNextTableWidget(BigDecimal price) {
		new OrderItemUomCombo(view, order);
    }
}

package ph.txtdis.windows;


public class PurchaseItemIdEntry extends ItemIdInput {

	public PurchaseItemIdEntry(OrderView orderView, Order report) {
		super(orderView, report);
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
    protected boolean isItemOnReferenceOrder() {
		// P/Os start paper trail
		return true;
    }	
}

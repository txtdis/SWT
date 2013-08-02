package ph.txtdis.windows;

public class PurchaseOrderItemIdEntry extends OrderItemIdEntry {

	public PurchaseOrderItemIdEntry(OrderView orderView, Order report) {
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
    protected boolean isItemOnReference() {
		// P/Os start paper trail
		return true;
    }	
}

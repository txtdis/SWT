package ph.txtdis.windows;


public class InvoiceItemIdEntry extends DeliveryItemIdInput {

	public InvoiceItemIdEntry(OrderView view, OrderData data) {
		super(view, data);
	}

	@Override
    protected boolean isNotDealerIncentive() {
	    return !super.isNotDealerIncentive();
    }
}

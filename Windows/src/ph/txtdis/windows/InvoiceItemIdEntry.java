package ph.txtdis.windows;


public class InvoiceItemIdEntry extends DeliveryItemIdEntry {

	public InvoiceItemIdEntry(OrderView orderView, Order report) {
		super(orderView, report);
	}

	@Override
    protected void setMonetaryItem() {
		monetaryItem = "Dealer Incentive";
    }
}

package ph.txtdis.windows;


public class InvoiceItemIdEntry extends DeliveryItemIdEntry {

	public InvoiceItemIdEntry(OrderView orderView, Order report) {
		super(orderView, report);
	}

	@Override
    protected void setMonetaryItem() {
		monetaryItem = "Dealer Incentive";
    }

	@Override
    protected boolean isItemMonetaryAndTransactionValid() {
	    if (!super.isItemMonetaryAndTransactionValid())
	    	return false;
	    if(isAMonetaryTransaction) {
			order.setDealerIncentive(true);
//			if (!helper.hasUnpaidIncentives(partnerId, postDate))
//				return false;
	    }
	    return true;
    }
}

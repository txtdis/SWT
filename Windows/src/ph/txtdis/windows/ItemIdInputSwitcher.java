package ph.txtdis.windows;


public class ItemIdInputSwitcher {

	public ItemIdInputSwitcher(OrderView orderView, OrderData order) {
		switch (order.getType()) {
			case COUNT:
				new CountItemIdEntry(orderView, order);
				break;
			case DELIVERY:
				new DeliveryItemIdInput(orderView, order);
				break;
			case INVOICE:
				new InvoiceItemIdEntry(orderView, order);
				break;
			case PURCHASE:
				new PurchaseItemIdEntry(orderView, order);
				break;
			case RECEIVING:
				new ReceivingItemIdEntry(orderView, order);
				break;
			case SALES:
				new SalesItemIdEntry(orderView, order);
				break;
			default:
				System.out.println(order.getType() + "@itemidinput");
				break;
		}
	}
}

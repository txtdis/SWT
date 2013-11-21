package ph.txtdis.windows;


public class ItemIdInputSwitcher {

	public ItemIdInputSwitcher(OrderView orderView, Order order) {
		switch (order.getType()) {
			case "count":
				new StockTakeItemIdEntry(orderView, order);
				break;
			case "delivery":
				new DeliveryItemIdEntry(orderView, order);
				break;
			case "invoice":
				new InvoiceItemIdEntry(orderView, order);
				break;
			case "purchase":
				new PurchaseItemIdEntry(orderView, order);
				break;
			case "receiving":
				new ReceivingItemIdEntry(orderView, order);
				break;
			case "sales":
				new SalesOrderItemIdEntry(orderView, order);
				break;
			default:
				System.out.println(order.getType());
				break;
		}
	}
}

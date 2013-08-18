package ph.txtdis.windows;

public class OrderViewSwitch {
	public OrderViewSwitch(Order order) {
		int id = order.getId();
		switch (order.getType()) {
			case "count":
				new StockTakeView(id);
				break;
			case "customer":
				new CustomerView(id);
				break;
			case "delivery":
				new DeliveryView(id);
				break;
			case "invoice":
				new InvoiceView(id);
				break;
			case "item":
				new ItemView(id);
				break;
			case "purchase":
				new PurchaseOrderView(id);
				break;
			case "receiving":
				new ReceivingView(id);
				break;
			case "remit":
				new RemittanceView(id);
				break;
			case "sales":
				new SalesOrderView(id);
				break;
			case "target":
				new SalesTargetView(id);
				break;
			default:
				break;
		}
	}
}

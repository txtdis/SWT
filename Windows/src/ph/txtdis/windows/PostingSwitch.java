package ph.txtdis.windows;


public class PostingSwitch {
	private boolean wasSuccessful;

	public PostingSwitch(Order order) {
		switch (order.getType()) {
			case "count":
				wasSuccessful = new StockTakePosting(order).wasCompleted();
				break;
			case "customer":
				wasSuccessful = new CustomerPosting(order).wasCompleted();
				break;
			case "delivery":
				wasSuccessful = new DeliveryPosting(order).wasCompleted();
				break;
			case "invoice":
				wasSuccessful = new InvoicePosting(order).wasCompleted();
				break;
			case "item":
				wasSuccessful = new ItemPosting(order).wasCompleted();
				break;
			case "purchase":
			case "sales":
				wasSuccessful = new OrderPosting(order).wasCompleted();
				break;
			case "receiving":
				wasSuccessful = new ReceivingPosting(order).wasCompleted();
				break;
			case "remit":
				wasSuccessful = new RemittancePosting(order).wasCompleted();
				break;
			case "target":
				wasSuccessful = new SalesTargetPosting(order).wasCompleted();
				break;
			default:
				break;
		}
	}

	public boolean wasSuccessful() {
		return wasSuccessful;
	}
}

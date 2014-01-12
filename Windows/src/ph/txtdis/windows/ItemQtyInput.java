package ph.txtdis.windows;


public class ItemQtyInput {

	public ItemQtyInput(OrderView view, Order order) {
		switch (order.getType()) {
			case "count":
				new StockTakeItemQtyInput((ReceivingView) view, (Receiving) order);
				break;
			case "receiving":
				new ReceivingItemQtyInput((ReceivingView) view, (Receiving) order);
				break;
			default:
				System.out.println(order.getType() + "@itemqtyinput");
		}
	}
}

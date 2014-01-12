package ph.txtdis.windows;

public class Delivery extends Order implements Startable {

	public Delivery() {}

	public Delivery(int orderId) {
		super(orderId);
	}
	
	@Override
	protected void setData() {
		module = "Delivery Report";
		type = "delivery";
		referenceAndActualStmt = "" +
				" h.actual, " +
				" h.ref_id, " +
				"";
	}

	@Override
    public void start() {
		new DeliveryView(0);
    }
}

package ph.txtdis.windows;

public class Delivery extends Order {

	public Delivery() {
	}

	public Delivery(int orderId) {
		super(orderId);
	}
	
	@Override
	protected void setOrder() {
		module = "Delivery Report";
		type = "delivery";
		reference = "" +
				" h.actual, " +
				" h.ref_id, " +
				"";
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Delivery i = new Delivery(0);
		for (Object[] os : i.getData()) {
			for (Object o : os) {
				System.out.print(o + ", ");
			}
			System.out.println();
		}
		Database.getInstance().closeConnection();
	}
}

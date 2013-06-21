package ph.txtdis.windows;


public class SalesOrder extends Order {

	public SalesOrder() {
	}

	public SalesOrder(int orderId) {
		super(orderId);
	}

	@Override
	protected void setOrder() {
		module = "Sales Order";
		type = "sales";
		reference = "" +
				" 0.0 AS actual, " +
				" h.sales_id AS ref_id, " +
				" 0.0 AS payment, " 
				;
		postDate = new DateAdder().plus(1);
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		SalesOrder so = new SalesOrder(1983);
		for (Object[] os : so.getData()) {
			for (Object o : os) {
				System.out.print(o + ", ");
			}
			System.out.println();
		}
		Database.getInstance().closeConnection();
	}

}

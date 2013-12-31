package ph.txtdis.windows;

public class SalesOrder extends Order {
	public SalesOrder() {
	}

	public SalesOrder(int orderId) {
		super(orderId);
	}

	@Override
	protected void setData() {
		module = "Sales Order";
		type = "sales";
		reference = "" +
				" 0.0 AS actual, " +
				" h.sales_id AS ref_id, " +
				" 0.0 AS payment, " 
				;
		date = DIS.TOMORROW;
	}
}

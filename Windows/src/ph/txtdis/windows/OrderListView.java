package ph.txtdis.windows;

public class OrderListView extends ListView {

	public OrderListView(String string) {
		super(string);
	}

	@Override
	protected void runClass() {
		report = new CustomerList(string);
	}
	
	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		new OrderListView("");
		Database.getInstance().closeConnection();
	}
}

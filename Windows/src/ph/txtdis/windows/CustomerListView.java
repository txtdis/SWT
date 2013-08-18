package ph.txtdis.windows;

public class CustomerListView extends ListView {

	public CustomerListView(String string) {
		super(string);
	}

	@Override
	protected void runClass() {
		report = new CustomerList(string);
	}
	
	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin","localhost");
		new CustomerListView("");
		Database.getInstance().closeConnection();
	}
}

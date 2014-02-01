package ph.txtdis.windows;

public class CustomerListView extends ListView {
	
	public CustomerListView() {
		this("");
	}

	public CustomerListView(String name) {
		this(new CustomerList(name));
	}

	public CustomerListView(Data data) {
	    super(data);
		type = Type.CUSTOMER_LIST;
		proceed();
    }
}

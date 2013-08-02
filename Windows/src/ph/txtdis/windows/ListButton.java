package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class ListButton extends ImageButton {

	public ListButton(Composite parent, String module) {
		super(parent, module, "Search16", "Search " + module + " ID");
	}

	@Override
	public void doWhenSelected() {
		switch (module) {
			case "Customer List":
				new CustomerListView("");
				break;
			case "Customer":
				new CustomerView(0);
				break;
			case "Item List":
				new ItemListView("");
				break;
			case "Bank List":
				new BankListView("");
				break;
			default:
				break;
		}
	}
}

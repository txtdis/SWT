package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class AddButton extends ImageButton {

	public AddButton(Composite parent, String module) {
		super(parent, module, "Plus", "Add new entry");
	}

	@Override
	protected void doWhenSelected() {
		parent.getShell().dispose();
		switch (module) {
			case "Customer List":
				new CustomerView(0);
				break;
			case "Target List":
				new SalesTargetView(0);
				break;
			case "Item List":
				new ItemView(0);
				break;
			case "Issued Invoice Booklet List":
				new InvoiceBookletDialog();
				break;
			default:
				new InfoDialog("No Add Button option for " + module);
				break;
		}
	}
}

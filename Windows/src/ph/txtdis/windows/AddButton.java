package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class AddButton extends ImageButton {

	public AddButton(Composite parent, String module) {
		super(parent, module, "Plus", "Add new entry");
	}

	@Override
	protected void proceed() {
		parent.getShell().dispose();
		switch (module) {
			case "Customer List":
				new CustomerView(new CustomerData(0));
				break;
			case "Target List":
				new SalesTargetView(new SalesTarget(0));
				break;
			case "Item List":
				new ItemView(new ItemData(0));
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

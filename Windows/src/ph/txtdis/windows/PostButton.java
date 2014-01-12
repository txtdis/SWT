package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class PostButton extends FocusButton {

	public PostButton(Composite parent, Report report) {
		super(parent, report, "Save", "Save " + report.getModule());
		getButton().setEnabled(false);
	}

	@Override
	protected void doWhenSelected() {
		Order order = (Order) report;

		switch (module) {
			case "Customer Data":
			case "Item Data":
			case "Sales Target":
			case "Delivery Report":
			case "Invoice":
			case "Purchase Order":
			case "Receiving Report":
			case "Remittance":
			case "Sales Order":
			case "Stock Take Tag":
				if (new PostingSwitch(order).wasSuccessful()) {
					getButton().setEnabled(false);
					parent.getShell().dispose();
					new OrderViewSwitch(order);
				}
				break;
			default:
				new ErrorDialog("No Post Button option\nfor " + module);
		}
	}
}

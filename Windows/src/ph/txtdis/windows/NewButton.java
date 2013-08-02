package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class NewButton extends ImageButton {

	public NewButton(Composite parent, String module) {
		super(parent, module, ("New" + (module.equals("Remittance16") ? "16"
				: "")),
				("Create New " + module + (module.equals("Stock Take") ? " Tag"
						: "")));
	}

	@Override
	protected void doWhenSelected() {
		if (!module.equals("Remittance16"))
			parent.getShell().dispose();
		switch (module) {
			case "Customer Data":
				new CustomerView(0);
				break;
			case "Delivery Report":
			case "Delivery Report ":
				new DeliveryView(0);
				break;
			case "Invoice":
			case "Invoice ":
				new InvoiceView(0);
				break;
			case "Item Data":
				new ItemView(0);
				break;
			case "Purchase Order":
				new PurchaseOrderView(0);
				break;
			case "Receiving Report":
				new ReceivingView(0);
				break;
			case "Remittance":
				new RemittanceView(0);
				break;
			case "Remittance16":
				new RemittanceDialog().open();
				break;
			case "Sales Order":
				new SalesOrderView(0);
				break;
			case "Sales Target":
				new ProgramView(0);
				break;
			case "Stock Take":
				new StockTakeView(0);
				break;
		}
	}
}

package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.Composite;

public class NewButton extends ImageButton {

	public NewButton(Composite parent, String module) {
		super(parent, module, "New", "Create new file");
	}

	@Override
	protected void proceed() {
		switch (module) {
			case "Customer Data":
				new CustomerView(new CustomerData(0));
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
				new PurchaseView(0);
				break;
			case "Receiving Report":
				new ReceivingView(0);
				break;
			case "Remittance":
				new RemitView(new RemitData(0));
				break;
			case "Sales Order":
				new SalesView(0);
				break;
			case "Sales Target":
				new SalesTargetView(0);
				break;
			case "Stock Take":
				Date date = Count.getLatestDate();
				if (!Count.isClosed(date)) {
					new ErrorDialog("Close, then reconcile last count\nbefore starting a new one.");
					break;
				}
				if (!Count.isReconciled(date)) {
					new ErrorDialog("Reconcile last count\nbefore starting a new one.");
					new CountVarianceView();
					break;
				}
			case "Stock Take Tag":
				new CountView(0);
				break;
			default:
				System.out.println(module + "@newbutton");
		}
	}
}

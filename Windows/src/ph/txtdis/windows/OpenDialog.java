package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Shell;

public class OpenDialog extends InputDialog {
	public OpenDialog(String module) {
		super(module);
	}

	@Override
	protected void setOkButtonAction() {
		String strId = text.getText();
		if (StringUtils.isBlank(strId))
			return;
		// retrieve report from id input
		int id = Integer.parseInt(strId);
		// check if id is in the system
		boolean hasId = false;

		switch (module) {
			case "Delivery Report":
			case "Delivery Report ":
				hasId = new OrderHelper(-id).isOnFile("");
				break;
			case "Customer Data":
			case "Customer ID":
				hasId = new Customer().isIdOnFile(id);
				module = "Customer ID";
				break;
			case "Item Data":
				hasId = new ItemHelper().getName(id).isEmpty() ? false : true;
				module = "Item ID";
				break;
			case "Incentive Program":
				hasId = new SalesTarget(id).getId() == 0 ? false : true;
				break;
			case "Purchase Order":
				int poId = new PurchaseOrder(id).getId();
				hasId = poId == 0 ? false : true;
				break;
			case "Receiving Report":
				int rrId = new Receiving(id).getId();
				hasId = rrId == 0 ? false : true;
				break;
			case "Remittance":
				hasId = new Remittance().isIdOnFile(id);
				break;
			case "Sales Order":
				int soId = new SalesOrder(id).getReferenceId();
				hasId = soId == 0 ? false : true;
				break;
			case "Stock Take":
			case "Stock Take Tag":
				hasId = new StockTake().isOnFile(id);
				break;
			default:
				System.out.println(module + "@opendialogtop");
				return;
		}

		if (!hasId) {
			text.getShell().dispose();
			new ErrorDialog("" + "Sorry, " + module + " #" + id + "\n" + "is not in our system.");
			return;
		} else {
			image.getImage().dispose();
			for (Shell shell : UI.DISPLAY.getShells())
				shell.dispose();
			switch (module) {
				case "Delivery Report":
				case "Delivery Report ":
					new DeliveryView(id);
					break;
				case "Customer ID":
					new CustomerView(id);
					break;
				case "Item ID":
					new ItemView(id);
					break;
				case "Receiving Report":
					new ReceivingView(id);
					break;
				case "Remittance":
					new RemittanceView(new Remittance(id));
					break;
				case "Purchase Order":
					new PurchaseOrderView(id);
					break;
				case "Sales Order":
					new SalesOrderView(id);
					break;
				case "Stock Take":
				case "Stock Take Tag":
					new StockTakeView(id);
					break;
				case "Incentive Program":
					new SalesTargetView(id);
					break;
				default:
					System.out.println(module + "@opendialogbottom");
			}
		}
	}

	@Override
	public void setName(String name) {
		this.name = "Open";
		module = module.equals("Sales Target") ? "Incentive Program" : module;
	}

	@Override
	public void setMessage(String message) {
		this.message = "Enter\n" + module + " #";
	}
}

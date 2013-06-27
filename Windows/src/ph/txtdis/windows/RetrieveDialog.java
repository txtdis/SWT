package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Shell;

public class RetrieveDialog extends InputDialog {
	public RetrieveDialog(String module) {
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
				hasId = new OrderHelper(-id).hasBeenUsed(""); 
				break;
			case "Customer Data": 
			case "Customer ID": 
				hasId = new CustomerHelper(id).isOnFile(); 
				module = "Customer ID";
				break;
			case "Item Data": 
				hasId = new ItemHelper().getName(id) != null ? true : false; 
				module = "Item ID";
				break;
			case "Incentive Program": 
				int programId = new Program(id).getProgramId();
				hasId = programId == 0 ? false : true;
				break;
			case "Purchase Order": 
				int poId = new PurchaseOrder(id).getId();
				hasId = poId == 0 ? false : true;
				break;
			case "Receiving Report": 
				hasId = new ReceivingHelper().hasId(id);
				break;
			case "Remittance": 
				hasId = new RemittanceHelper().isIdOnFile(id);
				break;
			case "Sales Order": 
				int soId = new SalesOrder(id).getSoId();
				hasId = soId == 0 ? false : true;
				break;
			case "Stock Take": 
				hasId = new StockTakeHelper().hasId(id);
				module = "Stock Take Tag";
				break;
			default: 
				new ErrorDialog("No Choice\nAvailable.");
		}

		if (!hasId) {
			new ErrorDialog("" +
					"Sorry, " + module + " #" + id + "\n" +
					"is not in our system.");
			text.setText("");
			return;
		} else {
			image.getImage().dispose();
			for (Shell shell : display.getShells()) 
				shell.dispose();
			switch (module) {
				case "Delivery Report": 
				case "Delivery Report ": 
					new DeliveryView(id); break;
				case "Customer ID":	new CustomerView(id); break;
				case "Item ID": new ItemView(id); break;
				case "Receiving Report": new ReceivingView(id); break;
				case "Remittance": new RemittanceView(id); break;
				case "Purchase Order": new PurchaseOrderView(id); break;
				case "Sales Order": new SalesOrderView(id); break;
				case "Stock Take Tag": new StockTakeView(id); break;
				case "Incentive Program": new ProgramView(id); break;
				default: new ErrorDialog("No Option for\nRetrieve Dialog.");
			}
		}
	}
	@Override
	public void setName(String name) {
		this.name = "Retrieve";
		module = module.equals("Sales Target") ? "Incentive Program" : module;
	}
	@Override
	public void setMessage(String message) {
			this.message = "Enter\n" + module + " #";
	}
}

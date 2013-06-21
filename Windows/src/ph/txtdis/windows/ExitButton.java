package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class ExitButton extends ImageButton {

	public ExitButton(Composite parent, String module) {
		super(parent, module, "GoOut", "Exit " + module);
	}

	@Override
	public void open(){
		parent.getShell().dispose();
		switch (module) {
			case "Customer":
			case "Inventory ":
			case "Transaction":
			case "Contact":
			case "Discrepancy Menu":
				new ListMenu();
				break;
			case "Inventory": 									
			case "Purchase Order": 				
			case "Receiving Report":
			case "Delivery Report":
			case "Stock Take":
			case "Stock Take ":
				new SupplyChainMenu();
				break;
			case "Pricelist": 				
			case "Sales Report":
			case "Invoice":
			case "Sales Order":
			case "Remittance":
				new SalesMenu();
				break;
			case "Receivables":
			case "Payables":
			case "Credit/Debit Memo":
			case "Value-Added Tax":
			case "Financials":
				new FinanceMenu();
				break;
			case "Purchasing Discrepancies": 
			case "Route Report": 
			case "Physical Count Discrepancies":
			case "Invoicing Discrepancies": 
			case "Collection Discrepancies": 
				new DiscrepancyMenu();
				break;
			case "Backup": 				
			case "Restore":
			case "Settings":
			case "SMS":
			case "Irregular Activities":
				new SystemsMenu();
				break;
			case "Listings Menu": 				
			case "Supply Chain Menu": 
			case "Sales Menu":
			case "Finance Menu":
			case "Systems Menu":
				new MainMenu();
				break;
		}
	}
}



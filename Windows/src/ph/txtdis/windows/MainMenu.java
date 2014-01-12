package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class MainMenu extends View {

	public MainMenu() {
		super();
		Composite composite = new Compo(shell, 5).getComposite();
		Report module = new PurchaseOrder(0);
		new ImgButton(composite, Type.PURCHASE, new PurchaseOrder());
		new ImgButton(composite, Type.RECEIVING, new Receiving());
		new ImgButton(composite, Type.INVENTORY, new Inventory());
		new ImgButton(composite, Type.COUNT, new StockTake());
		new ImgButton(composite, Type.DELIVERY, new Delivery());
		
		new ImgButton(composite, Type.CUSTOMER_LIST, new CustomerList());
		new ImgButton(composite, Type.SALES, new SalesOrder());
		new ImgButton(composite, Type.INVOICE, new Invoice());
		new ImgButton(composite, Type.REMIT, new Remittance(null));
		new ImgButton(composite, Type.RECEIVABLES, new Receivables());
		
		new ImgButton(composite, Type.ROUTE_REPORT, new LoadSettlement());
		new ImgButton(composite, Type.SALES_REPORT, new SalesReport());
		new ImgButton(composite, Type.FINANCE, new Vat());
		new ImgButton(composite, Type.BACKUP, new Backup());
		new ImgButton(composite, Type.RESTORE, new Restore());
	}
	
	public static void main(String[] args) {
		Database.getInstance().getConnection("marivic", "marvic", "mgdc_smis");
		Login.setGroup("sys_admin");
	    new MainMenu().show();
		Database.getInstance().closeConnection();
    }
}

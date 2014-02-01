package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class MainMenu extends View {
	
	public MainMenu() {
		Composite composite = new Compo(shell, 5).getComposite();
		new ImgButton(composite, Type.PURCHASE);
		new ImgButton(composite, Type.RECEIVING);
		new ImgButton(composite, Type.INVENTORY);
		new ImgButton(composite, Type.STOCKTAKE);
		new ImgButton(composite, Type.DELIVERY);
		
		new ImgButton(composite, Type.CUSTOMER);
		new ImgButton(composite, Type.SALES);
		new ImgButton(composite, Type.INVOICE);
		new ImgButton(composite, Type.REMIT);
		new ImgButton(composite, Type.RECEIVABLES);
		
		new ImgButton(composite, Type.SETTLEMENT);
		new ImgButton(composite, Type.SALES_REPORT);
		new ImgButton(composite, Type.FINANCE);
		new ImgButton(composite, Type.BACKUP);
		new ImgButton(composite, Type.RESTORE);
		
		proceed();
	}

	@Override
    protected void proceed() {
		show();
    }
}

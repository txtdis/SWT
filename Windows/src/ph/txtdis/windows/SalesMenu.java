package ph.txtdis.windows;

public class SalesMenu extends MainMenu {
	
	public SalesMenu() {
		super();
	}
	
	@Override
	protected void setNames() {
		name = "Sales"; 									
		a1 = "Price"; 				
		b1 = "Reports";
		a2 = "Sales Order";
		b2 = "Invoice";
		c2 = "Remittance";
	}
	
	@Override
	protected void setTitleBar() {
		new ModuleTitleBar(this, "Sales Menu").layButtons();
		;
	}
}

package ph.txtdis.windows;

public class DiscrepancyMenu extends MainMenu {

	public DiscrepancyMenu() {
		super();
	}
	
	@Override
	protected void setNames() {
		name = "Discrepancy"; 									
		a1 = "Purchasing"; 				
		b1 = "Receiving";
		a2 = "Physical Count";
		b2 = "Invoicing";
		c2 = "Collection";
	}
	
	@Override
	protected void setTitleBar() {
		new ModuleTitleBar(this, "Discrepancy Menu").layButtons();
	}
}

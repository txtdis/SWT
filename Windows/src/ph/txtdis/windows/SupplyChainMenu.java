package ph.txtdis.windows;

public class SupplyChainMenu extends MainMenu{
	public SupplyChainMenu() {
		super();
	}
	
	@Override
	protected void setNames() {
		name = "Supply Chain"; 									
		a1 = "Purchases"; 				
		b1 = "Receipts";
		a2 = "Inventory";
		b2 = "Shipment";
		c2 = "Stock Take";
	}
	
	@Override
	protected void setTitleBar() {
		new ModuleTitleBar(this, "Supply Chain Menu").layButtons();
	}
}

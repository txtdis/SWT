package ph.txtdis.windows;


public class ListMenu extends MainMenu {
	
	public ListMenu() {
		super();
	}
	
	@Override
	protected void setNames() {
		name = "Lists"; 									
		a1 = "Stock"; 				
		b1 = "Partner";
		a2 ="Transaction";
		b2 = "Contact";
		c2 = "Discrepancy";
	}
	
	@Override
	protected void setTitleBar() {
		new ModuleTitleBar(this, "Listings Menu").layButtons();
	}
}

package ph.txtdis.windows;

public class FinanceMenu extends MainMenu {

	public FinanceMenu() {
		super();
	}
	
	@Override
	protected void setTitleBar() {
		new ModuleTitleBar(this, "Finance Menu").layButtons();
	}
	
	@Override
	protected void setNames() { 
		name = "Finance"; 									
		a1 = "Account\nReceivables"; 				
		b1 = "Account\nPayables";
		a2 = "Credit/Debit\nMemos";
		b2 = "VAT\n ";
		c2 = "Discrepancy\n ";
	}
}

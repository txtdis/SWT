package ph.txtdis.windows;


public class SystemsMenu extends MainMenu {
		
	@Override
	protected void setNames() {
		name = "Systems"; 									
		a1 = "Backup"; 				
		b1 = "Restore";
		a2 = "Settings";
		b2 = "SMS";
		c2 = "Review";
	}
	
	@Override
	protected void setTitleBar() {
		new ModuleTitleBar(this, "Systems Menu").layButtons();
		;
	}
}

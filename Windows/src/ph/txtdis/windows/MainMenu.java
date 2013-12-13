package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class MainMenu extends View {

	protected String name;
	protected String a1, b1, a2, b2, c2;
	protected Button btnA1, btnB1, btnA2, btnB2, btnC2;
	protected Label lblA1, lblB1, lblA2, lblB2, lblC2;

	private Composite cmp1, cmp2;
	
	public MainMenu() {
		super();
		start();
		show();
	}
			
	protected void setNames() {
		name = "Main"; 									
		a1 = "Lists"; 				
		b1 = "Supply Chain";
		a2 = "Sales";
		b2 = "Finance";
		c2 = "Systems";
	}
	
	protected void setTitleBar() {
		new ModuleTitleBar(this, "Main Menu").layButtons();
	}
		
	protected void start() {
		setNames();
		setTitleBar();
		
		//First layer buttons
		cmp1 = new Composite(shell, SWT.NO_TRIM);
		cmp1.setLayout(new GridLayout(2, true));
		cmp1.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));				

		//Second layer buttons
		cmp2 = new Composite(shell, SWT.NO_TRIM);
		cmp2.setLayout(new GridLayout(3, true));
		cmp2.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));		
		
		new MenuButton(cmp1, a1);
		new MenuButton(cmp1, b1);
		new MenuButton(cmp2, a2);
		new MenuButton(cmp2, b2);
		new MenuButton(cmp2, c2);
	}	
		
	public static void main(String[] args) {
		Database.getInstance().getConnection("badette","013094","localhost");
		new MainMenu();
		Database.getInstance().closeConnection();
	}
}

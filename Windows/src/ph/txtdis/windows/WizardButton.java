package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class WizardButton extends ReportButton {

	public WizardButton(Composite parent, Data report) {
		super(parent, report, "Wizard", "Generate " + Type.PURCHASE_TARGET.getName());
	}
	
	@Override
	public void proceed(){
		new PurchaseTargetDialog();
	}
}
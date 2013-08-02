package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class WizardButton extends ReportButton {

	public WizardButton(Composite parent, Report report) {
		super(parent, report, "Wizard", "Generate " + report.getModule() );
	}
	
	@Override
	public void doWhenSelected(){
		switch (module) {
			case "Purchase Order":
				new PurchaseTargetDialog((PurchaseOrder) report);
				break;
			default:
				break;
		}
	}

}
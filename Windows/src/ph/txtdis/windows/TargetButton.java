package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class TargetButton extends ReportButton {

	public TargetButton(Composite parent, Data report) {
		super(parent, report, "Target", "Add new target/s");
	}

	@Override
	protected void proceed() {
		switch (module) {
			case "Sales Report":
				new SalesTargetListView();				
				break;
			case "Purchase Order":
				new PurchaseTargetView(null);
				break;
			default:
				break;
		}
	}
}


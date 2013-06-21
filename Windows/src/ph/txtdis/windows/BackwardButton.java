package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class BackwardButton extends DirectionalButton {
	public BackwardButton(Composite parent, Report report) {
		super(	parent, 
				report,
				"Backward",
				"Go back a " + (report.getModule() 
						== "Invoicing Discrepancies" ? "day" : "month")
				);
	}

	@Override
	protected void setIncrement() {
		increment = -1;
	}
}


package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class ForwardButton extends DirectionalButton {

	public ForwardButton(Composite parent, Report report) {
		super(parent, 
				report, 
				"Forward", 
				"Advance a " + report.getModule() == "Invoicing Discrepancies" 
				? "day" : "month");
	}

	@Override
	protected void setIncrement() {
		increment = 1;
	}
}


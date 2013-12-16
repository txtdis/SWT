package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class ForwardButton extends DirectionalButton {

	public ForwardButton(Composite parent, Report report) {
		super(parent, report, "Forward", "Advance a"
		        + (report.getModule().contains("Data") || report.getModule().equals("Remittance") ? "n ID#" : report.getModule().contains("Settlement") ? " day" : " month"));
	}

	@Override
	protected void setIncrement() {
		increment = 1;
	}
}

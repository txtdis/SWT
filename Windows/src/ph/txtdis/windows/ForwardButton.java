package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class ForwardButton extends DirectionalButton {

	public ForwardButton(Composite parent, Data report) {
		super(parent, report, "Forward", "Next");
	}
	
	@Override
	protected void setIncrement() {
		increment = 1;
	}
}

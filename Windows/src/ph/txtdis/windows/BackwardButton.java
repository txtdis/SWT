package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class BackwardButton extends DirectionalButton {
	public BackwardButton(Composite parent, Data data) {
		super(parent, data, "Backward", "Previous");
	}

	@Override
	protected void setIncrement() {
		increment = -1;
	}
}

package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class CheckButton {
	private Button checkButton;

	public CheckButton(Composite parent, String name, boolean isTrue) {
		checkButton = new Button(parent, SWT.CHECK);
		checkButton.setEnabled(false);
		checkButton.setSelection(isTrue);
		checkButton.setText(name);
	}


	public Button get() {
		return checkButton;
	}
}

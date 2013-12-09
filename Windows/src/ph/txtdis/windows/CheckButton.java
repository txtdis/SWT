package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class CheckButton {
	private Button checkButton;

	public CheckButton(Composite parent, String name, boolean isSelected) {
		checkButton = new Button(parent, SWT.CHECK | SWT.RIGHT_TO_LEFT);
		checkButton.setText(name);
		checkButton.setFont(UI.MONO);
		checkButton.setSelection(isSelected);
		checkButton.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				checkButton.setBackground(null);
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				checkButton.setBackground(UI.YELLOW);
			}
		});

		
	}


	public Button getButton() {
		return checkButton;
	}
}

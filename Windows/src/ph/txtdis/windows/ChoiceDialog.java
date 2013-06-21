package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;


public class ChoiceDialog extends DialogView {
	private boolean isOK;
	
	public ChoiceDialog(String msg) {
		super();
		setName("Error");
		setMessage(msg);
		open();
	}

	@Override
	protected void setButton() {
		final Button btnOK = new Button(getFooter(), SWT.PUSH);
		btnOK.setText("OK");
		btnOK.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				setOK(true);
				shell.dispose();
			}
		});

		final Button btnCancel = new Button(getFooter(), SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				setOK(false);
				shell.dispose();
			}
		});
	}

	public boolean isOK() {
		return isOK;
	}

	public void setOK(boolean isOK) {
		this.isOK = isOK;
	}
}

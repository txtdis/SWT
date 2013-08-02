package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public abstract class InputDialog extends DialogView {
	protected Text text;
	protected String module, input;

	public InputDialog(String module) {
		super();
		this.module = module;
		setName("");
		setMessage("");
		open();
	}

	@Override
	protected void setRightPane() {
		super.setRightPane();
		text = new Text(shell, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		text.setBackground(DIS.YELLOW);
	}

	@Override
	protected void setButton() {
		super.setButton();
		final Button btnCancel = new Button(getFooter(), SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				shell.dispose();
			}
		});
		text.setFocus();
	}

	@Override
	protected void setListener() {
		text.addListener (SWT.DefaultSelection, new Listener () {
			@Override
			public void handleEvent (Event e) {
				setOkButtonAction();
			}
		});
	}

	public String getInput() {
		return input;
	}
}

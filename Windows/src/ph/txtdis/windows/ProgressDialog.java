package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

public class ProgressDialog extends DialogView {
	public ProgressDialog() {
		this("Retrieving data...");
	}

	public ProgressDialog(String message) {
		super(null, message);
		addLabel(message);
		new ProgressBar(shell, SWT.HORIZONTAL | SWT.INDETERMINATE);
		center(); 
		new Thread() {
			public void run() {
				try {
					execute();
				} catch (Exception e) {
					dispose();
					new ErrorDialog(e);
				}
				dispose();
			}
		}.start();
		sleep();
	}

	private void addLabel(String message) {
	    Label label = new Label(shell, SWT.NONE);
	    label.setText(message);
	    label.setFont(UI.ITALIC);
    }

	protected void execute() throws Exception {
	};

	private void dispose() {
		if (!UI.DISPLAY.isDisposed())
			UI.DISPLAY.asyncExec(new Runnable() {
				public void run() {
					shell.dispose();
				}
			});
	}
}
package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class DataSwitcher {
	private Button now;
	private Control next;

	public DataSwitcher(Button button, Control control) {
		now = button;
		next = control;
		Listener cmbListener = new Listener () {
			@Override
			public void handleEvent (Event e) {
				switch (e.type) {
					case SWT.Traverse:
						if(e.detail != SWT.TRAVERSE_RETURN) break;
					case SWT.Selection:
						doWhenSelected();
						if(!now.isDisposed()) {
							now.setBackground(null);
							now.setEnabled(false);
						}
						if(next.getClass().equals(Text.class)) {
							next.setTouchEnabled(true);
						} else {
							next.setEnabled(true);
						}
						next.setFocus();
						break;
				}
			}
		};
		button.addListener (SWT.Selection, cmbListener);
		button.addListener (SWT.Traverse, cmbListener);
	}

	protected void doWhenSelected() {
	}

	protected void setNext(Control next) {
		this.next = next;
	}
}

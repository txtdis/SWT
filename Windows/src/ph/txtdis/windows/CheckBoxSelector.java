package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class CheckBoxSelector {
	private Control next;
	protected Button checkBox;

	public CheckBoxSelector(Button button, Control control) {
		checkBox = button;
		next = control;
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.Traverse:
					if (e.detail != SWT.TRAVERSE_RETURN)
						break;
				case SWT.Selection:
					doAfterSelection();
					if (next == null || next.isDisposed())
						break;
					next.setEnabled(true);
					next.setFocus();
					break;
				}
			}
		};
		button.addListener(SWT.Selection, listener);
		button.addListener(SWT.Traverse, listener);
	}

	protected void doAfterSelection() {
	}

	protected void setNext(Control next) {
		this.next = next;
	}
}

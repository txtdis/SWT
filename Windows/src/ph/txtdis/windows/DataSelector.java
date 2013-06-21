package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class DataSelector {
	private Combo now;
	private Control next;

	public DataSelector(Combo combo, Control control) {
		now = combo;
		next = control;
		Listener cmbListener = new Listener () {
			@Override
			public void handleEvent (Event e) {
				switch (e.type) {
				case SWT.Selection: 
				case SWT.DefaultSelection:
					act();
					if(!now.isDisposed()) {
						now.setEnabled(false);
						now.setBackground(View.white());
					}
					next.setEnabled(true);
					if(next.getClass().getSimpleName().equals("Text")) 
						next.setTouchEnabled(true);
					next.setFocus();
					break;
				}
			}
		};
		combo.addListener (SWT.Selection, cmbListener);
		combo.addListener (SWT.DefaultSelection, cmbListener);
	}
	
	protected void act() {
	}

	protected void setNext(Control next) {
		this.next = next;
	}
}

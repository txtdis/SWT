package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class ComboSelector {
	private Combo combo;
	private Control next;
	protected String selection;

	public ComboSelector(Combo combobox, Control control) {
		this.combo = combobox;
		next = control;
		Listener cmbListener = new Listener () {
			@Override
			public void handleEvent (Event e) {
				switch (e.type) {
				case SWT.Selection: 
				case SWT.DefaultSelection:
					selection = combo.getText();
					doAfterSelection();
					if (next == null || next.isDisposed()){
						return;
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
	
	protected void doAfterSelection() {
	}

	protected void setNext(Control next) {
		this.next = next;
	}
}

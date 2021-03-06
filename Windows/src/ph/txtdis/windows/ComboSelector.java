package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class ComboSelector {
	private Combo combo;
	private Control next;
	private Label label, nextLabel;
	protected String selection;

	public ComboSelector(ComboBox combobox, ComboBox nextCombobox) {
		this(combobox.getCombo(), combobox.getLabel(), nextCombobox.getCombo(), nextCombobox.getLabel());
	}

	public ComboSelector(ComboBox combobox, Control control) {
		this(combobox.getCombo(), combobox.getLabel(), control, null);
	}

	public ComboSelector(Combo combo, Control control) {
		this(combo, null, control, null);
	}

	public ComboSelector(Combo cmb, Label lbl, Control nextControl, Label nxtLbl) {
		combo = cmb;
		label = lbl;
		next = nextControl;
		nextLabel = nxtLbl;
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event e) {
					selection = combo.getText();
					processSelection();
					if (next == null || next.isDisposed())
						return;
					next.setEnabled(true);
					if (next.getClass().getSimpleName().equals("Text"))
						next.setTouchEnabled(true);
					next.setFocus();
					if (label != null)
						label.setBackground(null);
					if (nextLabel != null)
						nextLabel.setBackground(UI.YELLOW);
			}
		};
		combo.addListener(SWT.Selection, listener);
		combo.addListener(SWT.DefaultSelection, listener);
		combo.addListener(SWT.Traverse, listener);
	}

	protected void processSelection() {
	}

	protected void setNext(Control next) {
		this.next = next;
	}
}

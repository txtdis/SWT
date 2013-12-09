package ph.txtdis.windows;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

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
		combo.addTraverseListener(new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				handleEvent();
			}
		});
		
		combo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				handleEvent();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				handleEvent();
			}

		});
	}
	
	private void handleEvent () {
		selection = combo.getText();
		doAfterSelection();
		if (next == null || next.isDisposed()){
			return;
		}
		next.setEnabled(true);
		if(next.getClass().getSimpleName().equals("Text")) 
			next.setTouchEnabled(true);
		next.setFocus();
		if (label != null)
			label.setBackground(null);
		if (nextLabel != null)
			nextLabel.setBackground(UI.YELLOW);
	}

	protected void doAfterSelection() {
	}

	protected void setNext(Control next) {
		this.next = next;
	}
}

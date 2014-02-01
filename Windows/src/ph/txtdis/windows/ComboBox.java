package ph.txtdis.windows;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ComboBox {
	private Label label;
	private Combo combo;

	public ComboBox(Composite parent, String[] items) {
		setCombo(parent, items, 0);
	}

	public ComboBox(Composite parent, String[] items, int idx) {
		setCombo(parent, items, idx);
	}

	public ComboBox(Composite parent, String[] items, String name) {
		this(parent, items, name, 0);
	}
	
	public ComboBox(Composite parent, String[] items, String name, String selection) {
		this(parent, items, name, ArrayUtils.indexOf(items, selection));
	}

	public ComboBox(Composite parent, String[] items, String name, int idx) {
		label = new Label(parent, SWT.RIGHT);
		label.setText(name);
		label.setFont(UI.MONO);
		label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true, 1, 1));
		
		setCombo(parent, items, idx);
		combo.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				label.setBackground(null);
			}
			@Override
			public void focusGained(FocusEvent e) {
				label.setBackground(UI.YELLOW);
			}
		});
		
	}

	private void setCombo(Composite parent, String[] items, int idx) {
	    combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setItems(items);
		combo.setFont(UI.MONO);
		combo.select(idx < 0 ? 0 : idx);
    }

	public Label getLabel() {
		return label;
	}

	public Combo getCombo() {
		return combo;
	}
}

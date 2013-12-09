package ph.txtdis.windows;

import java.util.Arrays;

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
	private String[] names;

	public ComboBox(Composite parent, String[] items) {
		this(parent, items, null, null);
	}

	public ComboBox(Composite parent, String[] items, String name) {
		this(parent, items, name, null);
	}

	public ComboBox(Composite parent, String[] items, String name, String defaultItem) {
		if (name != null) {
			label = new Label(parent, SWT.RIGHT);
			label.setText(name);
			label.setFont(UI.MONO);
			label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true, 1, 1));
		}
		combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setItems(items);
		combo.setFont(UI.MONO);
		if (defaultItem == null) {
			combo.select(0);
		} else if(!defaultItem.isEmpty()){
			combo.select(Arrays.binarySearch(items, defaultItem));
		}
		if (name != null) 
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

	public Label getLabel() {
		return label;
	}

	public Combo getCombo() {
		return combo;
	}

	public String[] getNames() {
		return names;
	}
}

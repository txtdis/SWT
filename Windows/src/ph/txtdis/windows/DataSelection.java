package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class DataSelection {
	private Label label;
	private Combo combo;
	private int[] ids;
	private String[] names;

	public DataSelection(Composite cmp, String[] array, String name, String selection) {
		label = new Label(cmp, SWT.NONE);
		label.setText(name);
		label.setFont(View.monoFont());
		combo = new Combo(cmp, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setItems(array);
		combo.setFont(View.monoFont());
		int index = 0;
		combo.select(index);		
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

	public int[] getIds() {
		return ids;
	}
}

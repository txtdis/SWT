package ph.txtdis.windows;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class TableSelection {
	private Combo combo;

	public TableSelection(TableItem tableItem, int row, int col, String[] array, String selection) {
		this(tableItem, row, col);
		combo.setItems(array);
		combo.select(selection == null ? 0 : Arrays.binarySearch(array, selection));		
	}

	public TableSelection(TableItem tableItem, int row, int col) {
		Table table = tableItem.getParent();
		TableEditor editor = new TableEditor(table);
		combo = new Combo(table, SWT.READ_ONLY);
		combo.setEnabled(false);
		editor.grabHorizontal = true;
		editor.setEditor(combo, tableItem, col);
	}

	public Combo getCombo() {
		return combo;
	}
}

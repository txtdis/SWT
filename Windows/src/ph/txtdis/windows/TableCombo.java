package ph.txtdis.windows;

import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class TableCombo {
	private Combo combo;

	public TableCombo(TableItem tableItem, int columnIdx) {
		this(tableItem, columnIdx, new String[0], null);
	}
	
	public TableCombo(TableItem tableItem, int columnIdx, String[] comboItems) {
		this(tableItem, columnIdx, comboItems, null);		
	}


	public TableCombo(TableItem tableItem, int columnIdx, String[] comboItems, String defaultItem) {
		Table table = tableItem.getParent();
		TableEditor editor = new TableEditor(table);
		combo = new ComboBox(table, comboItems, "", defaultItem).getCombo();
		editor.grabHorizontal = true;
		editor.setEditor(combo, tableItem, columnIdx);
	}

	public Combo getCombo() {
		return combo;
	}
}

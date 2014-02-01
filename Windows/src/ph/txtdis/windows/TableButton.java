package ph.txtdis.windows;

import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class TableButton {
	private Button button;
	
	public TableButton(TableItem tableItem, int row, int column, String module) {
		Table table = tableItem.getParent();
		TableEditor editor = new TableEditor(table);
		button = new ListButton(table, module).getButton();
		editor.grabHorizontal = true;
		editor.setEditor(button, tableItem, column);
	}

	public Button getButton() {
		return button;
	}
}

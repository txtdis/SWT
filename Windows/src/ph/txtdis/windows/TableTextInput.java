package ph.txtdis.windows;

import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class TableTextInput {
	private Text text;

	public TableTextInput(TableItem tableItem, int columnIdx, Object initialData) {
		Table table = tableItem.getParent();
		TableEditor editor = new TableEditor(table);
		text = new TextInputBox(table, initialData).getText();
		text.setBackground(table.indexOf(tableItem) % 2 != 0 ? UI.GRAY : UI.WHITE);
		editor.grabHorizontal = true;
		editor.setEditor(text, tableItem, columnIdx);	
	}

	public Text getText() {
		return text;
	}
}

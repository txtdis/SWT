package ph.txtdis.windows;

import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class TableInput {
	private Text text;

	public TableInput(TableItem tableItem, int row, int column, Object object) {
		Table table = tableItem.getParent();
		TableEditor editor = new TableEditor(table);
		text = new DataEntry(table, object).getText();
		text.setBackground(row % 2 != 0 ? DIS.GRAY : DIS.WHITE);
		editor.grabHorizontal = true;
		editor.setEditor(text, tableItem, column);	
	}

	public Text getText() {
		return text;
	}
}

package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class TableCheckButton {
	private Button button;
	
	public TableCheckButton(TableItem tableItem, int row, int column) {
		Table table = tableItem.getParent();
		TableEditor editor = new TableEditor(table);
		button = new Button(table, SWT.CHECK);
		button.setBackground(row % 2 == 0 ? View.white() : View.gray());
		button.setText("OK");
		editor.grabHorizontal = true;
		editor.grabVertical = true;
		editor.horizontalAlignment = SWT.CENTER;
		editor.setEditor(button, tableItem, column);
	}

	public Button getButton() {
		return button;
	}
}

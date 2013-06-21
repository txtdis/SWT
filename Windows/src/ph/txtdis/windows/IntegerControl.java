package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class IntegerControl {
	protected int row;
	private Text current;
	private Control next;
	private TableItem tableItem;
	private int column;
	private String string;

	public IntegerControl(TableItem tableItem, final int column, final int row, final Text current, Control next) {
		this.tableItem = tableItem;
		this.column = column;
		this.row = row;
		this.next = next;
		this.current = current;
		new IntegerVerifier(current);
		current.addListener (SWT.DefaultSelection, new Listener () {
			@Override
			public void handleEvent (Event e) {
				string = current.getText();
				if (StringUtils.isBlank(string)) {
					if (row == 0 && column == 1) return;
					current.setText("");
				} else {
					if(verifyEntry(Integer.parseInt(string))) return;
					next();
				}
			}
		});
	}

	protected void clearEntry(String msg){
		new ErrorDialog(msg);
		current.setText("");
		current.setBackground(View.yellow());
	}

	protected boolean verifyEntry(int i) {
		return false;
	}

	protected void next() {
		tableItem.setText(column, string);
		next.setEnabled(true);
		next.setFocus();
	}
}
package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class TableLineItem {
	
	protected int row; 
	protected Table table;
	protected TableItem tableItem;
	protected TableEditor editor;	
	protected ReportView view;
	protected Report order;

	public TableLineItem(ReportView view, Report order, int row) {
		this(view.getTable(), order, row);
		this.view = view;
	}
	
	public TableLineItem(Table table, Report order, int row) {
		this.row = row;
		this.order = order;
		tableItem = new TableItem (table, SWT.NO_TRIM);
		tableItem.setBackground(row % 2 == 0 ? View.white() : View.gray());
		tableItem.setText(0, String.valueOf(row + 1));
		if(row > 9) table.setTopIndex(row - 9);
	}
	
	public TableItem getTableItem() {
		return tableItem;
	}

	public int getRow() {
		return row;
	}
}

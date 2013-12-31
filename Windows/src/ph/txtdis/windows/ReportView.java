package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class ReportView extends View {
	protected Report report;
	protected Table table;
	protected TableItem tableItem;

	protected void setProgress() {
		new ProgressDialog() {
			@Override
			public void proceed() {
				runClass();
			}
		};
	}

	protected void runClass() {
		report = new Report();
	}

	protected void setTitleBar() {
		new ReportTitleBar(this, report);
	}

	protected void setHeader() {
	}

	public Table getTable() {
		if (table == null)
			table = new ReportTable(this, report).getTable();
		return table;
	}

	public TableItem getTableItem() {
		return tableItem;
	}
	
	public TableItem getTableItem(int rowIdx) {
		int itemCount = table.getItemCount();
		if(itemCount >= rowIdx) {
			tableItem = new TableItem(table, SWT.NONE);
			tableItem.setBackground(rowIdx % 2 == 0 ? UI.WHITE : UI.GRAY);
			tableItem.setText(0, String.valueOf(rowIdx));
			table.setTopIndex(itemCount - 9);
		} else {
			tableItem = table.getItem(rowIdx);
		}
		return tableItem;
	}

	protected void setTotalBar() {
		new ReportTotal(this, report);
	}

	protected void setFooter() {
	}

	protected void showReport() {
		show();
	}

	protected void setListener() {
	}

	protected void setFocus() {
	}
}

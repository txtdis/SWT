package ph.txtdis.windows;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class ReportView extends View {
	protected Report report;
	protected Table table;
	protected TableItem tableItem;

	protected void setProgress() {
		ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor pm) {
				pm.beginTask("Preparing data...", IProgressMonitor.UNKNOWN);
				runClass();
				pm.done();
			}
		};
		try {
			pmd.run(true, false, runnable);
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
			//new ErrorDialog(e);
		}
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
		if(itemCount == rowIdx) {
			tableItem = new TableItem(table, SWT.NONE);
			tableItem.setBackground(rowIdx % 2 == 0 ? DIS.WHITE : DIS.GRAY);
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

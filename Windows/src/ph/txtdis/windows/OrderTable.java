package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class OrderTable {
	protected Table table;
	protected TableItem item;

	protected String[][] headers;
	protected Object[][] data;
	protected TableColumn col;
	protected String module;
	
	public OrderTable(ReportView view, Report report) {
		this(view, false, true, report);
	}

	public OrderTable(final ReportView view, final boolean withCheckBox, final boolean canSort, final Report order) {
		if (withCheckBox) {
			table = new Table (view.getShell(), SWT.CHECK | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | 
					SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		} else {
			table = new Table (view.getShell(), SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | 
					SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		}
		table.setLinesVisible (true);
		table.setHeaderVisible (true);
		table.setFont(View.monoFont());
		headers = order.getHeaders();
		data = order.getData();
		module = order.getModule();
		for (int i = 0; i < headers.length; i++) {
			col = new TableColumn (table, i);
			col.setText(headers[i][0]);
			col.setToolTipText("Click to Sort Ascending;\n Again for Descending.");
			switch (headers[i][1]) {
			case "Boolean": col.setAlignment(SWT.CENTER); break;
			case "String": col.setAlignment(SWT.LEFT); break;
			default: col.setAlignment(SWT.RIGHT); break;
			}
		} 
		for (int i = 0; i < table.getColumnCount() ; i++) {
			table.getColumn(i).pack();
		}
		table.setTopIndex(10);
		table.setLayout(new GridLayout());
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.CENTER;
		gd.grabExcessVerticalSpace = true;
		gd.heightHint = 270;
		table.setLayoutData(gd);
		if (canSort) new TableSorter(table, headers, module); 
		view.setTable(table);
		new StockTakeLineItem((StockTakeView) view, (StockTake) order, 0);

	}
}

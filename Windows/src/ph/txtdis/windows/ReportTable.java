package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ReportTable {
	protected Table table;
	protected TableItem tableItem;
	protected TableColumn col;

	private Data data;
	private SelectionListener tableSortListener;

	public ReportTable(ReportView view, Data data) {
		this(view.shell, data.getTableData(), data.getTableHeaders(), 0);
		this.data = data;
		enableSorting();
	}

	public ReportTable(Composite parent, final Object[][] tableData, final String[][] tableHeaders, int height) {
		table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL
		        | SWT.VIRTUAL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setFont(UI.MONO);
		for (int i = 0; i < tableHeaders.length; i++) {
			col = new TableColumn(table, i);
			col.setText(tableHeaders[i][0]);
			col.setToolTipText("Click to Sort Ascending;\n Again for Descending.");
			switch (tableHeaders[i][1]) {
			case "Boolean":
				col.setAlignment(SWT.CENTER);
				break;
			case "String":
				col.setAlignment(SWT.LEFT);
				break;
			default:
				col.setAlignment(SWT.RIGHT);
				break;
			}
		}

		table.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				if (tableData == null || tableData[0] == null || tableData[0][0] == null)
					return;
				tableItem = (TableItem) event.item;
				int rowIdx = table.indexOf(tableItem);
				if (rowIdx >= tableData.length)
					return;
				int colNum = 0;
				for (int i = 0; i < tableHeaders.length; i++) {
					if (tableData[rowIdx][i] == null) {
						tableItem.setText(colNum, "");
					} else {
						switch (tableHeaders[i][1]) {
						case "BigDecimal":
							BigDecimal bd = (BigDecimal) tableData[rowIdx][i];
							if (bd.compareTo(BigDecimal.ZERO) == 0) {
								tableItem.setText(colNum, "");
							} else {
								tableItem.setText(colNum, DIS.formatTo2Places(bd));
							}
							if (bd.compareTo(BigDecimal.ZERO) < 0)
								tableItem.setForeground(colNum, UI.RED);
							break;
						case "Quantity":
							BigDecimal qty = (BigDecimal) tableData[rowIdx][i];
							tableItem.setText(colNum, DIS.INTEGER.format(qty));
							if (qty.compareTo(BigDecimal.ZERO) < 0)
								tableItem.setForeground(colNum, UI.RED);
							break;
						case "UOM":
							tableItem.setText(colNum, DIS.FOUR_PLACE_DECIMAL.format(tableData[rowIdx][i]));
							break;
						case "ID":
						case "Line":
							int id = (int) tableData[rowIdx][i];
							String str = id == 0 ? "" : DIS.NO_COMMA_INTEGER.format(id);
							tableItem.setText(colNum, str);
							break;
						case "Boolean":
							boolean bool = (boolean) tableData[rowIdx][i];
							tableItem.setText(colNum, !bool ? "" : "OK");
							break;
						default:
							tableItem.setText(colNum, String.valueOf(tableData[rowIdx][i]));
						}
						if (data != null && data.getType() == Type.RECEIVABLES && colNum > 4)
							tableItem.setForeground(colNum, UI.RED);
					}
					colNum++;
				}
				tableItem.setBackground((rowIdx % 2) == 0 ? UI.WHITE : UI.GRAY);
			}
		});
		table.setItemCount(tableData == null ? 0 : tableData.length);
		for (TableColumn column : table.getColumns())
	        column.pack();
		table.setLayout(new GridLayout());
		height = height != 0 ? height : tableData == null || tableData.length < 10 ? 210 : table.getShell()
		        .getMonitor().getBounds().height - 385;
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.CENTER;
		gd.grabExcessVerticalSpace = true;
		gd.grabExcessHorizontalSpace = true;
		gd.heightHint = height;
		table.setLayoutData(gd);
	}

	protected void enableSorting() {
		tableSortListener = new TableSorter(table, data);
		for (TableColumn column : table.getColumns())
	        column.addSelectionListener(tableSortListener);
	}
	
	protected void disableSorting() {
		for (TableColumn column : table.getColumns())
	        column.removeSelectionListener(tableSortListener);		
	}

	public Table getTable() {
		return table;
	}
}
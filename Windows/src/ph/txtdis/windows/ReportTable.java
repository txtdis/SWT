package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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

	private Object[][] data;
	private String[][] headers;
	private String module;
	private int rowIdx, id;
	private int columnCount;
	private Report report;
	private ReportView reportView;
	private Order order;
	private OrderView orderView;

	public ReportTable(ReportView reportView, Report report) {
		this(reportView.shell, report.getData(), report.getHeaders(), report.getModule(), 0, true);
		this.report = report;
		this.reportView = reportView;
	}

	public ReportTable(Composite parent, Object[][] tableData, String[][] tableHeaders, String reportModule,
	        int height, boolean isWithHeaders) {
		data = tableData;
		headers = tableHeaders;
		module = reportModule;
		table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setFont(UI.MONO);
		for (int i = 0; i < headers.length; i++) {
			col = new TableColumn(table, i);
			col.setText(headers[i][0]);
			col.setToolTipText("Click to Sort Ascending;\n Again for Descending.");
			switch (headers[i][1]) {
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
				if (data == null || data[0] == null || data[0][0] == null)
					return;
				tableItem = (TableItem) event.item;
				rowIdx = table.indexOf(tableItem);
				if (rowIdx >= data.length)
					return;
				int colNum = 0;
				for (int i = 0; i < headers.length; i++) {
					if (data[rowIdx][i] == null) {
						tableItem.setText(colNum, "");
					} else {
						switch (headers[i][1]) {
							case "BigDecimal":
								BigDecimal bd = (BigDecimal) data[rowIdx][i];
								if (bd.compareTo(BigDecimal.ZERO) == 0) {
									tableItem.setText(colNum, "");
								} else {
									tableItem.setText(colNum, DIS.formatTo2Places(bd));
								}
								if (bd.compareTo(BigDecimal.ZERO) < 0)
									tableItem.setForeground(colNum, UI.RED);
								break;
							case "Integer":
								tableItem.setText(colNum, DIS.INTEGER.format(data[rowIdx][i]));
								break;
							case "UOM":
								tableItem.setText(colNum, DIS.FOUR_PLACE_DECIMAL.format(data[rowIdx][i]));
								break;
							case "ID":
								int id = (int) data[rowIdx][i];
								String str = id == 0 ? "" : DIS.NO_COMMA_INTEGER.format(id);
								tableItem.setText(colNum, str);
								break;
							case "Line":
								if (Integer.parseInt(data[rowIdx][i].toString()) == 0) {
									tableItem.setText(colNum, String.valueOf(rowIdx + 1));
								} else {
									tableItem.setText(colNum, String.valueOf(data[rowIdx][i]));
								}
								break;
							case "Quantity":
								BigDecimal qty = (BigDecimal) data[rowIdx][i];
								tableItem.setText(colNum, DIS.INTEGER.format(qty));
								if (qty.compareTo(BigDecimal.ZERO) < 0)
									tableItem.setForeground(colNum, UI.RED);
								break;
							case "Boolean":
								boolean bool = (boolean) data[rowIdx][i];
								tableItem.setText(colNum, !bool ? "" : "OK");
								break;
							default:
								tableItem.setText(colNum, String.valueOf(data[rowIdx][i]));
						}
						if (module.equals("Receivables") && colNum > 4) {
							tableItem.setForeground(colNum, UI.RED);
						}
					}
					colNum++;
				}
				tableItem.setBackground((rowIdx % 2) == 0 ? UI.WHITE : UI.GRAY);
			}
		});
		table.setItemCount(data == null ? 0 : data.length);
		columnCount = table.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			table.getColumn(i).pack();
		}
		table.setLayout(new GridLayout());
		height = height != 0 ? height : data == null || data.length < 10 ? 210 : table.getShell().getMonitor()
		        .getBounds().height - 385;
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.CENTER;
		gd.grabExcessVerticalSpace = true;
		gd.grabExcessHorizontalSpace = true;
		gd.heightHint = height;
		table.setLayoutData(gd);
		sortTable(true);
		doubleClickListener();
	}

	protected void sortTable(boolean sort) {
		if (sort)
			new TableSorter(table, headers, module);
	}

	protected void doubleClickListener() {
		table.addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TableItem tableItem = null;
				Point pt = new Point(event.x, event.y);
				int index = table.getTopIndex();
				int colIdx = -1;
				rowIdx = -1;
				while (index < table.getItemCount()) {
					tableItem = table.getItem(index);
					for (int i = 0; i < columnCount; i++) {
						Rectangle rect = tableItem.getBounds(i);
						if (rect.contains(pt)) {
							colIdx = i;
							rowIdx = index;
						}
					}
					index++;
				}
				if (colIdx < 0 || rowIdx < 0)
					return;
				// Get Row ID
				String strId = table.getItem(rowIdx).getText(module.equals("Remittance") ? 2 : 1);
				if (strId.isEmpty())
					strId = "0";
				id = Integer.parseInt(strId.replace("(", "-").replace(")", ""));
				// Get Column Text
				String colDatum;
				switch (module) {
					case "Customer List":
					case "Item List":
					case "Invoicing Discrepancies":
					case "Receivables":
					case "Receiving Report List":
					case "Cash Settlement":
					case "Remittance Settlement":
					case "Load Settlement":
					case "Sales Order List":
					case "Sales Report":
					case "Stock Take Tag List":
					case "Target List":
					case "Transmittal":
					case "Outlet List":
						new ModuleLauncher(report, id, colIdx);
						break;
					case "Overdue Statement":
					case "Value-Added Tax":
					case "Invoice/Delivery List":
						colDatum = table.getItem(rowIdx).getText(2);
						new ModuleLauncher(report, id, colDatum);
						break;
					case "Remittance":
						colDatum = table.getItem(rowIdx).getText(1);
						new ModuleLauncher(report, id, colDatum);
						break;
					case "Delivery Receipt":
					case "Purchase Order":
					case "Sales Order":
					case "Invoice":
					case "Stock Take":
						orderView = (OrderView) reportView;
						order = (Order) report;
						if (isPostingButtonEnabled(orderView, rowIdx)) {
							order.setRowIdx(rowIdx);
							new ItemIdInputSwitcher(orderView, order);
						}
						break;
					case "Stock Take ":
						colDatum = table.getItem(rowIdx).getText(3);
						new ModuleLauncher(report, id, colIdx, colDatum);
						break;
					default:
						new InfoDialog("Double-click");
				}
			}
		});
	}

	private boolean isPostingButtonEnabled(OrderView orderView, int rowIdx) {
		if (orderView.getPostButton() == null) {
			new ItemView(id);
			return false;
		}
		orderView.disposeAllTableWidgets(rowIdx);
		return true;
	}

	public Table getTable() {
		return table;
	}
}
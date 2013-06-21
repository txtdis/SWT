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
	protected TableItem tblItem;
	protected TableColumn col;

	private Object[][] data;
	private String[][] headers;
	private String module;
	private int rowNum;
	private int columnCount;
	private Report order;
	private ReportView view;

	public ReportTable(ReportView reportView, Report report) {
		this(	reportView.shell, 
				report.getData(), 
				report.getHeaders(), 
				report.getModule(), 
				0, 
				true);
		order = report;
		view = reportView;
		view.setTable(table);
	}

	public ReportTable(
			Composite cmp, 
			Object[][] d, 
			String[][] h, 
			String m, 
			int height, 
			boolean withHeaders
			) {
		data = d;
		headers = h;
		module = m;
		table = new Table (cmp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | 
				SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		table.setLinesVisible (true);
		table.setHeaderVisible (true);
		table.setFont(View.monoFont());
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
		table.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				if (data == null || data[0] == null || data[0][0] == null) 
					return;
				tblItem = (TableItem) event.item;
				rowNum = table.indexOf(tblItem);
				int colNum = 0;
				for (int i = 0; i < headers.length; i++) {
					if (data[rowNum][i] == null) {
						tblItem.setText(colNum, "");
					} else {
						switch (headers[i][1]) {
							case "BigDecimal":
								BigDecimal bd = (BigDecimal) data[rowNum][i];
								if (bd.compareTo(BigDecimal.ZERO) == 0) {
									tblItem.setText(colNum, "");
								} else {
									tblItem.setText(colNum, DIS.LNF.format(bd));
								}
								if (bd.compareTo(BigDecimal.ZERO) < 0) 
									tblItem.setForeground(colNum, View.red());
								break;
							case "Integer":
								tblItem.setText(colNum, DIS.LIF.format(data[rowNum][i]));
								break;
							case "UOM":
								tblItem.setText(colNum, DIS.XNF.format(data[rowNum][i]));
								break;
							case "ID":
								int id = (int) data[rowNum][i];
								String str = id == 0 ? "" : DIS.BIF.format(id);
								tblItem.setText(colNum, str);
								break;
							case "Line":
								if (Integer.parseInt(data[rowNum][i].toString()) == 0) {
									tblItem.setText(colNum, String.valueOf(rowNum + 1));
								} else {
									tblItem.setText(
											colNum, String.valueOf(data[rowNum][i]));
								}
								break;
							case "Quantity":
								BigDecimal qty = (BigDecimal) data[rowNum][i];
								tblItem.setText(colNum, DIS.LIF.format(qty));
								if (qty.compareTo(BigDecimal.ZERO) < 0) 
									tblItem.setForeground(colNum, View.red());
								break;
							case "Boolean":
								boolean bool = (boolean) data[rowNum][i];
								tblItem.setText(colNum, !bool ? "" : "OK");
								break;
							default:
								tblItem.setText(colNum, String.valueOf(data[rowNum][i]));
						}
						if (module.equals("Receivables") && colNum > 4) {
							tblItem.setForeground(colNum, View.red());
						}
					}
					colNum++;
				}
				tblItem.setBackground((rowNum % 2) == 0 ? View.white() : View.gray());
			}	
		});
		table.setItemCount(data == null ? 0 : data.length);
		columnCount = table.getColumnCount();
		for (int i = 0; i < columnCount ; i++) { 
			table.getColumn(i).pack();
		}
		table.setLayout(new GridLayout());
		table.setLayoutData(new GridData(
				GridData.CENTER, GridData.FILL, true, true, 1, 1));
		height = height != 0 ? height :
			data == null || data.length < 10 ? 210 : 
				table.getShell().getMonitor().getBounds().height - 390;
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.CENTER;
		gd.grabExcessVerticalSpace = true;
		gd.grabExcessHorizontalSpace = true;                   
		gd.heightHint = height;  
		table.setLayoutData(gd);
		sortTable(true);
		doubleClickListener();
	}

	protected void sortTable(boolean sort) {
		if (sort) new TableSorter(table, headers, module);
	}

	protected void doubleClickListener() {
		table.addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TableItem tableItem = null;
				Point pt = new Point(event.x, event.y);
				int index = table.getTopIndex();
				int colIdx = -1;
				int rowIdx = -1;
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
				if(colIdx < 0 || rowIdx < 0) return;
				// Get Row ID
				String strId = table.getItem(rowIdx).getText(
						module.equals("Remittance") ? 2 : 1) ;
				if(strId.isEmpty()) strId = "0";
				int id = Integer.parseInt(strId.replace("(", "-").replace(")", ""));
				// Get Column Text
				String colDatum;
				switch (module) {
					case "Customer List":
					case "Item List":
					case "Invoice":
					case "Invoice ":
					case "Invoicing Discrepancies":
					case "Receivables":
					case "Receiving Report List":
					case "Route Report":
					case "Sales Order List":
					case "Sales Report":
					case "Stock Take Tag List":
					case "Target List":
					case "Outlet List":
						new ModuleLauncher(order, id, colIdx);
						break;
					case "Overdue Statement":
					case "Value-Added Tax":
					case "Invoice/Delivery List":
						colDatum = table.getItem(rowIdx).getText(2);
						new ModuleLauncher(order, id, colDatum);
						break;
					case "Remittance":
						colDatum = table.getItem(rowIdx).getText(1);
						new ModuleLauncher(order, id, colDatum);
						break;
					case "Stock Take":
						StockTakeView stv = (StockTakeView) view;
						if(stv.getBtnPost() != null) {
							StockTake st = (StockTake) order;
							new StockTakeLineItem(stv, st, rowIdx);
						}
						break;
					case "Stock Take ":
						new ModuleLauncher(order, id, colIdx);
						break;
					default:
						new InfoDialog("Double-click");
				}
			}
		});
	}

	public Table getTable() {
		return table;
	}
}
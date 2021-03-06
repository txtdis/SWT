package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ReportTotal {
	protected Table table;
	protected TableItem item;

	protected String[][] headers;
	protected Object[][] tableData;
	protected Object[] totals;
	protected TableColumn col;
	
	public ReportTotal(ReportView view, Data order) {
		table = new Table (view.getShell(),  SWT.BORDER | SWT.VIRTUAL);
		table.setLinesVisible (true);
		table.setHeaderVisible (false);
		table.setFont(UI.MONO);
		headers = order.getTableHeaders();
		tableData = order.getTableData();
		totals = new Object[headers.length];

		for (int i = 0; i < headers.length; i++) {
			switch (headers[i][1]) {
			case "Long": 
				totals[i] = 0L; 
				break;
			case "Integer": 
				totals[i] = 0; 
				break;
			case "Quantity":
			case "BigDecimal": 
				totals[i] = BigDecimal.ZERO; 
				break;
			case "Date": 
				totals[i] = null; 
				break;
			default: 
				totals[i] = ""; 
				break;
			}
		}
		int length = tableData == null ? 0 : tableData.length;
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < headers.length; j++) {
				switch (headers[j][1]) {
				case "Long": 
					totals[j] = (Long) totals[j] + (Long) tableData[i][j]; 
					break;
				case "Integer": 
					totals[j] = (Integer) totals[j] + (Integer) tableData[i][j]; 
					break;
				case "Quantity":
				case "BigDecimal":
					if (tableData[i][j] != null)
						totals[j] =	((BigDecimal) totals[j]).add((BigDecimal) tableData[i][j]);
					break;
				default:
					totals[j] = "";
					break;
				}
			}
		}

		
		
		for (int i = 0; i < headers.length; i++) {
			col = new TableColumn (table, i);
			col.setText(headers[i][0]);
			switch (headers[i][1]) {
			case "Boolean": col.setAlignment(SWT.CENTER); break;
			case "String": col.setAlignment(SWT.LEFT); break;
			default: col.setAlignment(SWT.RIGHT); break;
			}
		} 
				item = new TableItem(table, SWT.NONE);
				int colNum = 0;
				for (int i = 0; i < headers.length; i++) {
					if (totals == null) break;
					if (totals[i] == null) {
						item.setText(colNum, "");
					} else {
						switch (headers[i][1]) {
						case "Quantity":
							BigDecimal qty = (BigDecimal) totals[i];
							item.setText(colNum, DIS.INTEGER.format(qty));
							if (qty.compareTo(BigDecimal.ZERO) < 0)
								item.setForeground(colNum, UI.RED);
							break;
						case "BigDecimal":
							BigDecimal bd = (BigDecimal) totals[i];
							item.setText(colNum, DIS.formatTo2Places(bd));
							if (bd.compareTo(BigDecimal.ZERO) < 0)
								item.setForeground(colNum, UI.RED);
							break;
						case "Integer":
							item.setText(colNum, DIS.INTEGER.format((Integer) totals[i]));
							break;
						case "Line":
							item.setText(colNum, "");
							break;
						default:
							item.setText(colNum, String.valueOf(totals[i]));
						}
						
						if (view.getType() == Type.RECEIVABLES && colNum > 4)
							item.setForeground(colNum, UI.RED);
						colNum++;
					}
				}
		for (int i = 0; i < table.getColumnCount() ; i++) 
			table.getColumn(i).pack();
		table.setLayout(new GridLayout());
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.CENTER;
		gd.verticalAlignment = GridData.END;
		gd.grabExcessVerticalSpace = true;
		gd.heightHint = 10;
		table.setLayoutData(gd);
	}
	
}
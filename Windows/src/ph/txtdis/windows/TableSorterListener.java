package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableSorterListener implements SelectionListener {
	private Data data;
	private Table table;
	private int updown = 1;

	public TableSorterListener(final Table table, Data data) {
		this.table = table;

		// Actual column sort listener
		Listener sortListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!(event.widget instanceof TableColumn))
					return;
				sortTable(table.indexOf((TableColumn) event.widget));
			}
		};

		// Add listener to each of the columns
		for (int i = 0; i < table.getColumnCount(); i++)
			table.getColumn(i).addListener(SWT.Selection, sortListener);

	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	void sortTable(final int columnIndex) {
		Comparator<TableItem> comparator = null;

		// String comparator
		Comparator<TableItem> strComparator = new Comparator<TableItem>() {
			@Override
			public int compare(TableItem t1, TableItem t2) {
				return t1.getText(columnIndex).compareTo(t2.getText(columnIndex)) * updown;
			}
		};

		// Numeric comparator
		Comparator<TableItem> numComparator = new Comparator<TableItem>() {
			@Override
			public int compare(TableItem t1, TableItem t2) {
				String s1 = t1.getText(columnIndex);
				String s2 = t2.getText(columnIndex);
				double i1 = s1.equals("") ? 0 : Double.parseDouble(s1.replace(",", "").replace("(", "-")
				        .replace(")", ""));
				double i2 = s2.equals("") ? 0 : Double.parseDouble(s2.replace(",", "").replace("(", "-")
				        .replace(")", ""));
				if (i1 < i2)
					return updown * 1;
				if (i1 > i2)
					return updown * -1;
				return 0;
			}
		};
		String[][] tableHeaders = data.getTableHeaders();
		switch (tableHeaders[columnIndex][1]) {
		case "String":
		case "Boolean":
		case "Date":
			comparator = strComparator;
			break;
		default:
			comparator = numComparator;
			break;
		}

		table.setRedraw(false);
		updown = (updown == 1 ? -1 : 1);
		TableItem[] tableItems = table.getItems();
		Arrays.sort(tableItems, comparator);

		for (int i = 0; i < tableItems.length; i++) {
			TableItem tblItem = new TableItem(table, SWT.NULL);
			for (int j = 0; j < table.getColumnCount(); j++) {
				String strItem = tableItems[i].getText(j);
				tblItem.setText(j, strItem);
				BigDecimal bdItem = BigDecimal.ZERO;
				if ((tableHeaders[j][1].contains("BigDecimal") || tableHeaders[j][1].contains("Quantity"))
				        && !strItem.isEmpty()) {
					bdItem = DIS.parseBigDecimal(strItem);
				}
				if (data.getType() == Type.RECEIVABLES && j > 4 || DIS.isNegative(bdItem)) {
					tblItem.setForeground(j, UI.RED);
				} else {
					tblItem.setForeground(j, UI.BLACK);
				}
			}
			tblItem.setChecked(tableItems[i].getChecked());
			tableItems[i].dispose();
			tblItem.setBackground(i % 2 == 0 ? UI.WHITE : UI.GRAY);
		}
		table.setRedraw(true);
	}
}

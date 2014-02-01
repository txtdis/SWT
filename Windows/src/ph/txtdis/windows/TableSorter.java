package ph.txtdis.windows;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableSorter implements SelectionListener {
	private Data data;
	private Table table;
	private int updown = 1;

	public TableSorter(final Table table, Data data) {
		this.table = table;
		this.data = data;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.widget instanceof TableColumn)
			sortTable(table.indexOf((TableColumn) e.widget));
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	void sortTable(final int colIdx) {
		Comparator<TableItem> comparator = null;

		Comparator<TableItem> strComparator = new Comparator<TableItem>() {
			@Override
			public int compare(TableItem t1, TableItem t2) {
				return t1.getText(colIdx).compareTo(t2.getText(colIdx)) * updown;
			}
		};

		// Numeric comparator
		Comparator<TableItem> numComparator = new Comparator<TableItem>() {
			@Override
			public int compare(TableItem t1, TableItem t2) {
				String s1 = t1.getText(colIdx);
				String s2 = t2.getText(colIdx);
				double i1 = DIS.parseDouble(s1);
				double i2 = DIS.parseDouble(s2);
				if (i1 < i2) {
					System.out.println(updown * 1);
					return updown * 1;
				}
				if (i1 > i2) {
					System.out.println(updown * -1);
					return updown * -1;
				}
				System.out.println(0);
				return 0;
			}
		};

		String[][] headers = data.getTableHeaders();
		switch (headers[colIdx][1]) {
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
		updown = updown == 1 ? -1 : 1;
		TableItem[] tableItems = table.getItems();
		Arrays.sort(tableItems, comparator);

		for (int i = 0; i < tableItems.length; i++) {
			TableItem tableItem = new TableItem(table, SWT.NULL);
			for (int j = 0; j < table.getColumnCount(); j++) {
				String textItem = tableItems[i].getText(j);
				tableItem.setText(j, textItem);
				tableItem.setForeground(j,
				        DIS.isNegative(textItem) || (data.getType() == Type.RECEIVABLES && j > 4) ? UI.RED : UI.BLACK);
			}
			tableItem.setChecked(tableItems[i].getChecked());
			tableItems[i].dispose();
			tableItem.setBackground(UI.setBackColor(i));
		}
		table.setRedraw(true);
	}
}

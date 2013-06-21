package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class InvoiceTable extends ReportTable {
	private int index;

	public InvoiceTable(final OrderView view, final Order order) {
		super(view, order);
		sortTable(false);
		if(order.getSumTotal().equals(BigDecimal.ZERO)) {
			table.addListener (SWT.MenuDetect, new Listener() {
				@Override
				public void handleEvent(Event event) {
					// check if out of bounds
					index = table.getSelectionIndex();
					if (index < 0) return; 
					// check if no data
					String strItemId = table.getItem(index).getText(1);
					if (strItemId.isEmpty()) return; 
					// get item ID
					final int itemId = Integer.parseInt(strItemId);
					Menu menu = new Menu(table);
					menu.setVisible(true);
					// Delete Menu Item
					MenuItem delete = new MenuItem(menu, SWT.NONE);
					delete.setText("Delete highlighted row");
					delete.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event event) {
							new OrderHelper(order.getId()).deleteRow(itemId);
						}
					});
					// Add Menu Item
					MenuItem add = new MenuItem(menu, SWT.NONE);
					add.setText("Append last row");
					add.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event event) {
							if(new OrderHelper(order.getId()).hasDetail()) {
								new InvoiceLineItem(view, order, order.getItemIds().size());
							}					
						}
					});
				}
			});
			table.setToolTipText("Right-click on any populated row\nto delete or add");
		} else {
			table.setToolTipText("" +
					"An imported invoice\n" +
					"from a printed S/O\n" +
					"cannot be edited.");
		}
	}
}
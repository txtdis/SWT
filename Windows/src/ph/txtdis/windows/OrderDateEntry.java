package ph.txtdis.windows;

import java.sql.Date;
import java.text.ParseException;
import java.util.Calendar;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class OrderDateEntry {
	private Text txtItemId;
	private String strPostDate, module;
	private Text txtPostDate, txtDueDate;
	private Date currentOrderDate, lastOrderDate;
	private OrderView view;
	private Order order;

	public OrderDateEntry(final ReportView reportView, Order o) {
		order = o;
		view = (OrderView) reportView;
		txtPostDate = view.getTxtPostDate();
		module = order.getModule();

		txtPostDate.addListener (SWT.DefaultSelection, new Listener () {
			@Override
			public void handleEvent (Event event) {
				txtPostDate = view.getTxtPostDate();
				txtDueDate = view.getTxtDueDate();
				txtItemId = view.getTxtItemId();
				strPostDate = txtPostDate.getText().trim();
				int id = order.getId();
				int lastId = id - 1;
				String series = order.getSeries();
				try {							
					currentOrderDate = new Date(DateUtils.truncate(
							new Date(DIS.DF.parse(strPostDate).getTime()),
							Calendar.DAY_OF_MONTH).getTime());
				} catch (ParseException e) {
					new ErrorDialog(e);
				}
				if(new OrderHelper(id).isIdStartOfBooklet(series)) 
					lastOrderDate = currentOrderDate;
				else
					lastOrderDate = new OrderHelper(lastId).getDate();
				if(module.contains("Invoice") && lastOrderDate.after(currentOrderDate)) {
					clearDate("Invoice date must on or after\n" +
							"preceding S/I#" + lastId + " date of " + lastOrderDate +".");
					return;
				}
				if(module.contains("Sales Order") && currentOrderDate.before(DIS.TODAY)) {
					clearDate("" +
							"S/O date cannot be\n" +
							"earlier than today.");
					return;
				}
				order.setPostDate(currentOrderDate); 
				txtDueDate.setText(new DateAdder(
						txtPostDate.getText()).add(order.getLeadTime()));
				next();
			}
		});
	}

	private void clearDate(String string){
		currentOrderDate = lastOrderDate;
		txtPostDate.setText(currentOrderDate.toString());
		new ErrorDialog(string);
		txtPostDate.setEditable(true);
		txtPostDate.setBackground(View.yellow());
		txtPostDate.selectAll();
		return;						
	}

	private void next() {
		txtPostDate.setTouchEnabled(false);
		if(txtItemId == null) {
			view.setTxtItemId(new InvoiceLineItem(
					view, order, order.getItemIds().size()).getTxtItemId());
			txtItemId = view.getTxtItemId();
		} 
		txtItemId.setTouchEnabled(true);
		txtItemId.setFocus();		
	}
}

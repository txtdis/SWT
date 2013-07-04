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
	private Text txtSoId, txtPostDate, txtDueDate, txtItemId;
	private String strPostDate, module;
	private Date currentOrderDate, lastOrderDate;
	private OrderView view;
	private Order order;

	public OrderDateEntry(final ReportView reportView, Order o) {
		order = o;
		view = (OrderView) reportView;
		txtPostDate = view.getTxtPostDate();
		txtSoId = view.getTxtSoId();
		module = order.getModule();

		txtPostDate.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				txtPostDate = view.getTxtPostDate();
				txtDueDate = view.getTxtDueDate();
				txtItemId = view.getTxtItemId();
				strPostDate = txtPostDate.getText().trim();
				int id = order.getId();
				int lastId = id - 1;
				String series = order.getSeries();
				int outletId = order.getPartnerId();
				try {
					currentOrderDate = new Date(DateUtils.truncate(
							new Date(DIS.DF.parse(strPostDate).getTime()),
							Calendar.DAY_OF_MONTH).getTime());
				} catch (ParseException e) {
					new ErrorDialog(e);
				}
				if (new OrderHelper(id).isIdStartOfBooklet(series))
					lastOrderDate = currentOrderDate;
				else
					lastOrderDate = new OrderHelper(lastId).getDate();
				if (module.contains("Invoice")) {
					if (lastOrderDate.after(currentOrderDate)) {
						clearDate("Invoice date must on or after\n"
								+ "preceding S/I#" + lastId + " date of "
								+ lastOrderDate + ".");
						return;
					} else if (currentOrderDate.after(DIS.SI_WITH_SO_CUTOFF)
							&& txtSoId.getText().trim().isEmpty()) {
						clearDate("S/O(P/O) # cannot be blank");
						txtSoId.setTouchEnabled(true);
						txtSoId.setFocus();
						return;
					}
				}
				if (module.contains("Sales Order")) {
					if (currentOrderDate.before(DIS.TODAY)) {
						clearDate("S/O date cannot be\n"
								+ "earlier than today.");
						return;
					} else if (new CustomerHelper().isExTruck(outletId)) {
						int soId = new OrderHelper().getSoId(currentOrderDate,
								outletId);
						if (soId != 0) {
							clearDate("Only one S/O per day is allowed:"
									+ "\n#" + soId + " is dated "
									+ DIS.SDF.format(currentOrderDate)
									+ ".\n\n"
									+ "If reason is unprinted receipt,\n"
									+ "manually copy system data to both\n"
									+ "Load Order and Sales Invoice forms.\n"
									+ "Invoicing process will not be changed.");
							return;
						}
					}
				}
				Calendar when = Calendar.getInstance();
				Calendar cal = Calendar.getInstance();
				cal.setTime(currentOrderDate);
				Calendar midMonth = Calendar.getInstance();
				midMonth.set(Calendar.DAY_OF_MONTH, 15);
				if (cal.after(midMonth)) {
					when.set(Calendar.DAY_OF_MONTH, 1);
				} else {
					when.set(Calendar.DAY_OF_MONTH,
							cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				}
				Date[] dates = { new Date(when.getTimeInMillis()) };
				Date date = new CalendarDialog(dates, false).getDate();
				when.setTime(date);
				if (!DateUtils
						.truncatedEquals(cal, when, Calendar.DAY_OF_MONTH)) {
					clearDate("Entered and clicked dates\n"
							+ "do not match; try again.");
					return;
				}
				order.setPostDate(currentOrderDate);
				txtDueDate.setText(new DateAdder(txtPostDate.getText())
						.add(new Credit().getTerm(order.getPartnerId(),
								currentOrderDate)));
				next();
			}
		});
	}

	private void clearDate(String string) {
		txtPostDate.setTouchEnabled(true);
		txtPostDate.setText(currentOrderDate.toString());
		new ErrorDialog(string);
		txtPostDate.setEditable(true);
		txtPostDate.setBackground(View.yellow());
		txtPostDate.selectAll();
		return;
	}

	private void next() {
		txtPostDate.setTouchEnabled(false);
		if (txtItemId == null) {
			view.setTxtItemId(new InvoiceLineItem(view, order, order
					.getItemIds().size()).getTxtItemId());
			txtItemId = view.getTxtItemId();
		}
		txtItemId.setTouchEnabled(true);
		txtItemId.setFocus();
	}
}

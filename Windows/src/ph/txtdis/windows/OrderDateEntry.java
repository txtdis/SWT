package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.util.Calendar;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class OrderDateEntry {
	private BigDecimal variance;
	private Button postButton;
	private Date currentOrderDate;
	private Order order;
	private OrderView view;
	private String strPostDate;
	private Text txtDueDate, txtPostDate, txtSoId;

	public OrderDateEntry(OrderView orderView, Order report) {
		order = report;
		view = orderView;
		txtPostDate = view.getTxtPostDate();
		txtSoId = view.getReferenceIdInput();
		postButton = view.getPostButton();

		txtPostDate.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				txtPostDate = view.getTxtPostDate();
				txtDueDate = view.getTxtDueDate();
				strPostDate = txtPostDate.getText().trim();
				int id = order.getId();
				int lastId = id - 1;
				String series = order.getSeries();
				int partnerId = order.getPartnerId();
				try {
					currentOrderDate = new Date(DateUtils.truncate(
					        new Date(DIS.POSTGRES_DATE.parse(strPostDate).getTime()), Calendar.DAY_OF_MONTH).getTime());
				} catch (ParseException e) {
					new ErrorDialog(e);
				}
				if (order.isAnSI()) {
					Date lastOrderDate = new OrderHelper(lastId).getDate();
					Date referenceDate = new OrderHelper().getReferenceDate(order.getReferenceId());
					if (new OrderHelper(id).isIdStartOfBooklet(series)) {
						lastOrderDate = currentOrderDate;
					} else if (lastOrderDate == null) {
						new ErrorDialog("Invoice #" + lastId + "\nmust be entered first");
						return;
					} else if (lastOrderDate.after(currentOrderDate)) {
						clearDate("Invoice date must on or after\npreceding S/I #" + lastId + " dated " + lastOrderDate
						        + ".");
						return;
					} else if (currentOrderDate.after(DIS.SI_WITH_SO_CUTOFF) && txtSoId.getText().trim().isEmpty()
					        && order.getEnteredTotal().signum() > -1) {
						clearDate("S/O(P/O) # cannot be blank");
						txtSoId.setTouchEnabled(true);
						txtSoId.setFocus();
						return;
					} else if (!DateUtils.isSameDay(currentOrderDate, referenceDate)) {
						clearDate("Invoice and S/O(P/O) dates\nmust be the same");
						return;
					}
				} else if (order.isAnSO()) {
					if (currentOrderDate.before(DIS.TODAY)) {
						clearDate("S/O date cannot be\nearlier than today.");
						return;
					}
					if (currentOrderDate.after(DIS.TOMORROW) && !DIS.isSunday(DIS.TOMORROW)) {
						clearDate("S/O date cannot be\nafter tomorrow, unless\nit is a Sunday.");
						return;
					}
					int routeId = new Route().getId(partnerId);
					DateAdder date = new DateAdder(currentOrderDate);
					Date[] dates = new Date[] {
					        DIS.CLOSURE_BEFORE_SO_CUTOFF,
					        DIS.isMonday(currentOrderDate) ? date.plus(-2) : date.plus(-1) };
					if (!areLoadedMaterialsBalanced(dates, routeId))
						return;
					if (!wereCollectiblesRemitted(dates, routeId))
						return;
					if (order.isForAnExTruck()) {
						int soId = new OrderHelper().getSoId(currentOrderDate, partnerId);
						if (soId != 0) {
							clearDate("Only one S/O per day is allowed:\n#" + soId + " is dated "
							        + DIS.STANDARD_DATE.format(currentOrderDate)
							        + ".\n\nIf reason is unprinted receipt,\n"
							        + "manually copy system data to both\nLoad Order and Sales Invoice forms.\n"
							        + "Invoicing process will not be changed.");
							return;
						}
					}
				}
				if (!new CalendarDialog(new Date[] { currentOrderDate }, false).isEqual()) {
					return;
				} else {
					System.out.println(currentOrderDate);
				}
				order.setDate(currentOrderDate);
				txtDueDate.setText(new DateAdder(txtPostDate.getText()).add(new Credit().getTerm(order.getPartnerId(),
				        currentOrderDate)));
				txtPostDate.setTouchEnabled(false);
				if (postButton != null)
					new ItemIdInputSwitcher(view, order);
			}
		});
	}

	private void clearDate(String string) {
		txtPostDate.setTouchEnabled(true);
		txtPostDate.setText(currentOrderDate.toString());
		new ErrorDialog(string);
		txtPostDate.setEditable(true);
		txtPostDate.setBackground(UI.YELLOW);
		txtPostDate.selectAll();
		return;
	}

	private boolean areLoadedMaterialsBalanced(Date[] dates, int routeId) {
		variance = new LoadedMaterialBalance(dates, routeId).getTotalVariance();
		if (variance.abs().compareTo(BigDecimal.ONE) < 1)
			return true;
		clearDate("There are " + DIS.CURRENCY_SIGN + DIS.TWO_PLACE_DECIMAL.format(variance)
		        + " still unaccounted;\ninput all previous and current transactions\nbefore continuing");
		txtPostDate.getShell().dispose();
		new LoadedMaterialBalanceView(dates, routeId);
		return false;
	}

	private boolean wereCollectiblesRemitted(Date[] dates, int routeId) {
		variance = new Remittance().getBalance();
		return true;
	}

}

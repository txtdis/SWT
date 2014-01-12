package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class OrderDateEntry {
	private Button postButton;
	private Date date;
	private CashSettlement cashSettlement;
	private RemittanceSettlement depositSettlement;
	private LoadSettlement loadSettlement;
	private Order order;
	private OrderView view;
	private String dateText;
	private Text dueDateDisplay, dateInput, txtSoId;

	public OrderDateEntry(OrderView orderView, Order report) {
		order = report;
		view = orderView;
		dateInput = view.getDateInput();
		txtSoId = view.getReferenceIdInput();
		postButton = view.getPostButton();

		dateInput.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				dateInput = view.getDateInput();
				dueDateDisplay = view.getDueDateDisplay();
				dateText = dateInput.getText().trim();
				int id = order.getId();
				int lastId = id - 1;
				String series = order.getSeries();
				int partnerId = order.getPartnerId();
				date = DIS.parseDate(dateText);
				if (order.isAnSI()) {
					Date lastOrderDate = new OrderHelper(lastId).getDate();
					Date referenceDate = new OrderHelper().getReferenceDate(order.getReferenceId());
					if (order.getEnteredTotal().signum() < 0)
						referenceDate = date;
					if (new OrderHelper(id).isIdStartOfBooklet(series)) {
						lastOrderDate = date;
					} else if (lastOrderDate == null) {
						new ErrorDialog("Invoice #" + lastId + "\nmust be entered first");
						return;
					} else if (lastOrderDate.after(date)) {
						clearDate("Invoice date must on or after\npreceding S/I #" + lastId + " dated " + lastOrderDate);
						return;
					} else if (date.after(DIS.SI_MUST_HAVE_SO_CUTOFF) && txtSoId.getText().trim().isEmpty()
					        && order.getEnteredTotal().signum() >= 0) {
						clearDate("S/O(P/O) # cannot be blank");
						txtSoId.setTouchEnabled(true);
						txtSoId.setFocus();
						return;
					} else if (!DateUtils.isSameDay(date, referenceDate)) {
						clearDate("Invoice and S/O(P/O) dates\nmust be the same");
						return;
					}
				} else if (order.isAnSO()) {
					if (DIS.TODAY.after(DIS.parseDate("2014-01-12")))
						if (date.before(DIS.TODAY)) {
							clearDate("S/O date cannot be\nearlier than today.");
							return;
						} else if (date.before(DIS.CLOSED_DSR_BEFORE_SO_CUTOFF)) {
							clearDate("S/O date cannot be\nearlier than " + DIS.CLOSED_DSR_BEFORE_SO_CUTOFF);
							return;
						}
					if (date.after(DIS.TOMORROW) && !DIS.isSunday(DIS.TOMORROW)) {
						clearDate("S/O date cannot be\nafter tomorrow, unless\nit is a Sunday.");
						return;
					}

					int routeId = new Route().getId(partnerId, DIS.TODAY);
					Date[] dates = new Date[] { DIS.CLOSED_DSR_BEFORE_SO_CUTOFF, date };
					if (DIS.TODAY.after(DIS.parseDate("2014-01-12")))
						if (!hasMaterialLoadBeenSettled(dates, routeId))
							return;
					if (!hasCashRemittanceBeenSettled(dates, routeId))
						return;
					dates = new Date[] { DIS.CLOSED_DSR_BEFORE_SO_CUTOFF, DIS.addDays(date, -7) };
					// if (!hasRemittanceBeenDeposited(dates, routeId))
					// return;
					if (order.isForAnExTruck()) {
						int soId = new OrderHelper().getSoId(date, partnerId);
						if (soId != 0) {
							clearDate("Only one S/O per day is allowed:\n#" + soId + " is dated "
							        + DIS.STANDARD_DATE.format(date) + ".\n\nIf reason is unprinted receipt,\n"
							        + "manually copy system data to both\nLoad Order and Sales Invoice forms.\n"
							        + "Invoicing process will not be changed.");
							return;
						}
					}
				}
				if (!new CalendarDialog(new Date[] { date }, false).isEqual())
					return;
				order.setDate(date);

				dueDateDisplay.setText((DIS.addDays(date, new Credit().getTerm(order.getPartnerId(), date)).toString()));
				dateInput.setTouchEnabled(false);
				if (postButton != null)
					new ItemIdInputSwitcher(view, order);
			}
		});
	}

	private void clearDate(String string) {
		dateInput.setTouchEnabled(true);
		dateInput.setText(date.toString());
		new ErrorDialog(string);
		dateInput.setEditable(true);
		dateInput.setBackground(UI.YELLOW);
		dateInput.selectAll();
		return;
	}

	private boolean hasMaterialLoadBeenSettled(final Date[] dates, final int routeId) {
		new ProgressDialog() {
			@Override
			public void proceed() {
				loadSettlement = new LoadSettlement(dates, routeId);
			}
		};
		BigDecimal variance = loadSettlement.getTotalVariance();
		if (variance.compareTo(BigDecimal.ZERO) != 0) {
			clearDate("There are " + DIS.CURRENCY_SIGN + DIS.formatTo2Places(variance)
			        + "\nworth of products still unaccounted");
			dateInput.getShell().dispose();
			new SettlementView(loadSettlement);
			return false;
		}
		return true;
	}

	private boolean hasCashRemittanceBeenSettled(final Date[] dates, final int routeId) {
		new ProgressDialog() {
			@Override
			public void proceed() {
				cashSettlement = new CashSettlement(dates, routeId);
			}
		};
		BigDecimal variance = cashSettlement.getTotalVariance();
		if (variance.compareTo(BigDecimal.ZERO) != 0) {
			clearDate("There are " + DIS.CURRENCY_SIGN + DIS.formatTo2Places(variance) + "\npayments still unremitted");
			dateInput.getShell().dispose();
			new SettlementView(cashSettlement);
			return false;
		}
		return true;
	}

	private boolean hasRemittanceBeenDeposited(final Date[] dates, final int routeId) {
		new ProgressDialog() {
			@Override
			public void proceed() {
				depositSettlement = new RemittanceSettlement(dates, routeId);
			}
		};
		BigDecimal variance = depositSettlement.getTotalVariance();
		if (variance.compareTo(BigDecimal.ZERO) != 0) {
			clearDate("There are " + DIS.CURRENCY_SIGN + DIS.formatTo2Places(variance)
			        + "\nremittance still undeposited");
			dateInput.getShell().dispose();
			new SettlementView(depositSettlement);
			return false;
		}
		return true;
	}
}

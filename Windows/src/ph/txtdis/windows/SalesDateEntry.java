package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

public class SalesDateEntry extends OrderDateEntry {
	private CashSettlement cashSettlement;
	private RemitSettlement depositSettlement;
	private LoadSettlement loadSettlement;

	public SalesDateEntry(OrderView orderView, OrderData orderData) {
		super(orderView, orderData);
	}

	
	@Override
	protected boolean isDateInputValid(final OrderData data, Date date) {
		int partnerId = data.getPartnerId();
		if (date.before(DIS.YESTERDAY)) {
			new ErrorDialog("S/O date cannot be\nearlier than today.");
			return false;
		}
		if (date.after(DIS.TOMORROW) && !DIS.isSunday(DIS.TOMORROW)) {
			new ErrorDialog("S/O date cannot be\nafter tomorrow, unless\nit is a Sunday.");
			return false;
		}
		if (data.isSO() || (data.isSI() && data.isForAnExTruck())) {
			BigDecimal overdue = new Overdue().getBalance(data.getPartnerId());
			data.setOverdue(overdue);
		}
		int routeId = Route.getId(partnerId, DIS.TODAY);
		Date[] dates = new Date[] { DIS.CLOSED_DSR_BEFORE_SO_CUTOFF, DIS.addDays(date, -1) };
		if (!hasMaterialLoadBeenSettled(dates, routeId))
			return false;
		if (!hasCashRemittanceBeenSettled(dates, routeId))
			return false;
		dates = new Date[] { DIS.CLOSED_DSR_BEFORE_SO_CUTOFF, DIS.addDays(date, -2) };
		if (!hasRemittanceBeenDeposited(dates, routeId))
			return false;
		if (data.isForAnExTruck()) {
			int soId = OrderControl.getSalesId(date, partnerId);
			if (soId != 0) {
				new ErrorDialog("Only one S/O per day is allowed:\n#" + soId + " is dated " + date
				        + ".\n\nIf reason is unprinted receipt,\n"
				        + "manually copy system data to both\nLoad Order and Sales Invoice forms.\n"
				        + "Invoicing process will not be changed.");
				return false;
			}
		}
		return true;
	}

	private boolean hasMaterialLoadBeenSettled(final Date[] dates, final int routeId) {
		loadSettlement = new LoadSettlement(dates, routeId);
		BigDecimal variance = loadSettlement.getTotalVariance();
		if (variance.compareTo(BigDecimal.ZERO) != 0) {
			new ErrorDialog("There are " + DIS.$ + DIS.formatTo2Places(variance)
			        + "\nworth of products still unaccounted");
			dateInput.getShell().close();
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
			new ErrorDialog("There are " + DIS.$ + DIS.formatTo2Places(variance)
			        + "\npayments still unremitted");
			dateInput.getShell().close();
			new SettlementView(cashSettlement);
			return false;
		}
		return true;
	}

	private boolean hasRemittanceBeenDeposited(final Date[] dates, final int routeId) {
		new ProgressDialog() {
			@Override
			public void proceed() {
				depositSettlement = new RemitSettlement(dates, routeId);
			}
		};
		BigDecimal variance = depositSettlement.getTotalVariance();
		if (variance.compareTo(BigDecimal.ZERO) != 0) {
			new ErrorDialog("There are " + DIS.$ + DIS.formatTo2Places(variance)
			        + "\nremit still undeposited");
			dateInput.getShell().close();
			new SettlementView(depositSettlement);
			return false;
		}
		return true;
	}

}

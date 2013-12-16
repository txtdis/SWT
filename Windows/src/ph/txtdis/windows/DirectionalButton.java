package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.eclipse.swt.widgets.Composite;

public abstract class DirectionalButton extends FocusButton {
	protected int increment;
	private Date[] dates;
	private Calendar start, end;

	public DirectionalButton(Composite parent, Report report, String icon, String tooltip) {
		super(parent, report, icon, tooltip);
	}

	@Override
	public void doWhenSelected() {
		setIncrement();
		if (module.contains("Data")  || module.equals("Remittance"))
			incrementIDs();
		else
			incrementDates();
	}

	private void incrementIDs() {
		int newId = report.getId() + increment;
		if (newId < 1 || !isIdOnFile(newId))
			return;
		doWhenIdOnFile(newId);
	}

	private boolean isIdOnFile(int newId) {
		switch (module) {
		case "Customer Data":
			return new Customer().isOnFile(newId);
		default:
			return false;
		}
	}

	private void doWhenIdOnFile(int newId) {
	    parent.getShell().dispose();
		new CustomerView(newId);
    }
	
	private void incrementDates() {
		start = Calendar.getInstance();
		end = Calendar.getInstance();
		dates = new Date[] { new Date(start.getTimeInMillis()), new Date(end.getTimeInMillis()) };
		switch (module) {
		case "Load-In/Out Settlement":
			LoadSettlement lmb = (LoadSettlement) report;
			dates = lmb.getDates();
			int routeId = lmb.getRouteId();
			incrementDaily();
			new LoadSettlementView(dates, routeId);
			break;
		case "Cash Settlement":
			CashSettlement cs = (CashSettlement) report;
			dates = cs.getDates();
			routeId = cs.getRouteId();
			incrementDaily();
			new CashSettlementView(dates, routeId);
			break;
		case "Invoicing Discrepancies":
			dates = ((InvoiceDiscrepancy) report).getDates();
			incrementDaily();
			new InvoiceDiscrepancyView(dates);
			break;
		case "Value-Added Tax":
			dates = ((Vat) report).getDates();
			incrementMonthly();
			new VatView(dates);
			break;
		case "Sales Report":
			SalesReport salesReport = (SalesReport) report;
			incrementMonthly();
			new SalesReportView(salesReport.getDates(), salesReport.getMetric(), salesReport.getCategoryId(),
			        salesReport.isPerRoute());
			break;
		}
	}

	protected void setIncrement() {
		increment = -1;
	}

	private void incrementMonthly() {
		end.setTime(dates[1]);
		end.add(Calendar.MONTH, increment);
		end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
		start.set(end.get(Calendar.YEAR), end.get(Calendar.MONTH), 1);
		dates[0] = new Date(start.getTimeInMillis());
		dates[1] = new Date(end.getTimeInMillis());
		parent.getShell().dispose();
	}

	private void incrementDaily() {
		start.setTime(dates[0]);
		end.setTime(dates[0]);
		start.add(Calendar.DAY_OF_MONTH, increment);
		end.add(Calendar.DAY_OF_MONTH, increment);
		dates[0] = new Date(start.getTimeInMillis());
		dates[1] = new Date(end.getTimeInMillis());
		parent.getShell().dispose();
	}
}

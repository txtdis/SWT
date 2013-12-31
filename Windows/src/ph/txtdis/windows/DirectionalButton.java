package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.eclipse.swt.widgets.Composite;

public abstract class DirectionalButton extends FocusButton {
	protected int increment, minId, maxId;
	private Date[] dates;
	private Calendar start, end;
	private String type;

	public DirectionalButton(Composite parent, Report report, String icon, String tooltip) {
		super(parent, report, icon, tooltip);
	}

	@Override
	public void doWhenSelected() {
		switch (module) {
		case "Customer Data":
			type = "customer";
			break;
		case "Item Data":
			type = "item";
			break;
		case "Remittance":
			type = "remittance";
			break;
		default:
			break;
		}
		setIncrement();
		if (module.contains("Data") || module.equals("Remittance")) {
			OrderHelper helper = new OrderHelper();
			maxId = helper.getMaxId(type);
			minId = helper.getMinId(type);
			incrementIDs(report.getId());
		} else
			incrementDates();
	}

	private void incrementIDs(int newId) {
		newId += increment;
		if (newId < 0)
			newId = 0;
		if (!isIdOnFile(newId))
			if (newId > maxId)
				newId = maxId;
			else if (newId < minId)
				newId = minId;
			else
				incrementIDs(newId);
		doWhenIdOnFile(newId);
	}

	private boolean isIdOnFile(int newId) {
		switch (module) {
		case "Customer Data":
			return new Customer().isIdOnFile(newId);
		case "Remittance":
			return new Remittance().isIdOnFile(newId);
		default:
			return false;
		}
	}

	private void doWhenIdOnFile(int newId) {
		parent.getShell().dispose();
		switch (module) {
		case "Customer Data":
			new CustomerView(newId);
		case "Remittance":
			new RemittanceView(newId);
		}
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
			new SettlementView(new LoadSettlement(dates, routeId));
			break;
		case "Cash Settlement":
			CashSettlement cs = (CashSettlement) report;
			dates = cs.getDates();
			routeId = cs.getRouteId();
			incrementDaily();
			new SettlementView(new CashSettlement(dates, routeId));
			break;
		case "Deposit/Transmittal Settlement":
			DepositSettlement rd = (DepositSettlement) report;
			dates = rd.getDates();
			routeId = rd.getRouteId();
			incrementDaily();
			new SettlementView(new DepositSettlement(dates, routeId));
			break;
		case "Invoicing Discrepancies":
			dates = report.getDates();
			incrementDaily();
			new InvoiceDiscrepancyView(dates);
			break;
		case "Value-Added Tax":
			dates = report.getDates();
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

package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.Composite;

public abstract class DirectionalButton extends FocusButton {
	protected int increment, minId, maxId;
	private Date[] dates;
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
				newId = minId;
			else if (newId < minId)
				newId = maxId;
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
			new RemittanceView(new Remittance(newId));
		}
	}

	private void incrementDates() {
		dates = new Date[] { DIS.TODAY, DIS.TODAY };
		switch (module) {
		case "Load Settlement":
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
		case "Remittance Settlement":
			RemittanceSettlement rd = (RemittanceSettlement) report;
			dates = rd.getDates();
			routeId = rd.getRouteId();
			incrementDaily();
			new SettlementView(new RemittanceSettlement(dates, routeId));
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
		dates[1] = DIS.addMonths(dates[1], increment);
		dates[0] = DIS.getFirstOfTheMonth(dates[1]);
		dates[1] = DIS.getLastOfTheMonth(dates[1]);
		parent.getShell().dispose();
	}

	private void incrementDaily() {
		if (increment < 0) {
			dates[1] = DIS.addDays(dates[0], increment);
			dates[0] = DIS.addDays(dates[0], increment);
		} else {
			dates[0] = DIS.addDays(dates[1], increment);
			dates[1] = DIS.addDays(dates[1], increment);
		}
		parent.getShell().dispose();
	}
}

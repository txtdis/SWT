package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.Composite;

public abstract class DirectionalButton extends ReportButton {
	protected int increment, minId, maxId;
	private Date[] dates;
	private Type type;

	public DirectionalButton(Composite parent, Data report, String icon, String tooltip) {
		super(parent, report, icon, tooltip);
	}

	@Override
	public void proceed() {
		switch (module) {
		case "Customer Data":
			type = Type.CUSTOMER;
			break;
		case "Item Data":
			type = Type.ITEM;
			break;
		case "Remittance":
			type = Type.REMIT;
			break;
		default:
			break;
		}
		setIncrement();
		if (module.contains("Data") || module.equals("Remittance")) {
			maxId = OrderControl.getMaximumId(type);
			minId = OrderControl.getMinimumId(type);
			incrementIDs(((InputData) data).getId());
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
			try {
	            Customer.getName(newId);
	            return true;
            } catch (Exception e) {
            	return false;
            }
		case "Remittance":
			return new RemitData().isIdOnFile(newId);
		default:
			return false;
		}
	}

	private void doWhenIdOnFile(int newId) {
		parent.getShell().dispose();
		switch (module) {
		case "Customer Data":
			new CustomerView(new CustomerData(newId));
		case "Remittance":
			new RemitView(new RemitData(newId));
		}
	}

	private void incrementDates() {
		dates = new Date[] { DIS.TODAY, DIS.TODAY };
		switch (module) {
		case "Load Settlement":
			LoadSettlement ls = (LoadSettlement) data;
			dates = ls.getDates();
			int routeId = ls.getRouteId();
			incrementDaily();
			new SettlementView(new LoadSettlement(dates, routeId));
			break;
		case "Cash Settlement":
			CashSettlement cs = (CashSettlement) data;
			dates = cs.getDates();
			routeId = cs.getRouteId();
			incrementDaily();
			new SettlementView(new CashSettlement(dates, routeId));
			break;
		case "Remittance Settlement":
			RemitSettlement rd = (RemitSettlement) data;
			dates = rd.getDates();
			routeId = rd.getRouteId();
			incrementDaily();
			new SettlementView(new RemitSettlement(dates, routeId));
			break;
		case "Value-Added Tax":
			dates = data.getDates();
			incrementMonthly();
			new FinanceView(dates);
			break;
		case "Sales Report":
			SalesReport sr = (SalesReport) data;
			incrementMonthly();
			new SalesReportView(sr.getDates(), sr.getMetric(), sr.getCategoryId(), sr.isPerRoute());
			break;
		}
	}

	protected void setIncrement() {
		increment = -1;
	}

	private void incrementMonthly() {
		dates[1] = DIS.addMonths(dates[1], increment);
		dates[0] = DIS.getFirstOfMonth(dates[1]);
		dates[1] = DIS.getLastOfMonth(dates[1]);
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

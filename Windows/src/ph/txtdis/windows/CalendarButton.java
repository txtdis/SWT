package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.Composite;

public class CalendarButton extends ReportButton {
	private Date[] dates;

	public CalendarButton(Composite parent, Data data) {
		super(parent, data, "Calendar", "Choose Date");
	}

	@Override
	protected void proceed() {
		dates = data.getDates();
		int routeId;
		switch (module) {
		case "Stock Take":
		case "Stock Take Tag":
			dates = new Date[] { data.getDate() };
			Date date = new CalendarDialog(dates).getDate();
			new CountReportView(date);
			break;
		case "Stock Take Reconciliation":
			dates = new Date[] { data.getDates()[1] };
			new CountReportView(new CalendarDialog(dates).getDate());
			break;
		case "Value-Added Tax":
			dates = data.getDates();
			new FinanceView(new CalendarDialog(dates).getDates());
			break;
		case "Cash Settlement":
			dates = data.getDates();
			routeId = ((CashSettlement) data).getRouteId();
			new SettlementView(new CashSettlement(new CalendarDialog(dates).getDates(), routeId));
			break;
		case "Remittance Settlement":
			dates = data.getDates();
			routeId = ((RemitSettlement) data).getRouteId();
			new SettlementView(new RemitSettlement(new CalendarDialog(dates).getDates(), routeId));
			break;
		case "Load Settlement":
			dates = data.getDates();
			routeId = ((LoadSettlement) data).getRouteId();
			new SettlementView(new LoadSettlement(new CalendarDialog(dates).getDates(), routeId));
			break;
		case "Sales Order":
			dates = new Date[] { DIS.TODAY };
			break;
		case "Sales Report":
			SalesReport salesReport = (SalesReport) data;
			dates = salesReport.getDates();
			new SalesReportView(new CalendarDialog(dates).getDates(), salesReport.getMetric(),
			        salesReport.getCategoryId(), salesReport.isPerRoute());
			break;
		default:
			new ErrorDialog("No option for\nCalendar Button");
			break;
		}
	}
}

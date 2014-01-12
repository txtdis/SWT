package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.Composite;

public class CalendarButton extends ReportButton {
	private Date[] dates;

	public CalendarButton(Composite parent, Report report) {
		super(parent, report, "Calendar", "Choose Date");
	}

	@Override
	protected void doWhenSelected() {
		int routeId;
		switch (module) {
		case "Stock Take":
		case "Stock Take Tag":
			dates = new Date[] { report.getDate() };
			new StockTakeView(new CalendarDialog(dates).getDate());
			break;
		case "Stock Take Reconciliation":
			dates = new Date[] { report.getDates()[1] };
			new StockTakeView(new CalendarDialog(dates).getDate());
			break;
		case "Value-Added Tax":
			dates = report.getDates();
			new VatView(new CalendarDialog(dates).getDates());
			break;
		case "Cash Settlement":
			dates = report.getDates();
			routeId = ((CashSettlement) report).getRouteId();
			new SettlementView(new CashSettlement(new CalendarDialog(dates).getDates(), routeId));
			break;
		case "Remittance Settlement":
			dates = report.getDates();
			routeId = ((RemittanceSettlement) report).getRouteId();
			new SettlementView(new RemittanceSettlement(new CalendarDialog(dates).getDates(), routeId));
			break;
		case "Load Settlement":
			dates = report.getDates();
			routeId = ((LoadSettlement) report).getRouteId();
			new SettlementView(new LoadSettlement(new CalendarDialog(dates).getDates(), routeId));
			break;
		case "Sales Order":
			dates = new Date[] { DIS.TODAY };
			break;
		case "Sales Report":
			SalesReport salesReport = (SalesReport) report;
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

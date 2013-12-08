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
		switch (module) {
			case "Stock Take":
				dates = new Date[] {
					((StockTake) report).getDate() };
				new StockTakeView(new CalendarDialog(dates).getDate());
				break;
			case "Stock Take ":
				dates = new Date[] {
					((StockTakeVariance) report).getDates()[1] };
				new StockTakeView(new CalendarDialog(dates).getDate());
				break;
			case "Irregular Activities":
				break;
			case "Value-Added Tax":
				dates = ((Vat) report).getDates();
				new VatView(new CalendarDialog(dates).getDates());
				break;
			case "Loaded Material Balance":
				dates = ((LoadedMaterialBalance) report).getDates();
				int routeId = ((LoadedMaterialBalance) report).getRouteId();
				new LoadedMaterialBalanceView(new CalendarDialog(dates).getDates(), routeId);
				break;
			case "Invoicing Discrepancies":
				dates = ((InvoiceDiscrepancy) report).getDates();
				new InvoiceDiscrepancyView(new CalendarDialog(dates).getDates());
				break;
			case "Sales Order":
				dates = new Date[] {DIS.TODAY};
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

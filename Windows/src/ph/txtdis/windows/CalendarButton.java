package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.eclipse.swt.widgets.Composite;

public class CalendarButton extends ReportButton {
	private Date[] dates;

	public CalendarButton(Composite parent, Report report) {
		super(parent, report, "Calendar", "Choose Date");
	}

	@Override
	protected void open() {
		Calendar cal = Calendar.getInstance();
		switch (module) {
			case "Stock Take":
				dates = new Date[] {((StockTake) report).getPostDate()};
				new StockTakeView(new CalendarDialog(dates).getDate());
				break;
			case "Stock Take ":
				dates = new Date[] {((StockTakeVariance) report).getDates()[1]};
				new StockTakeView(new CalendarDialog(dates).getDate());
				break;
			case "Irregular Activities":
				break;
			case "Value-Added Tax":
				dates = ((Vat) report).getDates();
				new VatView(new CalendarDialog(dates).getDates());
				break;
			case "Route Report":
				dates = ((ShippingMaterialBalance) report).getDates();
				int routeId = ((ShippingMaterialBalance) report).getRouteId();
				new ShippingMaterialBalanceView(new CalendarDialog(dates).getDates(), routeId);
				break;
			case "Invoicing Discrepancies":
				dates = ((InvoiceDiscrepancy) report).getDates();
				new InvoiceDiscrepancyView(new CalendarDialog(dates).getDates());
				break;
			case "Sales Order":
				dates = new Date[] {new Date(cal.getTimeInMillis())};
				//new SalesReportView(new CalendarDialog(dates));
				break;
			case "Sales Report":
				dates = ((SalesReport) report).getDates();
				String metric = ((SalesReport) report).getMetric();
				int cat = ((SalesReport) report).getCategoryId();
				int grp = ((SalesReport) report).getRouteOrOutlet();
				new SalesReportView(new CalendarDialog(dates).getDates(), metric, cat, grp);
				break;				
			default:
				new ErrorDialog("No option for\nCalendar Button");
				break;
		}
	}
}


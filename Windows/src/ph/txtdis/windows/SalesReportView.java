package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

public class SalesReportView extends ReportView implements Subheaderable {

	public SalesReportView() {
		this(null, "SALES TO TRADE", -10, false);
    }

	public SalesReportView(Date[] dates, String metric, int cat, boolean isPerRoute) {
		if (!new LoadVariance().isSettled())
			return;
		data = new SalesReport(dates, metric, cat, isPerRoute);
		addHeader();
		addSubheader();
		addTable();
		addTotalBar();
		show();
	}

	@Override
	protected void addHeader() {
		type = Type.SALES_REPORT;
		new Header(this, data) {
			@Override
			protected void layButtons() {
				String bizUnit = ((SalesReport) data).getCategoryId() == -10 ? "RM" : "Dry";
				new OptionButton(buttons, data);
				new TargetButton(buttons, data);
				new ImporterButton(buttons, module + " - " + bizUnit) {
					@Override
					protected void setStrings() {
						date = new Date(Calendar.getInstance().getTimeInMillis());
						msg = new String[] {
							"Import new MOR Template" };
						prefix = new String[] {
							"MOR" };
						info = module + "\ntemplate ";
					}
				};
				new ReportGenerationButton(buttons, data);
				new ReportButton(buttons, data, "Database", "Dump sales data to\na spreadsheet") {
					@Override
					protected void proceed() {
						String[] header = new String[] {
						        "OUTLET", "ROUTE", "STREET", "DISTRICT", "CITY", "PROVINCE", "INVOICE", "DATE", "SKU",
						        "PROD LINE", "CATEGORY", "QUANTITY" };
						new ExcelWriter(header, ((SalesReport) data).getDataDump());
					}
				};
				new BackwardButton(buttons, data);
				new CalendarButton(buttons, data);
				new ForwardButton(buttons, data);
				new ImgButton(buttons, Type.EXCEL, view);
			}
		};
	}

	@Override
    public void addSubheader() {
		new Subheading(shell, (Subheaded) data);
	}
}

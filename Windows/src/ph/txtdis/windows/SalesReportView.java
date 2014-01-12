package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

public class SalesReportView extends ReportView {
	private boolean isPerRoute;
	private int cat;
	private Date[] dates;
	private String metric;
	private SalesReport stt;

	public SalesReportView(Date[] dates, String metric, int cat, boolean isPerRoute) {
		this.dates = dates;
		this.metric = metric;
		this.cat = cat;
		this.isPerRoute = isPerRoute;
		setProgress();
		setTitleBar();
		setHeader();
		getTable();
		setTotalBar();
		setFooter();
		setListener();
		setFocus();
		showReport();
	}

	@Override
	protected void runClass() {
		report = stt = new SalesReport(dates, metric, cat, isPerRoute);
	}

	@Override
	protected void setTitleBar() {

		new ListTitleBar(this, stt) {
			@Override
			protected void layButtons() {
				String bizUnit = ((SalesReport) report).getCategoryId() == -10 ? "RM" : "Dry";
				new OptionButton(buttons, report);
				new TargetButton(buttons, report);
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
				new ReportGenerationButton(buttons, report);
				new ReportButton(buttons, report, "Database", "Dump sales data to\na spreadsheet") {
					@Override
					protected void doWithProgressMonitorWhenSelected() {
						String[] header = new String[] {
						        "OUTLET", "ROUTE", "STREET", "DISTRICT", "CITY", "PROVINCE", "INVOICE", "DATE", "SKU",
						        "PROD LINE", "CATEGORY", "QUANTITY" };
						new ExcelWriter(header, stt.getDataDump());
					}
				};
				new BackwardButton(buttons, report);
				new CalendarButton(buttons, report);
				new ForwardButton(buttons, report);
				new ExcelButton(buttons, report);
			}
		};
	}

	@Override
	protected void setHeader() {
		new ReportHeaderBar(shell, report);
	}

	public static void main(String[] args) {
		//Database.getInstance().getConnection("badette","013094","localhost");
		Database.getInstance().getConnection("badette","013094","192.168.1.100");
		new SalesReportView(null, "SALES TO TRADE", -10, false);
		Database.getInstance().closeConnection();
	}
}

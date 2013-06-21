package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

public class SalesReportView extends ReportView {
	private Date[] dates;
	private String metric;
	private int cat, grp;
	private SalesReport stt;

	public SalesReportView(Date[] dates, String metric, int cat, int grp) {
		this.dates = dates;
		this.metric = metric;
		this.cat = cat;
		this.grp = grp;
		setProgress();
		setTitleBar();
		setHeader();
		setTableBar();
		setTotalBar();
		setFooter();
		setListener();
		setFocus();
		showReport();
	}

	@Override
	protected void runClass() {
		report = stt = new SalesReport(dates, metric, cat, grp);
	}

	@Override
	protected void setTitleBar() {
		final String stmt = "" +
				"WITH latest\n" +
				"     AS (  SELECT customer_id, max (start_date) AS start_date\n" +
				"             FROM account\n" +
				"         GROUP BY customer_id),\n" +
				"     latest_route\n" +
				"     AS (SELECT account.customer_id, account.route_id\n" +
				"           FROM latest\n" +
				"                INNER JOIN account\n" +
				"                   ON     latest.customer_id = account.customer_id\n" +
				"                      AND latest.start_date = account.start_date)\n" +
				"  SELECT outlet.name AS outlet,\n" +
				"         route.name AS route,\n" +
				"         addy.street,\n" +
				"         barangay.name AS barangay,\n" +
				"         city.name AS city,\n" +
				"         province.name AS province,\n" +
				"         header.invoice_id AS invoice_id,\n" +
				"         header.invoice_date AS invoice_date,\n" +
				"         item.name AS item,\n" +
				"         prod_line.name AS product_line,\n" +
				"         category.name AS category,\n" +
				"         detail.qty * per_unit.qty / report.qty AS qty\n" +
				"    FROM invoice_header AS header\n" +
				"         INNER JOIN invoice_detail AS detail\n" +
				"            ON     header.invoice_id = detail.invoice_id\n" +
				"               AND header.series = header.series\n" +
				"               AND header.actual > 0\n" +
				"				AND header.invoice_date BETWEEN ? AND ?\n" +				
				"         INNER JOIN customer_master AS outlet\n" +
				"            ON header.customer_id = outlet.id\n" +
				"         LEFT OUTER JOIN item_tree AS prod_tree\n" +
				"            ON prod_tree.child_id = detail.item_id\n" +
				"         LEFT OUTER JOIN item_family AS prod_line\n" +
				"            ON prod_line.id = prod_tree.parent_id " +
				"				AND prod_line.tier_id = 3\n" +
				"         LEFT OUTER JOIN item_tree AS cat_tree\n" +
				"            ON cat_tree.child_id = prod_tree.parent_id\n" +
				"         LEFT OUTER JOIN item_family AS category\n" +
				"            ON category.id = cat_tree.parent_id " +
				"				AND category.tier_id = 2\n" +
				"         LEFT OUTER JOIN qty_per AS per_unit\n" +
				"            ON per_unit.uom = detail.uom " +
				"				AND per_unit.item_id = detail.item_id\n" +
				"         LEFT OUTER JOIN qty_per AS report\n" +
				"            ON report.item_id = detail.item_id " +
				"				AND report.report = TRUE\n" +
				"         LEFT OUTER JOIN latest_route " +
				"			 ON outlet.id = latest_route.customer_id\n" +
				"         LEFT OUTER JOIN route ON latest_route.route_id = route.id\n" +
				"         LEFT OUTER JOIN address AS addy\n" +
				"            ON addy.customer_id = header.customer_id\n" +
				"         LEFT OUTER JOIN area AS barangay " +
				"			 ON addy.district = barangay.id\n" +
				"         LEFT OUTER JOIN area AS city ON addy.city = city.id\n" +
				"         LEFT OUTER JOIN area AS province " +
				"			 ON addy.province = province.id\n" +
				"         LEFT OUTER JOIN item_master AS item " +
				"			 ON detail.item_id = item.id\n" +
				"ORDER BY outlet;\n";

		new ListTitleBar(this, stt) {
			@Override
			protected void layButtons() {
				String bu = ((SalesReport) report).getCategoryId() == -10 ? "RM" : "Dry";
				new OptionButton(buttons, report);
				new TargetButton(buttons, report);
				new ImportButton(buttons, module + " - " + bu) {
					@Override
					protected void setStrings() {
						date = new Date(Calendar.getInstance().getTimeInMillis());
						msg = new String[] {"Import new MOR Template"};
						prefix = new String[] {"MOR"};
						info = module + "\n" + "template ";
					}
				};
				new ReportGenerationButton(buttons, report);
				new ReportButton(buttons, report, "DataDump", "Dump sales data to\n" +
						"a spreadsheet") {
					@Override
					protected void go() {
						SalesReport sr = (SalesReport) report;
						Object[][] data = new SQL().getDataArray(sr.getDates(), stmt);
						String[] header = new String[] {
							"OUTLET",
							"ROUTE",
							"STREET",
							"DISTRICT",
							"CITY",
							"PROVINCE",
							"INVOICE",
							"DATE",
							"SKU",
							"PROD LINE",
							"CATEGORY",
							"QUANTITY"
						};
						new ExcelWriter(header, data);
					}
				};
				new BackwardButton(buttons, report);
				new CalendarButton(buttons, report);
				new ForwardButton(buttons, report);
				new ExcelButton(buttons, report);
				new ExitButton(buttons, module);
			}
		};
	}

	@Override
	protected void setHeader() {
		new ReportHeaderBar(shell, report);
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		new SalesReportView(null, "SALES TO TRADE", -10, 1);
		Database.getInstance().closeConnection();
	}
}

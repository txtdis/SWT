package ph.txtdis.windows;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class SalesOrderPrinting extends Printer {
	private SalesOrder order;
	private CustomerHelper helper;
	private String address;
	private Date postDate, dueDate;
	private BigDecimal subTotal = BigDecimal.ZERO;
	private BigDecimal total = BigDecimal.ZERO;
	private boolean isCustomerCopy;
	private boolean wasOverduePrinted = false;
	private boolean isExTruck = false;
	private int partnerId, salesId;

	public SalesOrderPrinting(Report report) {
		super(report);
	}

	@Override
	protected boolean print() {
		// Prepare Data
		order = (SalesOrder) report;
		partnerId = order.getPartnerId();
		helper = new CustomerHelper(partnerId);
		address = new Address(partnerId).getCityDistrict();
		postDate = order.getPostDate();
		dueDate = new DateAdder(postDate).plus(order.getLeadTime());
		isExTruck = helper.isExTruck(partnerId);
		salesId = order.getId();
		int loop = 2;
		int endOfLoop = loop - 1;
		// Write to Serial Port
		try {
			if (isExTruck) {
				printHeader();
				Object[][] overdueList = new SQL()
						.getDataArray(
								new Object[] {partnerId, DIS.OVERDUE_CUTOFF},
								""
										+ "WITH outlet\n"
										+ "     AS (SELECT customer_id AS id\n"
										+ "           FROM account AS a\n"
										+ "                INNER JOIN route AS r ON a.route_id = r.id\n"
										+ "                INNER JOIN customer_master AS cm ON cm.name = r.name\n"
										+ "          WHERE cm.id = ?)\n"
										+ "  SELECT cm.name, sum (balance)\n"
										+ "    FROM overdue AS od\n"
										+ "         INNER JOIN customer_master AS cm ON od.customer_id = cm.id\n"
										+ "         INNER JOIN outlet AS ol ON od.customer_id = ol.id\n"
										+ "   WHERE due_date >= ?\n"
										+ "GROUP BY cm.name\n"
										+ "ORDER BY cm.name;");
				for (Object[] outlets : overdueList) {
					ps.print(StringUtils.rightPad(outlets[0].toString(), 33));
					ps.println(StringUtils.leftPad(DIS.SNF.format(outlets[1]),
							9));
				}
				printFooter();
			}
			Object[][] addedItems = new SQL().getDataArray(order.getId(), "" +
					"WITH sales AS (SELECT ? AS id),\n" +
					"     booked\n" +
					"     AS (  SELECT sd.line_id,\n" +
					"                  sd.item_id AS id,\n" +
					"                  uom.unit,\n" +
					"                  qp.qty AS qty_per,\n" +
					"                  sum (sd.qty * qp.qty) AS qty\n" +
					"             FROM sales_header AS sh\n" +
					"                  INNER JOIN sales_detail AS sd ON sd.sales_id = sh.sales_id\n" +
					"                  INNER JOIN qty_per AS qp\n" +
					"                     ON sd.uom = qp.uom AND sd.item_id = qp.item_id\n" +
					"                  INNER JOIN uom ON uom.id = sd.uom\n" +
					"                  INNER JOIN sales AS s ON sd.sales_id = s.id\n" +
					"         GROUP BY sd.item_id,\n" +
					"                  uom.unit,\n" +
					"                  qp.qty,\n" +
					"                  line_id),\n" +
					"     counted\n" +
					"     AS (  SELECT cd.item_id AS id, sum (cd.qty * qp.qty) AS qty\n" +
					"             FROM count_detail AS cd\n" +
					"                  INNER JOIN count_header AS ch ON cd.count_id = ch.count_id\n" +
					"                  INNER JOIN location AS loc ON ch.location_id = loc.id\n" +
					"                  INNER JOIN qty_per AS qp\n" +
					"                     ON cd.uom = qp.uom AND cd.item_id = qp.item_id\n" +
					"                  INNER JOIN sales_header AS sh\n" +
					"                     ON ch.count_date = (sh.sales_date - 1)\n" +
					"                  INNER JOIN customer_master AS cm\n" +
					"                     ON cm.id = sh.customer_id AND loc.name = cm.name\n" +
					"                  INNER JOIN sales AS s ON s.id = sh.sales_id\n" +
					"         GROUP BY cd.item_id),\n" +
					"     to_load\n" +
					"     AS (SELECT b.line_id,\n" +
					"                b.qty - CASE WHEN c.qty IS NULL THEN 0 ELSE c.qty END AS qty,\n" +
					"                b.unit,\n" +
					"                b.qty_per\n" +
					"           FROM booked AS b LEFT JOIN counted AS c ON b.id = c.id)\n" +
					"  SELECT CASE WHEN qty % qty_per <> 0 THEN qty ELSE qty / qty_per END AS qty,\n" +
					"         CASE WHEN qty % qty_per <> 0 THEN uom.unit ELSE to_load.unit END\n" +
					"            AS unit\n" +
					"    FROM to_load INNER JOIN uom ON uom.id = 0\n" +
					"ORDER BY line_id\n" +
					"");
			total = BigDecimal.ZERO;
			subTotal = BigDecimal.ZERO;
			for (int counter = 0; counter < loop; counter++) {
				isCustomerCopy = counter == endOfLoop;
				printHeader();
				int previousItemBizUnit = (int) order.getData()[0][8];
				int i = 0;
				for (Object[] items : order.getData()) {
					int currentItemBizUnit = (int) items[8];
					if (isExTruck && !isCustomerCopy
							&& (currentItemBizUnit != previousItemBizUnit)) {
						printFooter();
						printHeader();
					}
					BigDecimal qty = (BigDecimal) items[4];
					String uom = (String) items[3];
					if (!isCustomerCopy) {
						qty = (BigDecimal) addedItems[i][0];
						uom = (String) addedItems[i][1];
						i++;
					}
					ps.print(StringUtils.leftPad(DIS.LIF.format(qty), 3));
					ps.print(uom + " ");
					ps.print(StringUtils.rightPad(items[7].toString(), 19));
					if (isCustomerCopy) {
						ps.print(StringUtils.leftPad(DIS.SNF.format(items[5])
								+ "@", 8));
						total = (BigDecimal) items[6];
						ps.print(StringUtils.leftPad(DIS.SNF.format(total), 9));
					} else {
						ps.print(StringUtils.leftPad("" + items[1], 4));
						ps.print(" _____  _____");
					}
					ps.println();
					subTotal = subTotal.add(total);
					previousItemBizUnit = currentItemBizUnit;
				}
				printFooter();
			}
			for (int i = 0; i < 10; i++) {
				int buffer = port.getOutputBufferSize();
				if (buffer == 0) {
					os.write(DLE);
					os.write(EOT);
					os.write(STATUS);
				}
				Thread.sleep(1000);
			}
			return new SalesOrderPrintOut(salesId).set();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			new ErrorDialog("Sales Order Printer:\n" + e);
			return false;
		}
	}

	private void printHeader() throws IOException {
		// Print logo
		printLogo();
		// Print receipt
		ps.println("");
		if (isCustomerCopy) {
			ps.println("DATE   : " + DIS.LDF.format(postDate));
			ps.println("DUE    : " + DIS.LDF.format(dueDate));
		} else {
			String msg = wasOverduePrinted ? "** " + DIS.LDF.format(postDate) + " **"
					: "NO DELIVERY TO THE FF:";
			os.write(ESC);
			os.write(EXCLAMATION);
			os.write(HUGE);
			ps.println(StringUtils.center(msg, 21));
			os.write(ESC);
			os.write(N);
			os.write(CHAR_PER_LINE);
			os.write(NARROW);
			os.write(ESC);
			os.write(AT);
			ps.println("");
		}
		if (isExTruck) {
			if (!wasOverduePrinted) {
				ps.println(StringUtils.leftPad("", 42, "-"));
				ps.println("        OUTLET                  OVERDUE");
			}
		} else {
			ps.println("SOLD TO: " + helper.getName());
			if (isCustomerCopy) {
				ps.println("ADDRESS: " + address);
				ps.println(StringUtils.leftPad("", 42, "-"));
				ps.println(StringUtils.center("PARTICULARS", 42));
			} else {
				ps.println(StringUtils.leftPad("", 42, "-"));
				ps.println("  QTY    DESCRIPTION     CODE  OUT    IN");
			}
		}
		ps.println(StringUtils.leftPad("", 42, "-"));
	}

	private void printFooter() {
		BigDecimal ZERO = BigDecimal.ZERO;
		BigDecimal dr1 = order.getDiscountRate1();
		BigDecimal dv1 = order.getTotalDiscount1();
		BigDecimal dr2 = order.getDiscountRate2();
		BigDecimal dv2 = order.getTotalDiscount2();
		String copy = isCustomerCopy ? "CUSTOMER COPY" : "WAREHOUSE/CHECKER COPY";
		if (isCustomerCopy) {
			ps.println(StringUtils.leftPad("--------", 42));
			ps.println(StringUtils.leftPad(DIS.SNF.format(subTotal), 42));
			ps.print(StringUtils.rightPad("VATABLE", 8));
			ps.print(StringUtils.leftPad(
					DIS.SNF.format(order.getTotalVatable()), 9));
			if (!dr1.equals(ZERO)) {
				ps.print(StringUtils.leftPad("LESS ", 11));
				ps.print(DIS.SNF.format(dr1) + "%");
				ps.println(StringUtils.leftPad("-" + DIS.SNF.format(dv1), 9));
			} else {
				ps.println(StringUtils.leftPad("--", 20));
			}
			ps.print(StringUtils.rightPad("VAT", 8));
			ps.print(StringUtils.leftPad(DIS.SNF.format(order.getTotalVat()), 9));
			if (!dr2.equals(ZERO)) {
				ps.print(StringUtils.leftPad("LESS ", 11));
				ps.print(DIS.SNF.format(dr2) + "%");
				ps.println(StringUtils.leftPad("-" + DIS.SNF.format(dv2), 9));
			} else {
				ps.println(StringUtils.leftPad("--", 20));
			}
			ps.println(StringUtils.leftPad("--------", 42));
			ps.print(StringUtils.leftPad("TOTAL", 33));
			ps.println(StringUtils.leftPad(DIS.SNF.format(order.getSumTotal()),
					9));
			ps.println(StringUtils.leftPad("========", 42));
			ps.println("");
			ps.print(StringUtils.rightPad("PREPARED BY:", 21));
			ps.println("RECEIVED BY:");
			ps.println("___________________    ___________________");
			String issuer = (String) new SQL().getDatum(""
					+ "SELECT cd.name || ' ' || cd.surname "
					+ "  FROM contact_detail AS cd "
					+ "INNER JOIN customer_master AS cm "
					+ "    ON cd.customer_id = cm.id "
					+ "INNER JOIN channel AS ch ON cm.type_id = ch.id "
					+ " WHERE cd.name = UPPER(CURRENT_USER) "
					+ "	AND ch.name = 'SELF';");
			;
			ps.print(StringUtils.center(issuer, 19));
			ps.print("    ");
			String receiver = new Contact().getName(partnerId);
			if (receiver == null) {
				receiver = helper.getName();
			} else {
				receiver = receiver.replace("  ", " ");
			}
			ps.println(StringUtils.center(receiver, 19));
		} else if (isExTruck && !wasOverduePrinted) {
			copy = "SALES COPY";
			wasOverduePrinted = true;
		} else {
			ps.println("");
			ps.println("LOAD-OUT:");
			ps.println("_____________  _____________  ____________");
			ps.println("  WAREHOUSE       CHECKER        TRUCK");
			ps.println("");
			ps.println("BACKLOAD:");
			ps.println("_____________  _____________  ____________");
			ps.println("    TRUCK         CHECKER      WAREHOUSE");
		}
		ps.println("");
		String so = "S/O# " + salesId;
		ps.print(so);
		ps.println(StringUtils.leftPad(copy, 42 - so.length()));
		ps.println(StringUtils.leftPad("", 42, "_"));
		ps.println();
		ps.println();
		ps.println();
		ps.println();
	}

	public static void main(String[] args) {
		// Database.getInstance().getConnection("sheryl", "10-8-91");
		Database.getInstance().getConnection("roland", "TIPON");
		View.black();
		Login.user = "roland";
		new SalesOrderPrinting(new SalesOrder(3264));
		Database.getInstance().closeConnection();
	}

}

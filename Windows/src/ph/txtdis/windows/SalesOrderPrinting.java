package ph.txtdis.windows;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class SalesOrderPrinting extends Printer {
	protected SalesOrder order;
	protected CustomerHelper customer;
	protected String partner, address, issuer, receiver, copy, soID;
	protected Date postDate;
	protected int partnerId, salesId;
	private Date dueDate;
	private String msg;
	private OrderHelper helper;
	private BigDecimal subTotal, total;
	private Object[][] netItemQtyToLoad;
	private boolean isCustomerCopy, wereOutletsWithOverduePrinted, isExTruck;
	private final static char LINES_PER_PAGE = 16;

	public SalesOrderPrinting(Report report) {
		super();
		helper = new OrderHelper();
		order = (SalesOrder) report;
		partnerId = order.getPartnerId();
		customer = new CustomerHelper(partnerId);
		partner = customer.getName();
		address = new Address(partnerId).getCityDistrict();
		postDate = order.getPostDate();
		salesId = order.getId();
		soID = "S/O #" + salesId;
		setPrinter();
	}

	@Override
	protected boolean print() throws IOException {
		// Prepare Data
		issuer = new Contact().getFullName();
		receiver = new Contact(partnerId).getFullName();
		receiver = receiver.trim().isEmpty() ? partner : receiver;
		isExTruck = order.isForAnExTruck();
		dueDate = new DateAdder(postDate).plus(order.getLeadTime());
		total = BigDecimal.ZERO;
		subTotal = BigDecimal.ZERO;
		netItemQtyToLoad = helper.getNetItemQtyToLoad(salesId);
		int loop = 2;
		int endOfLoop = loop - 1;
		int i, j, previousItemBizUnit, currentItemBizUnit, dataSize;
		boolean isEndOfPage, doLastTwoItemBizUnitsDiffer;
		Object[][] data;
		BigDecimal qty;
		String uom;
		
		// Write to Serial Port
		printOutletsWithOverdue();

		for (i = 0; i < loop; i++) {
			isCustomerCopy = (i == endOfLoop);
			printHeader();
			previousItemBizUnit = (int) order.getData()[0][8];
			data = order.getData();
			dataSize = data.length;
			for (j = 0; j < dataSize; j++) {
				currentItemBizUnit = (int) data[j][8];
				doLastTwoItemBizUnitsDiffer = currentItemBizUnit != previousItemBizUnit;
				isEndOfPage = (j % LINES_PER_PAGE) == 0;
				if (isCustomerCopy) {
					qty = (BigDecimal) data[j][4];
					uom = (String) data[j][3];
					if (isEndOfPage || doLastTwoItemBizUnitsDiffer) {
						printHeader();						
					}
				} else {
					qty = (BigDecimal) netItemQtyToLoad[j][0];
					uom = (String) netItemQtyToLoad[j][1];
					if (doLastTwoItemBizUnitsDiffer){
						printFooter();
						printHeader();
					}
				}
				ps.print(StringUtils.leftPad(DIS.INTEGER.format(qty), 3));
				ps.print(uom + " ");
				ps.print(StringUtils.rightPad(data[j][7].toString(), 19));
				if (isCustomerCopy) {
					ps.print(StringUtils.leftPad(DIS.NO_COMMA_DECIMAL.format(data[j][5]) + "@", 8));
					total = (BigDecimal) data[j][6];
					ps.print(StringUtils.leftPad(DIS.NO_COMMA_DECIMAL.format(total), 9));
				} else {
					ps.print(StringUtils.leftPad("" + data[j][1], 4));
					ps.print(" _____  _____");
				}
				ps.println();
				subTotal = subTotal.add(total);
				previousItemBizUnit = currentItemBizUnit;
			}
			printFooter();
		}
		waitForPrintingToEnd();
		return new SalesOrderPrintOut(salesId).set();
	}

	private void printOutletsWithOverdue() throws IOException {
	    if (isExTruck) {
			printHeader();
			System.out.println("partner: " + partner);
			System.out.println("postDate: " + postDate);
			for (Object[] outlets : new Overdue(partner, DIS.OVERDUE_CUTOFF).getRouteOutlets()) {
				ps.print(StringUtils.rightPad(outlets[0].toString(), 33));
				ps.println(StringUtils.leftPad(DIS.NO_COMMA_DECIMAL.format(outlets[1]), 9));
			}
			printFooter();
		}
    }

	private void printHeader() throws IOException {
		// Print logo
		printLogo();
		// Print receipt
		if (isCustomerCopy) {
			ps.println("DATE   : " + DIS.LONG_DATE.format(postDate));
			ps.println("DUE    : " + DIS.LONG_DATE.format(dueDate));
		} else {
			if (!isExTruck || wereOutletsWithOverduePrinted) {
				msg = "** " + DIS.LONG_DATE.format(postDate) + " **";
			} else {
			    msg = "NO DELIVERY TO THE FF";
			}
			printHuge();
			ps.println(StringUtils.center(msg, LINES_PER_PAGE/2));
			printNormal();
		}
		if (isExTruck) {
			if (!wereOutletsWithOverduePrinted) {
				printDash();
				ps.println("        OUTLET                  OVERDUE");
			} else {
				ps.println("LOAD TO: " + partner);
			}
		} else {
			ps.println("SOLD TO: " + partner);
			if (isCustomerCopy) {
				ps.println("ADDRESS: " + address);
				printDash();
				ps.println(StringUtils.center("PARTICULARS", COLUMN_WIDTH));
			} else {
				printDash();
				ps.println("  QTY    DESCRIPTION     CODE  OUT    IN");
			}
		}
		ps.println(StringUtils.leftPad("", COLUMN_WIDTH, "-"));
	}

	private void printFooter() {
		BigDecimal discountRate1 = order.getDiscountRate1();
		BigDecimal totalDiscount1 = order.getTotalDiscount1();
		BigDecimal discountRate2 = order.getDiscountRate2();
		BigDecimal totalDiscount2 = order.getTotalDiscount2();
		copy = isCustomerCopy ? "CUSTOMER COPY" : "WAREHOUSE/CHECKER COPY";
		if (isCustomerCopy) {
			ps.println(StringUtils.leftPad("--------", COLUMN_WIDTH));
			ps.println(StringUtils.leftPad(DIS.NO_COMMA_DECIMAL.format(subTotal), COLUMN_WIDTH));
			ps.print(StringUtils.rightPad("VATABLE", 8));
			ps.print(StringUtils.leftPad(DIS.NO_COMMA_DECIMAL.format(order.getTotalVatable()), 9));
			if (!discountRate1.equals(BigDecimal.ZERO)) {
				ps.print(StringUtils.leftPad("LESS ", 11));
				ps.print(DIS.NO_COMMA_DECIMAL.format(discountRate1) + "%");
				ps.println(StringUtils.leftPad("-" + DIS.NO_COMMA_DECIMAL.format(totalDiscount1), 9));
			} else {
				ps.println(StringUtils.leftPad("--", 20));
			}
			ps.print(StringUtils.rightPad("VAT", 8));
			ps.print(StringUtils.leftPad(DIS.NO_COMMA_DECIMAL.format(order.getTotalVat()), 9));
			if (!discountRate2.equals(BigDecimal.ZERO)) {
				ps.print(StringUtils.leftPad("LESS ", 11));
				ps.print(DIS.NO_COMMA_DECIMAL.format(discountRate2) + "%");
				ps.println(StringUtils.leftPad("-" + DIS.NO_COMMA_DECIMAL.format(totalDiscount2), 9));
			} else {
				ps.println(StringUtils.leftPad("--", 20));
			}
			ps.println(StringUtils.leftPad("--------", COLUMN_WIDTH));
			ps.print(StringUtils.leftPad("TOTAL", 33));
			ps.println(StringUtils.leftPad(DIS.NO_COMMA_DECIMAL.format(order.getComputedTotal()), 9));
			ps.println(StringUtils.leftPad("========", COLUMN_WIDTH));
			ps.println();
			ps.println("    PREPARED BY:          RECEIVED BY:");
			ps.println("___________________    ___________________");
			ps.println(StringUtils.center(issuer, 21) + StringUtils.center(receiver, 21));
		} else if (isExTruck && !wereOutletsWithOverduePrinted) {
			copy = "SALES COPY";
			wereOutletsWithOverduePrinted = true;
		} else {
			ps.println();
			ps.println("LOAD-OUT:");
			ps.println("_____________  _____________  ____________");
			ps.println("  WAREHOUSE       CHECKER        TRUCK");
			ps.println();
			ps.println("BACKLOAD:");
			ps.println("_____________  _____________  ____________");
			ps.println("    TRUCK         CHECKER      WAREHOUSE");
		}
		ps.println("");
		String soID = "S/O #" + salesId;
		ps.print(soID);
		ps.println(StringUtils.leftPad(copy, COLUMN_WIDTH - soID.length()));
		printPageEnd();
	}

	public static void main(String[] args) {
		// Database.getInstance().getConnection("sheryl", "10-8-91");
		Database.getInstance().getConnection("roland", "TIPON");
		Login.setUser("roland");
		new SalesOrderPrinting(new SalesOrder(3264));
		Database.getInstance().closeConnection();
	}
}

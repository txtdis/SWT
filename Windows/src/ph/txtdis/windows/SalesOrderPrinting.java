package ph.txtdis.windows;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class SalesOrderPrinting extends Printer {
	protected SalesData order;
	protected String partner, address, issuer, receiver, copy, soID;
	protected Date postDate;
	protected int partnerId, salesId;
	private Date dueDate;
	private String msg;
	private BigDecimal subTotal, total;
	private Object[][] netItemQtyToLoad;
	private boolean isCustomerCopy, wereOutletsWithOverduePrinted, isExTruck;
	private final static char LINES_PER_PAGE = 16;

	public SalesOrderPrinting(Data report) {
		super();
		order = (SalesData) report;
		partnerId = order.getPartnerId();
		partner = Customer.getName(partnerId);
		address = new Address(partnerId).getCityDistrict();
		postDate = order.getDate();
		salesId = order.getId();
		soID = "S/O #" + salesId;
		setPrinter();
	}

	@Override
	protected boolean print() throws IOException {
		// Prepare Data
		issuer = "";
		receiver = new Contact().getFullName(partnerId);
		receiver = receiver.trim().isEmpty() ? partner : receiver;
		isExTruck = order.isForAnExTruck();
		dueDate = DIS.addDays(postDate, order.getLeadTime());
		total = BigDecimal.ZERO;
		subTotal = BigDecimal.ZERO;
		netItemQtyToLoad = OrderControl.getNetItemQtyToLoad(salesId);
		int loop = 2;
		int endOfLoop = loop - 1;
		int i, j, previousItemBizUnit, currentItemBizUnit, dataSize;
		boolean isEndOfPage, doLastTwoItemBizUnitsDiffer;
		Object[][] data = order.getTableData();
		BigDecimal qty;
		String uom;
		
		// Write to Serial Port
		for (i = 0; i < loop; i++) {
			isCustomerCopy = (i == endOfLoop);
			printHeader();
			dataSize = data.length;
			previousItemBizUnit = (int) data[0][8];
			
			for (j = 0; j < dataSize; j++) {
				currentItemBizUnit = (int) data[j][8];
				doLastTwoItemBizUnitsDiffer = currentItemBizUnit != previousItemBizUnit;
				isEndOfPage = (j % LINES_PER_PAGE) == 0;
				if (isCustomerCopy) {
					qty = (BigDecimal) data[j][4];
					uom = (String) data[j][3];
					if (isEndOfPage || doLastTwoItemBizUnitsDiffer)
						printLine();						
				} else {
					qty = (BigDecimal) netItemQtyToLoad[j][0];
					uom = (String) netItemQtyToLoad[j][1];
					if (doLastTwoItemBizUnitsDiffer)
						printLine();
				}
				
				ps.print(getQty(qty));
				ps.print(uom + " ");
				ps.print(getName(data[j]));
				
				if (isCustomerCopy) {
					ps.print(getPrice(data[j]));
					total = (BigDecimal) data[j][6];
					ps.print(getSubtotal());
				} else {
					ps.print(getItemId(data[j]));
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

	private String getItemId(Object[] data) {
	    return StringUtils.leftPad("" + data[1], 4);
    }

	private String getSubtotal() {
	    return StringUtils.leftPad(DIS.NO_COMMA_DECIMAL.format(total), 9);
    }

	private String getPrice(Object[] data) {
	    return StringUtils.leftPad(DIS.NO_COMMA_DECIMAL.format(data[5]) + "@", 8);
    }

	private String getName(Object[] data) {
	    return StringUtils.rightPad(data[7].toString(), 19);
    }

	private String getQty(BigDecimal qty) {
	    return StringUtils.leftPad(DIS.INTEGER.format(qty), 3);
    }

	private void printHeader() throws IOException {
		printLogo();
		printSubheader();
	}

	private void printSubheader() throws IOException {
	    if (isCustomerCopy) {
			ps.println("DATE   : " + DIS.LONG_DATE.format(postDate));
			ps.println("DUE    : " + DIS.LONG_DATE.format(dueDate));
		} else {
			msg = "** " + DIS.LONG_DATE.format(postDate) + " **";
			printHuge();
			ps.println(StringUtils.center(msg, LINES_PER_PAGE/2));
			printNormal();
		}
		if (isExTruck) {
			ps.println("LOAD TO: " + partner);
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
		BigDecimal discountRate1 = order.getDiscount1Percent();
		BigDecimal totalDiscount1 = order.getDiscount1Total();
		BigDecimal discountRate2 = order.getDiscount2Percent();
		BigDecimal totalDiscount2 = order.getDiscount2Total();
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
	
	public void printLine() {
		ps.println("________________________________________");
	}
}

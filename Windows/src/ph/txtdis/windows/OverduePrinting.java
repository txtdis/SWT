package ph.txtdis.windows;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public class OverduePrinting extends Printer {
	private int outletId;

	public OverduePrinting(int outletId) {
		super(null);
		this.outletId = outletId;
	}

	@Override
	protected boolean print() throws IOException {
		// Prepare Data
		Overdue overdue = new Overdue(outletId);
		String outlet = new Customer().getName();
		String balance = DIS.TWO_PLACE_DECIMAL.format(overdue.getBalance());
		String issuer = new Contact().getFullName();
		String receiver = new Contact(outletId).getFullName();
		// Print Logo
		printLogo();
		// Print receipt
		os.write(ESC);
		os.write(AT);
		ps.println("");
		ps.println(StringUtils.center(outlet + "'S", COLUMN_WIDTH));
		ps.println(StringUtils.center("" + "PAST-DUE ACCOUNTS AS OF " + "" + DIS.STANDARD_DATE.format(DIS.TODAY), COLUMN_WIDTH));
		ps.println("----------------------------------------");
		ps.print(StringUtils.leftPad("S/I #", 6));
		ps.print(StringUtils.leftPad("DATE", 8));
		ps.print(StringUtils.leftPad("DUE", 9));
		ps.println(StringUtils.leftPad("AMOUNT", 14));
		ps.println("----------------------------------------");
		for (Object[] items : overdue.getData()) {
			ps.print(StringUtils.leftPad(DIS.NO_COMMA_INTEGER.format(items[1]), 6));
			ps.print(StringUtils.leftPad(DIS.STANDARD_DATE.format(items[2]), 10));
			ps.print(StringUtils.leftPad(DIS.STANDARD_DATE.format(items[3]), 10));
			ps.println(StringUtils.leftPad(DIS.TWO_PLACE_DECIMAL.format(items[5]), 14));
		}
		ps.println(StringUtils.leftPad("------------", COLUMN_WIDTH));
		ps.print(StringUtils.leftPad("TOTAL", 26));
		ps.println(StringUtils.leftPad(balance, 14));
		ps.println(StringUtils.leftPad("============", COLUMN_WIDTH));
		ps.println("");
		ps.print(StringUtils.rightPad("ISSUED BY:", 21));
		ps.println("RECEIVED BY:");
		ps.println("___________________  ___________________");
		ps.print(StringUtils.center(issuer, 19));
		ps.print("  ");
		if (receiver == null) {
			receiver = outlet;
		} else {
			receiver = receiver.replace("  ", " ");
		}
		ps.println(StringUtils.center(receiver, 19));
		printPageEnd();
		waitForPrintingToEnd();
		return true;
	}
}

package ph.txtdis.windows;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public class ReturnedMaterialPrinting extends SalesOrderPrinting {

	public ReturnedMaterialPrinting(Report report) {
		super(report);
	}

	@Override
	protected boolean print() throws IOException {
		// Prepare Data
		issuer = new Contact(partnerId).getFullName();
		issuer = issuer.trim().isEmpty() ? partner : issuer;
		receiver = new Contact().getFullName();
		// Write to Serial Port
		for (int c = 0; c < 2; c++) {
			// Print Logo
			printLogo();
			// Print receipt
			printHuge();
			ps.println(StringUtils.center("RETURN MATERIAL", COLUMN_WIDTH/2));
			if (c == 0) {
				ps.println(StringUtils.center("RECEIPT AUTHORITAZION", COLUMN_WIDTH/2));
			} else {
				ps.println(StringUtils.center("RECEIPT", COLUMN_WIDTH/2));
			}
			printNormal();
			ps.println("DATE   : " + DIS.LONG_DATE.format(postDate));
			ps.println("OUTLET : " + partner);
			ps.println("ADDRESS: " + address);
			ps.println("----------------------------------------");
			ps.println(StringUtils.center("PARTICULARS", COLUMN_WIDTH));
			ps.println("----------------------------------------");
			for (Object[] items : order.getData()) {
				ps.print(StringUtils.leftPad(DIS.NO_COMMA_DECIMAL.format(items[4]), 5));
				ps.print(items[3] + " ");
				ps.print(StringUtils.rightPad(items[7].toString(), 17));
				ps.print(StringUtils.leftPad("" + items[1], 4));
				ps.print(" ____  ____");
				ps.println();
			}
			ps.println();
			if (c == 0) {
				ps.println(StringUtils.center("Difference in quantities from approved,", COLUMN_WIDTH));
				ps.println(StringUtils.center("invalidates this authorization.", COLUMN_WIDTH));
				ps.println();
				ps.print(StringUtils.rightPad("ISSUED BY:", 21));
				ps.println("RECEIVED BY:");
				ps.println("___________________  ___________________");
				ps.print(StringUtils.center(issuer, 19));
				ps.print("  ");
				ps.println(StringUtils.center(receiver, 19));
			} else {
				ps.println(StringUtils.center("Do NOT accept if quantities", COLUMN_WIDTH));
				ps.println(StringUtils.center("differ from approved.", COLUMN_WIDTH));
				ps.println();
				ps.println("RECEIVED BY:");
				ps.println("_____________ _____________ ____________");
				ps.println("    TRUCK        CHECKER      WAREHOUSE");
			}
			ps.println("");
			ps.print(soID);
			copy = c == 0 ? "CUSTOMER COPY " : "WAREHOUSE/CHECKER COPY ";
			ps.println(StringUtils.leftPad(copy, COLUMN_WIDTH - soID.length()));
			printPageEnd();
		}
		waitForPrintingToEnd();
		return new SalesOrderPrintOut(salesId).set();
	}
}
package ph.txtdis.windows;

import java.io.IOException;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class ReturnedMaterialPrinting extends Printer {

	public ReturnedMaterialPrinting(Report report) {
		super(report);
	}

	@Override
	protected boolean print() {
		// Prepare Data
		SalesOrder order = (SalesOrder) report;
		int partnerId = order.getPartnerId();
		int salesId = order.getId();
		CustomerHelper helper = new CustomerHelper(partnerId);
		Date date = order.getPostDate();
		String issuer = new Contact().getName(partnerId);
		String copy = "";
		String currentUser = (String) new SQL().getDatum("" +
				"SELECT cd.name || ' ' || cd.surname " +
				"  FROM contact_detail AS cd " +
				"INNER JOIN customer_master AS cm " +
				"    ON cd.customer_id = cm.id " +
				"INNER JOIN channel AS ch " +
				"	 ON cm.type_id = ch.id " +
				" WHERE	cd.name = UPPER(CURRENT_USER) " +
				"	AND ch.name = 'SELF';"); 
		String receiver = currentUser;
		// Write to Serial Port
		try {
			for (int c = 0; c < 2; c++) {
				// Print Logo
				printLogo();
				// Print receipt
				os.write(ESC);
				os.write(AT);
				ps.println("");
				if (c == 0){
					ps.println(StringUtils.center("AUTHORITY TO RETURN MATERIALS", 40));
				} else {
					ps.println(StringUtils.center("RETURN MATERIAL RECEIPT", 40));
				}
				ps.println("");
				ps.println("DATE   : " + DIS.LDF.format(date));
				ps.println("OUTLET : " + helper.getName());
				ps.println("ADDRESS: " + new Address(partnerId).getCityDistrict());
				ps.println("----------------------------------------");
				ps.println(StringUtils.center("PARTICULARS", 40));
				ps.println("----------------------------------------");
				for (Object[] items : order.getData()) {
					ps.print(StringUtils.leftPad(DIS.SNF.format(items[4]), 5));
					ps.print(items[3] + " ");
					ps.print(StringUtils.rightPad(items[7].toString(), 17));
					ps.print(StringUtils.leftPad("" + items[1], 4));
					ps.print(" ____  ____");
					ps.println();
				}
				ps.println();
				if (c == 0){
					ps.println(
							StringUtils.center("Difference in quantities from approved,", 40));
					ps.println(
							StringUtils.center("invalidates this authorization.", 40));
					ps.println();
					ps.print(StringUtils.rightPad("ISSUED BY:", 21));
					ps.println("RECEIVED BY:");
					ps.println("___________________  ___________________");
					if (issuer == null) {
						issuer = helper.getName();
					} else {
						issuer = issuer.replace("  ", " ");
					}					
					ps.print(StringUtils.center(issuer, 19));
					ps.print("  ");
					ps.println(StringUtils.center(receiver, 19));
				} else {
					ps.println(StringUtils.center("Do NOT accept if quantities", 40));
					ps.println(StringUtils.center("differ from approved.", 40));
					ps.println();
					ps.println("RECEIVED BY:");
					ps.println("_____________ _____________ ____________");
					ps.println("    TRUCK        CHECKER      WAREHOUSE");
				}
				ps.println("");
				String so = "S/O# " + salesId;
				ps.print(so);
				if (c == 0){
					copy = "CUSTOMER COPY ";
				} else {
					copy = "WAREHOUSE/CHECKER COPY "; 
				}
				ps.println(StringUtils.leftPad(copy, 40 - so.length()));
				ps.println("________________________________________");
				ps.println();
				ps.println();
				ps.println();
				ps.println();
			}
			for (int i = 0; i < 10; i++) {
				int buffer = port.getOutputBufferSize();
				if(buffer == 0) {
					os.write(DLE);
					os.write(EOT);
					os.write(STATUS);
				}
				Thread.sleep(1000);
			}
			return new SalesOrderPrintOut(salesId).set();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			new ErrorDialog("Returned Material Printer:\n" + e);
			return false;
		}
	}
}
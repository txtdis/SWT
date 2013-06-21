package ph.txtdis.windows;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class SalesOrderPrinting extends Printer {

	public SalesOrderPrinting(Report report) {
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
		Date dueDate = new DateAdder(date).plus(order.getLeadTime());
		BigDecimal ZERO = BigDecimal.ZERO;
		BigDecimal dr1 = order.getDiscountRate1();
		BigDecimal dv1 = order.getTotalDiscount1();
		BigDecimal dr2 = order.getDiscountRate2();
		BigDecimal dv2 = order.getTotalDiscount2();		
		BigDecimal subTotal = BigDecimal.ZERO;
		BigDecimal total = BigDecimal.ZERO;
		String issuer = "WAREHOUSE";
		String copy = "";
		String currentUser = new SQL().getDatum("" +
				"SELECT cd.name || ' ' || cd.surname " +
				"  FROM contact_detail AS cd " +
				"INNER JOIN customer_master AS cm " +
				"    ON cd.customer_id = cm.id " +
				"INNER JOIN channel AS ch " +
				"	 ON cm.type_id = ch.id " +
				" WHERE	cd.name = UPPER(CURRENT_USER) " +
				"	AND ch.name = 'SELF';").toString(); 
		// Write to Serial Port
		try {
			for (int c = 0; c < 2; c++) {
				// Print logo
				printLogo();
				// Print receipt
				ps.println("");
				if (c == 1) {
					ps.println("DATE   : " + DIS.LDF.format(date));
					ps.println("DUE    : " + DIS.LDF.format(dueDate));
				} else {
					os.write(ESC);
					os.write(EXCLAMATION);
					os.write(HUGE);					
					ps.println(StringUtils.center("** " + DIS.LDF.format(date) + " **", 21));
					os.write(ESC);
					os.write(N);
					os.write(CPL);
					os.write(NARROW);
					os.write(ESC);
					os.write(AT);					
					ps.println("");
				}
				ps.println("SOLD TO: " + helper.getName());
				if (c == 1) {
					ps.println("ADDRESS: " + new Address(partnerId).getCityDistrict());
					ps.println(StringUtils.leftPad("", 42, "-"));
					ps.println(StringUtils.center("PARTICULARS", 42));
				} else {
					ps.println(StringUtils.leftPad("", 42, "-"));
					ps.println("  QTY    DESCRIPTION     CODE  OUT    IN");				
				}
				ps.println(StringUtils.leftPad("", 42, "-"));
				for (Object[] items : order.getData()) {
					ps.print(StringUtils.leftPad(DIS.LIF.format(items[4]), 3));
					ps.print(items[3] + " ");
					ps.print(StringUtils.rightPad(items[7].toString(), 19));
					if (c == 1) {
						ps.print(StringUtils.leftPad(DIS.SNF.format(items[5]) + "@", 8));
						total = (BigDecimal) items[6];
						ps.print(StringUtils.leftPad(DIS.SNF.format(total), 9));
					} else {
						ps.print(StringUtils.leftPad("" + items[1], 4));
						ps.print(" _____  _____");
					}
					ps.println();
					subTotal = subTotal.add(total);
				}
				if(c == 1) {
					ps.println(StringUtils.leftPad("--------", 42));
					ps.println(StringUtils.leftPad(DIS.SNF.format(subTotal), 42));
					ps.print(StringUtils.rightPad("VATABLE",8)); 
					ps.print(StringUtils.leftPad(DIS.SNF.format(order.getTotalVatable()),9));
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
					ps.println(StringUtils.leftPad(DIS.SNF.format(order.getSumTotal()), 9));
					ps.println(StringUtils.leftPad("========", 42));
					ps.println("");
					ps.print(StringUtils.rightPad("ISSUED BY:", 21));
					ps.println("RECEIVED BY:");
					ps.println("___________________    ___________________");
					issuer = currentUser;
					ps.print(StringUtils.center(issuer, 19));
					ps.print("    ");
					String receiver = new Contact().getName(partnerId);
					if (receiver == null) {
						receiver = helper.getName();
					} else {
						receiver = receiver.replace("  ", " ");
					}					
					ps.println(StringUtils.center(receiver, 19));
				} else {
					ps.println("");
					ps.println("LOAD-IN:");
					ps.println("_____________  _____________  ____________");
					ps.println("  WAREHOUSE       CHECKER        TRUCK");
					ps.println("");
					ps.println("BACKLOAD:");
					ps.println("_____________  _____________  ____________");
					ps.println("    TRUCK          CHECKER      WAREHOUSE");
				}
				ps.println("");
				String so = "S/O# " + salesId;
				ps.print(so);
				switch (c) {
					case 0: copy = "WAREHOUSE/CHECKER COPY"; break;
					case 1: copy = "CUSTOMER COPY"; break;
				}
				ps.println(StringUtils.leftPad(copy, 42 - so.length()));
				ps.println(StringUtils.leftPad("", 42, "_"));
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
			new ErrorDialog("Sales Order Printer:\n" + e);
			return false;
		}
	}
}

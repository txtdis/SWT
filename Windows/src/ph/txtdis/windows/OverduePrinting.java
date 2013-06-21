package ph.txtdis.windows;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public class OverduePrinting extends Printer {

	public OverduePrinting(Report report) {
		super(report);
	}
	
	@Override
	protected boolean print() {
		// Prepare Data
		Overdue overdue = (Overdue) report;
		int outletId = overdue.getCustomerId();
		CustomerHelper ch = new CustomerHelper(outletId);
		String outlet = ch.getName();
		String balance = DIS.LNF.format(overdue.getBalance());
		String issuer = new SQL().getDatum("" +
				"SELECT cd.name || ' ' || cd.surname " +
				"FROM 	contact_detail AS cd, " +
				"		system_user AS su " +
				"WHERE	cd.id = su.contact_id " +
				"	AND	su.system_id = CURRENT_USER ").toString(); 
		String receiver = new Contact().getName(outletId);
		// Print Logo
		printLogo();
		// Print receipt
		try {
		os.write(ESC);
		os.write(AT);
		ps.println("");
		ps.println(StringUtils.center(outlet + "'S", 40));
		ps.println(StringUtils.center("" +
				"PAST-DUE ACCOUNTS AS OF " +
				"" + DIS.SDF.format(DIS.TODAY), 40));
		ps.println("----------------------------------------");
		ps.print(StringUtils.leftPad("S/I #", 6));
		ps.print(StringUtils.leftPad("DATE", 8));
		ps.print(StringUtils.leftPad("DUE", 9));
		ps.println(StringUtils.leftPad("AMOUNT", 14));
		ps.println("----------------------------------------");
		for (Object[] items : overdue.getData()) {
			ps.print(StringUtils.leftPad(
					DIS.SIF.format(items[1]), 6));
			ps.print(StringUtils.leftPad(
					DIS.SDF.format(items[2]), 10));
			ps.print(StringUtils.leftPad(
					DIS.SDF.format(items[3]), 10));
			ps.println(StringUtils.leftPad(
					DIS.LNF.format(items[5]), 14));
		}
		ps.println(StringUtils.leftPad("------------", 40));
		ps.print(StringUtils.leftPad("TOTAL", 26)); 
		ps.println(StringUtils.leftPad(balance, 14));
		ps.println(StringUtils.leftPad("============", 40));
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
		ps.println("________________________________________");
		ps.println("");
		ps.println("");
		ps.println("");
		ps.println("");
		return true;
		} catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Overdue Printer:\n" + e);
			return false;
		}
	}		
}

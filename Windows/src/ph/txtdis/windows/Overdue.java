package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

public class Overdue extends OverdueStatement {

	public Overdue(int customerId, Date startDate) {
		super(customerId, startDate);
		module = "Overdue Invoices";
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.MARCH, 1);
		System.out.println(
				new Overdue(90, new Date(cal.getTimeInMillis())).getCustomerId());
		
		Database.getInstance().closeConnection();	
	}
}

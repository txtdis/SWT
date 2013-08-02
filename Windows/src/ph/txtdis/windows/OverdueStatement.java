package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class OverdueStatement extends Report {

	public OverdueStatement (int partnerId, Date startDate) {
		this.partnerId = partnerId;
		module = "Overdue Statement";
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("SI/(DR)", 7), "ID"},
				{StringUtils.center("SERIES", 6), "String"},
				{StringUtils.center("SI/DR DATE", 10), "Date"},
				{StringUtils.center("DUE DATE", 10), "Date"},
				{StringUtils.center("DAYS OVER", 9), "ID"},
				{StringUtils.center("AMOUNT", 13), "BigDecimal"}
		};

		data = new Overdue(partnerId, startDate).getData(); 
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Calendar cal = Calendar.getInstance();
		cal.set(2010, Calendar.MARCH, 1);
		new OverdueStatement(410, new Date(cal.getTimeInMillis()));
		Database.getInstance().closeConnection();
	}
}

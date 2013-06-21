package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class OverdueStatement extends Report {
	private int customerId;
	private BigDecimal balance;

	public OverdueStatement (int customerId, Date startDate) {
		this.customerId = customerId;
		startDate = startDate == null ? new Date(0L) : startDate;
		module = "Overdue Statement";
		balance = BigDecimal.ZERO;
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("SI/(DR)", 7), "ID"},
				{StringUtils.center("SERIES", 6), "String"},
				{StringUtils.center("SI/DR DATE", 10), "Date"},
				{StringUtils.center("DUE DATE", 10), "Date"},
				{StringUtils.center("DAYS OVER", 9), "ID"},
				{StringUtils.center("AMOUNT", 13), "BigDecimal"}
		};

		data = new SQL().getDataArray(new Object[] {customerId, startDate}, "" +
				"SELECT 0, " +
				"		order_id, " +
				"		series, " +
				"		order_date, " +
				"		due_date, " +
				"		days_over, " +
				"		balance " +
				"FROM 	overdue " +
				"WHERE 	customer_id = ? " +
				"	AND order_date >= ? " +
				"ORDER BY days_over DESC " +
				"");
		if (data != null) {
			for (Object[] ao : data) {
				if(ao.length >= 5)
					balance = balance.add(ao[6] == null ? BigDecimal.ZERO : (BigDecimal)ao[6]); 
			}
		}
	}

	public int getCustomerId() {
		return customerId;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Calendar cal = Calendar.getInstance();
		cal.set(2010, Calendar.MARCH, 1);
		new OverdueStatement(410, new Date(cal.getTimeInMillis()));
		Database.getInstance().closeConnection();
	}
}

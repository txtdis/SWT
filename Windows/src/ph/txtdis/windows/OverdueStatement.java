package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;

public class OverdueStatement extends Report {

	public OverdueStatement (int partnerId) {
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

		Overdue overdue = new Overdue(partnerId);
		data = overdue.getData();
		dates = overdue.getDates();
	}
}

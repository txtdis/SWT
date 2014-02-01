package ph.txtdis.windows;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class OverdueStatement extends Data implements Subheaded {
	private int partnerId;

	public OverdueStatement (int partnerId) {
		this.partnerId = partnerId;
		type = Type.OVERDUE;
		tableHeaders = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("SI/(DR)", 7), "ID"},
				{StringUtils.center("SERIES", 6), "String"},
				{StringUtils.center("SI/DR DATE", 10), "Date"},
				{StringUtils.center("DUE DATE", 10), "Date"},
				{StringUtils.center("DAYS OVER", 9), "ID"},
				{StringUtils.center("AMOUNT", 13), "BigDecimal"}
		};

		tableData = new Overdue().getData(partnerId);
		dates = new Date[] {DIS.NO_SO_WITH_OVERDUE_CUTOFF, DIS.TODAY};
	}

	@Override
    public String getSubheading() {
	    return Customer.getName(partnerId) + "\nearlier than " + DIS.LONG_DATE.format(dates[0]) + " excleded";
    }
}

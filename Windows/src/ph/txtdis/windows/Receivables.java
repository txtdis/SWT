package ph.txtdis.windows;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class Receivables extends Report {

	public Receivables() {
		module = "Receivables";
		headers = new String[][]{
				{StringUtils.center("ROUTE", 12), "String"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("CUSTOMER NAME", 28), "String"},
				{StringUtils.center("TOTAL", 13), "BigDecimal"},
				{StringUtils.center("CURRENT", 13), "BigDecimal"},
				{StringUtils.center("1-7", 13), "BigDecimal"},
				{StringUtils.center("8-15", 13), "BigDecimal"},
				{StringUtils.center("16-30", 13), "BigDecimal"},
				{StringUtils.center(">30", 13), "BigDecimal"}
		};

		// Data
		data = new Data().getDataArray("SELECT * FROM aging");
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Calendar cal = Calendar.getInstance();
		cal.set(2013, 1, 28);
		Receivables r = new Receivables();
		for (Object[] os : r.getData()) {
			for (Object o : os) {
				System.out.print(o + ", ");
			}
			System.out.println();
		}
		Database.getInstance().closeConnection();
	}

}

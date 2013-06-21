package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class Vat extends Report {

	protected Calendar cal = Calendar.getInstance();
	protected Date start, end;
	protected Date[] dates;
	
	public Vat(){
		this(null);
	};

	public Vat(Date[] dates){
		if (dates == null) {
			dates = new Date[2];
			cal.set(Calendar.DAY_OF_MONTH, 1);
			dates[0] = new Date(cal.getTimeInMillis());
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			dates[1]= new Date(cal.getTimeInMillis());
		}
		start = dates[0];
		end = dates[1];
		this.dates = dates;

		module = "Value-Added Tax";

		headers = new String[][] {
				{StringUtils.center("DATE", 10), "Date"},
				{StringUtils.center("INVOICE",7), "ID"},
				{StringUtils.center("SERIES", 6), "String"},
				{StringUtils.center("CUSTOMER NAME", 30), "String"},
				{StringUtils.center("TOTAL", 13), "BigDecimal"},
				{StringUtils.center("VAT", 12), "BigDecimal"}
		};
		data = new SQL().getDataArray(new Date[] {start, end}, "" +
				"WITH " +
				"vat AS ( " +
				"	SELECT 	value AS rate " +
				"	FROM 	default_number " +
				"	WHERE name = 'VAT' " +
				")" +
				"SELECT	DISTINCT " +
				"		ih.invoice_date, " +
				"		ih.invoice_id, " +
				"		ih.series, " +
				"		cm.name, " +
				"			CASE WHEN ih.actual IS NULL THEN 0 ELSE ih.actual END AS" +
				"		actual, " +
				"			(CASE WHEN ih.actual IS NULL THEN 0 ELSE ih.actual END)" +
				"			 * (SELECT rate FROM vat) AS " +
				"		vat " +
				"FROM 	invoice_header AS ih " +
				"LEFT OUTER JOIN invoice_detail AS id " +
				"	ON	ih.invoice_id = id.invoice_id " +
				"	AND	ih.series = id.series " +
				"INNER JOIN customer_master AS cm " +
				"	ON 	ih.customer_id = cm.id " +
				"WHERE 	ih.invoice_date BETWEEN ? AND ? " +
				"ORDER BY ih.invoice_date " 
				);
	}

	public Date[] getDates() {
		return dates;
	}
	
	public Object[][] getData() {
		return data;
	}
	
	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Vat i = new Vat();
		for (Object[] os : i.getData()) {
			for (Object o : os) {
				System.out.print(o + ", ");
			}
			System.out.println();
		}
		Database.getInstance().closeConnection();
	}

}

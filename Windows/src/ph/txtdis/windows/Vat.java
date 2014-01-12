package ph.txtdis.windows;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class Vat extends Report implements Startable {
	
	public Vat() {}

	public Vat(Date[] dates){
		this.dates = dates == null ? new Date[] {DIS.getFirstOfTheMonth(DIS.TODAY), DIS.getLastOfTheMonth(DIS.TODAY)} : dates;
		module = "Value-Added Tax";
		headers = new String[][] {
				{StringUtils.center("DATE", 10), "Date"},
				{StringUtils.center("S/I(D/R)",7), "ID"},
				{StringUtils.center("SERIES", 6), "String"},
				{StringUtils.center("CUSTOMER NAME", 30), "String"},
				{StringUtils.center(DIS.CURRENCY_SIGN + " TOTAL", 13), "BigDecimal"},
				{StringUtils.center(DIS.CURRENCY_SIGN + " VAT", 12), "BigDecimal"}
		};
		data = new Data().getDataArray(this.dates, ""
				// @sql:on
				+ "WITH parameter AS\n" 
				+ "		 (SELECT cast (? AS date) AS start_date, cast (? AS date) AS end_date),\n" 
				+ "	 vat AS\n" 
				+ "		 (SELECT value AS rate\n" 
				+ "			FROM default_number\n" 
				+ "		   WHERE name = 'VAT'),\n" 
				+ "	 invoiced AS\n" 
				+ "		 (SELECT invoice_date,\n" 
				+ "				 invoice_id,\n" 
				+ "				 series,\n" 
				+ "				 name,\n" 
				+ "				 actual,\n" 
				+ "				 actual * (SELECT rate FROM vat)\n" 
				+ "			FROM invoice_header\n" 
				+ "				 INNER JOIN customer_master ON customer_id = id\n" 
				+ "				 INNER JOIN parameter ON invoice_date BETWEEN start_date AND end_date),\n" 
				+ "	 delivered AS\n" 
				+ "		 (SELECT delivery_date,\n" 
				+ "				 -delivery_id,\n" 
				+ "				 cast ('DR' AS text),\n" 
				+ "				 name,\n" 
				+ "				 actual,\n" 
				+ "				 0.0 AS vat\n" 
				+ "			FROM delivery_header\n" 
				+ "				 INNER JOIN customer_master ON customer_id = id\n" 
				+ "				 INNER JOIN parameter ON delivery_date BETWEEN start_date AND end_date)\n" 
				+ "SELECT * FROM invoiced\n" 
				+ "UNION\n" 
				+ "SELECT * FROM delivered\n" 
				+ "ORDER BY 1\n" 
				// @sql:off
				);
	}

	@Override
    public void start() {
		new VatView(null);
    }
}

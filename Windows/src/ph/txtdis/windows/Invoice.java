package ph.txtdis.windows;

import java.sql.Date;

public class Invoice {

	public static String addCTE(Date start, Date end) {
		// @sql:on
		return    "	 invoiced AS\n" 
				+ "		 (SELECT DISTINCT ON(ih.invoice_id, ih.series, id.item_id)\n" 
				+ "			     ih.invoice_id AS order_id, "
				+ "			     ih.series, "
				+ "				 last_value( a.route_id)\n" 
				+ "				     OVER (PARTITION BY ih.invoice_id, ih.series, id.item_id ORDER BY a.start_date desc)\n"
				+ "                  AS route_id,\n" 
				+ "				 id.item_id, (id.qty * qp.qty) AS qty\n" 
				+ "			FROM invoice_header AS ih\n" 
				+ "				 INNER JOIN invoice_detail AS id\n" 
				+ "					 ON     ih.invoice_id = id.invoice_id\n"
				+ "                     AND ih.series = id.series\n" 
				+ "				        AND ih.invoice_date BETWEEN '" + start + "' AND '" + end + "'\n" 
				+ "				 INNER JOIN qty_per AS qp ON id.uom = qp.uom AND id.item_id = qp.item_id\n" 
				+ "				 INNER JOIN account AS a\n" 
				+ "					 ON ih.customer_id = a.customer_id AND a.start_date <= ih.invoice_date\n" 
				+ "		   WHERE ih.actual > 0"
				;
		// @sql:off
	}

	public static String addCTE(int id, String series) {
		// @sql:on
		return addCTE(DIS.FAR_PAST, DIS.FAR_FUTURE) 
				+ "              AND ih.invoice_id = " + id + "\n"
		        + "              AND ih.series = '" + series + "')\n";
		// @sql:off
	}
}

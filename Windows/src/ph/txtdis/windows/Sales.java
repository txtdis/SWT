package ph.txtdis.windows;

import java.sql.Date;

public class Sales {

	public Sales() {
	}

    public static String addCTE(Date start, Date end) {
		// @sql:on
	    return 	  "	 booked AS\n" 
				+ "		 (SELECT DISTINCT ON(sh.sales_id, sd.item_id)\n" 
				+ "			     sh.sales_id, "
				+ "				 last_value( a.route_id)\n" 
				+ "				     OVER (PARTITION BY sh.sales_id, sd.item_id ORDER BY a.start_date desc) AS route_id,\n" 
				+ "				 sd.item_id, sd.qty * qp.qty AS qty\n" 
				+ "			FROM sales_header AS sh\n" 
				+ "				 INNER JOIN sales_detail AS sd\n"
				+ "                 ON     sh.sales_id = sd.sales_id\n" 
				+ "				       AND sh.sales_date BETWEEN '" + start + "' AND '" + end + "'\n" 
				+ "				 INNER JOIN qty_per AS qp ON sd.uom = qp.uom AND sd.item_id = qp.item_id\n" 
				+ "				 INNER JOIN account AS a\n" 
				+ "					 ON sh.customer_id = a.customer_id AND a.start_date <= sh.sales_date),\n" 
				+ "	 loaded AS\n" 
				+ "		 (	SELECT route_id, item_id, sum (qty) AS qty\n" 
				+ "			  FROM booked\n" 
				+ "		  GROUP BY route_id, item_id\n" ;
	    // @sql:off
    }

    public static String addCTE(int id, String series) {
	    return addCTE(DIS.FAR_PAST, DIS.FAR_FUTURE) + "              AND sh.sales_id = " + id + ")\n";
    }
}

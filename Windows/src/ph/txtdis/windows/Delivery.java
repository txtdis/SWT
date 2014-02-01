package ph.txtdis.windows;

import java.sql.Date;

public class Delivery {

    public static String addCTE(Date start, Date end) {
	    return 	"	 delivered AS\n" 
				+ "		 (SELECT DISTINCT ON(dh.delivery_id, dd.item_id)\n" 
				+ "			     -dh.delivery_id AS order_id, "
				+ "			     CAST('DR' AS text) AS series, "
				+ "				 last_value( a.route_id)\n" 
				+ "				     OVER (PARTITION BY dh.delivery_id, dd.item_id ORDER BY a.start_date DESC) AS route_id,\n" 
				+ "				 dd.item_id, dd.qty * qp.qty AS qty\n" 
				+ "			FROM delivery_header AS dh\n" 
				+ "				 INNER JOIN delivery_detail AS dd\n"
				+ "                 ON     dh.delivery_id = dd.delivery_id\n" 
				+ "				       AND dh.delivery_date BETWEEN '" + start + "' AND '" + end + "'\n" 
				+ "				 INNER JOIN qty_per AS qp ON dd.uom = qp.uom AND dd.item_id = qp.item_id\n" 
				+ "				 INNER JOIN account AS a\n" 
				+ "					 ON dh.customer_id = a.customer_id AND a.start_date <= dh.delivery_date\n" 
				+ "		   WHERE dh.actual > 0"
				;
    }

    public static String addCTE(int id, String series) {
	    return addCTE(DIS.FAR_PAST, DIS.FAR_FUTURE) + "              AND dh.delivery_id = " + id + ")\n";
    }
}

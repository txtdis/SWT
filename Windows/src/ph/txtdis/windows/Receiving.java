package ph.txtdis.windows;

import java.sql.Date;

public class Receiving {

	public Receiving() {
	}

	public static String addCTE(Date start, Date end) {
		return    "  received_per_rr AS\n" 
				+ "		 (SELECT rh.receiving_id,\n"
				+ "			     rh.partner_id,\n"				
				+ "			     rh.receiving_date,\n"				
				+ "				 rd.item_id,\n"
				+ "				 sum(rd.qty * qp.qty) AS qty\n" 
				+ "			FROM receiving_header AS rh\n" 
				+ "				 INNER JOIN receiving_detail AS rd\n"
				+ "                 ON     rh.receiving_id = rd.receiving_id\n" 
				+ "				       AND rh.receiving_date BETWEEN '" + start + "' AND '" + end + "'\n" 
				+ "				 INNER JOIN qty_per AS qp ON rd.uom = qp.uom AND rd.item_id = qp.item_id\n" 
				+ "				 INNER JOIN customer_header AS ch ON ch.id = rh.partner_id\n" 
				+ "				 INNER JOIN channel AS c ON c.id = ch.type_id\n" 
				+ "		   WHERE rh.ref_id > 0 AND rd.qc_id = 0 AND c.name <> 'EMPLOYEE'\n" 
				+ "		GROUP BY rh.receiving_id,\n"
				+ "			     rh.partner_id,\n"				
				+ "			     rh.receiving_date,\n"				
				+ "				 rd.item_id), "
				+ "	 received AS\n" 
				+ "		 (SELECT DISTINCT ON(rr.receiving_id, rr.item_id)\n" 
				+ "			     rr.receiving_id,\n"
				+ "				 last_value( a.route_id)\n" 
				+ "				     OVER (PARTITION BY rr.receiving_id, rr.item_id ORDER BY a.start_date DESC) AS route_id,\n" 
				+ "				 rr.item_id,\n"
				+ "				 rr.qty\n" 
				+ "			FROM received_per_rr AS rr\n" 
				+ "				 INNER JOIN account AS a\n" 
				+ "					 ON rr.partner_id = a.customer_id AND a.start_date <= rr.receiving_date),\n" 
				+ "	 returned AS\n" 
				+ "		 (	SELECT route_id, item_id, sum (qty) AS qty\n" 
				+ "			  FROM received\n" 
				+ "		  GROUP BY route_id, item_id\n";
	}

	public static String addCTE(int id, String series) {
	    return addCTE(DIS.FAR_PAST, DIS.FAR_FUTURE) + "              AND dh.receiving_id = " + id + ")\n";
	}
}

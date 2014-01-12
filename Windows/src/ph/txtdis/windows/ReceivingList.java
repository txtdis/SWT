package ph.txtdis.windows;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class ReceivingList extends Report {

	public ReceivingList(Date[] dates, int itemId, Integer routeId, Integer qcId) {
		module = "Receiving Report List";
		this.dates = dates;
		this.itemId = itemId;
		this.routeId = routeId;

		headers = new String[][] {{
			StringUtils.center("#", 3), "Line" }, {
			StringUtils.center("R/R", 7), "ID" }, {
			StringUtils.center("DATE", 10), "Date" }, {
			StringUtils.center("CUSTOMER", 28), "String" }, {
			StringUtils.center("QUANTITY", 9), "BigDecimal" } 
		};

		if(routeId != null)
			data = getPerRouteList(dates[0], dates[1], itemId, routeId);
		else 
			data = getTotalList(DIS.addDays(dates[0], 1), dates[1], itemId, qcId);

	}

	private Object[][] getPerRouteList(Date start, Date end, int itemId, int routeId) {
		return new Data().getDataArray(new Object[] {start, end, itemId, routeId}, "" 
				+ " WITH received_per_rr AS\n" 
				+ "		 (SELECT rh.receiving_id,\n"
				+ "			     rh.partner_id,\n"				
				+ "			     rh.receiving_date,\n"				
				+ "				 rd.item_id,\n"
				+ "				 sum(rd.qty * qp.qty) AS qty\n" 
				+ "			FROM receiving_header AS rh\n" 
				+ "				 INNER JOIN receiving_detail AS rd ON rh.receiving_id = rd.receiving_id\n" 
				+ "				 INNER JOIN qty_per AS qp ON rd.uom = qp.uom AND rd.item_id = qp.item_id\n" 
				+ "		   WHERE     rh.receiving_date BETWEEN ? AND ?\n" 
				+ "		   		 AND rh.ref_id > 0\n"
				+ "				 AND rd.qc_id = 0\n"
				+ "				 AND rd.item_id = ?\n" 
				+ "		GROUP BY rh.receiving_id,\n"
				+ "			     rh.partner_id,\n"				
				+ "			     rh.receiving_date,\n"				
				+ "				 rd.item_id), "
				+ "	     received AS\n" 
				+ "		 (SELECT DISTINCT ON(rr.receiving_id, rr.item_id)\n" 
				+ "			     rr.receiving_id,\n"
				+ "			     rr.partner_id,\n"
				+ "			     rr.receiving_date,\n"
				+ "				 last_value( a.route_id)\n" 
				+ "				     OVER (PARTITION BY rr.receiving_id, rr.item_id ORDER BY a.start_date DESC) AS route_id,\n" 
				+ "				 rr.item_id,\n"
				+ "				 rr.qty\n" 
				+ "			FROM received_per_rr AS rr\n" 
				+ "				 INNER JOIN account AS a\n" 
				+ "					 ON rr.partner_id = a.customer_id AND a.start_date <= rr.receiving_date)\n" 
				+ "SELECT row_number() OVER (ORDER BY receiving_id),\n"
				+ " 	  receiving_id,\n"
				+ " 	  receiving_date,\n"
				+ "		  name,\n" 
				+ "		  qty\n" 
				+ "  FROM received AS r\n"
				+ "       INNER JOIN customer_master AS cm\n"
				+ "	         ON r.partner_id = cm.id\n"
				+ " WHERE     qty <> 0\n"
				+ "       AND route_id = ?\n"
				+ " ORDER BY receiving_id;" 
				);
	}

	private Object[][] getTotalList(Date start, Date end, int itemId, int qcId) {
		return new Data().getDataArray(new Object[] {start, end, itemId, qcId}, "" 
				+ "SELECT ROW_NUMBER() OVER (ORDER BY rh.receiving_id),\n"
				+ " 	  rh.receiving_id,\n"
				+ " 	  rh.receiving_date,\n"
				+ "		  cm.name,\n" 
				+ "		  rd.qty * qp.qty AS qty\n" 
				+ "	 FROM receiving_header AS rh\n" 
				+ "	  	  INNER JOIN receiving_detail AS rd ON rh.receiving_id = rd.receiving_id\n" 
				+ "		  INNER JOIN qty_per AS qp ON rd.uom = qp.uom AND rd.item_id = qp.item_id\n" 
				+ "       INNER JOIN customer_master AS cm\n"
				+ "	         ON rh.partner_id = cm.id\n"
				+ " WHERE     rh.receiving_date BETWEEN ? AND ?\n"
				+ "       AND rd.item_id = ?\n" 
				+ "       AND rd.qc_id = ?\n" 
				+ " ORDER BY rh.receiving_id;" 
				);
	}
}

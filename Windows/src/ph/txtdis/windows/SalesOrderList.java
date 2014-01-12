package ph.txtdis.windows;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class SalesOrderList extends Report {

	public SalesOrderList(Date[] dates, int itemId, Integer routeId, Integer qcId){
		module = "Sales Order List";
		this.dates = dates;
		this.itemId = itemId;
		this.routeId = routeId;

		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("S/O", 7), "ID"},
				{StringUtils.center("DATE", 10), "Date"},
				{StringUtils.center("CUSTOMER", 28), "String"},
				{StringUtils.center("QUANTITY", 9), "BigDecimal"}
		};

		if(routeId != null)
			data = getPerRouteList(dates[0], dates[1], itemId, routeId);
		else 
			data = getTotalList(DIS.addDays(dates[0], 1), dates[1], itemId, qcId);

	}

	private Object[][] getPerRouteList(Date start, Date end, int itemId, int routeId) {
		return new Data().getDataArray(new Object[] {start, end, itemId, routeId}, "" 
				+ "WITH booked AS\n" 
				+ "		( SELECT DISTINCT ON(sh.sales_id, sd.item_id)\n" 
				+ "			     sh.sales_id, "
				+ "			     sh.sales_date, "
				+ "			     sh.customer_id, "
				+ "				 last_value( a.route_id)\n" 
				+ "				     OVER (PARTITION BY sh.sales_id, sd.item_id ORDER BY a.start_date desc) AS route_id,\n" 
				+ "				 sd.item_id, sd.qty * qp.qty AS qty\n" 
				+ "			FROM sales_header AS sh\n" 
				+ "				 INNER JOIN sales_detail AS sd ON sh.sales_id = sd.sales_id\n" 
				+ "				 INNER JOIN qty_per AS qp ON sd.uom = qp.uom AND sd.item_id = qp.item_id\n" 
				+ "				 INNER JOIN account AS a\n" 
				+ "					 ON sh.customer_id = a.customer_id AND a.start_date <= sh.sales_date\n" 
				+ "		   WHERE     sh.sales_date BETWEEN ? AND ?\n"
				+ "              AND sd.item_id = ?)" 
				+ "SELECT ROW_NUMBER() OVER (ORDER BY sales_id), "
				+ " 	  sales_id, "
				+ " 	  sales_date, "
				+ "		  name, " 
				+ "		  qty " 
				+ "  FROM booked AS b\n"
				+ "       INNER JOIN customer_master AS cm\n"
				+ "	         ON b.customer_id = cm.id\n"
				+ " WHERE qty <> 0 AND route_id = ?\n"
				+ " ORDER BY sales_id " 
				);
	}

	private Object[][] getTotalList(Date start, Date end, int itemId, int qcId) {
		return new Data().getDataArray(new Object[] {start, end, itemId}, "" 
				+ "SELECT ROW_NUMBER() OVER (ORDER BY h.sales_id),\n"
				+ " 	  h.sales_id,\n"
				+ "		  cm.name,\n" 
				+ "		  d.qty * qp.qty AS qty\n" 
				+ "	 FROM sales_header AS h\n" 
				+ "	  	  INNER JOIN sales_detail AS d ON h.sales_id = d.sales_id\n" 
				+ "		  INNER JOIN qty_per AS qp ON d.uom = qp.uom AND d.item_id = qp.item_id\n" 
				+ "       INNER JOIN customer_master AS cm\n"
				+ "	         ON h.customer_id = cm.id\n"
				+ " WHERE     h.sales_date BETWEEN ? AND ?\n"
				+ "       AND d.item_id = ?\n" 
				+ "       AND cm.name " + (qcId == 2 ? "" : "NOT ") + "LIKE '%DISPOSAL%'\n" 
				+ " ORDER BY h.sales_id;" 
				);
	}
}

package ph.txtdis.windows;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class SoldList extends Report {
	private int productLineId;

	public SoldList(Date[] dates) {
		if (dates == null) {
			dates = new Date[2];
			dates[0] = DIS.getFirstOfTheMonth(DIS.addMonths(DIS.TODAY, -1));
			dates[1] = DIS.getLastOfTheMonth(DIS.addMonths(DIS.TODAY, -1));
		}
		module = "Invoice/Delivery List";
		this.dates = dates;
	}

	public SoldList(Date[] dates, int partnerId, int productLineId, Integer categoryId){
		this(dates);
		this.categoryId = categoryId;
		this.productLineId = productLineId;
		this.partnerId = partnerId;
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("S/I(D/R)", 8), "ID"},
				{StringUtils.center("SERIES", 6), "String"},
				{StringUtils.center("DATE", 10), "Date"},
				{StringUtils.center("QUANTITY", 9), "Quantity"}
		};

		data = new Data().getDataArray(new Object[] {productLineId, this.dates[0], this.dates[1], partnerId}, 
				// @sql:on
				SQL.addItemParentStmt() + ",\n" + 
				"invoices AS ( " + 
				"	SELECT	ih.invoice_id, " +
				"			ih.series, " +
				"			ih.invoice_date, " +
				"			ih.customer_id, " +
				"			ih.actual, " +
				"			ih.ref_id, " +
				"			ih.user_id, " +
				"			ih.time_stamp, " +
				"			id.item_id, " +
				"			id.qty, " +
				"			id.uom, " +
				"			id.qty * qp.qty AS pcs, " +
				"			qp.qty AS qty_per " +
				"	FROM invoice_header AS ih " +
				"	INNER JOIN invoice_detail as id " +
				"		ON ih.invoice_id = id.invoice_id " +
				"		AND ih.series = id.series " +
				"	INNER JOIN qty_per as qp " +
				"		ON id.uom = qp.uom " +
				"			AND	id.item_id = qp.item_id " +
				") " +
				"SELECT	ROW_NUMBER() OVER(ORDER BY i.invoice_date), " +
				"		i.invoice_id, " +
				"		i.series, " +
				"		i.invoice_date, " +
				"		SUM(i.pcs / qp.qty) AS qty " +
				"FROM	invoices AS i  " +
				"INNER JOIN	parent_child AS ip " +
				"	ON	i.item_id = ip.child_id " +
				"	AND	ip.parent_id  = ? " +
				"INNER JOIN qty_per AS qp " +
				"	ON	i.item_id = qp.item_id " +
				"	AND qp.report = true " +
				" WHERE	    i.invoice_date BETWEEN ? AND ? " +
				"		AND i.customer_id = ? " +
				"GROUP BY i.invoice_id, " +
				"		i.series, " +
				"		i.invoice_date " +
				"HAVING SUM(i.pcs / qp.qty) <> 0 " +
				"ORDER BY i.invoice_date " +
				"");
	}

	public SoldList(Date[] dates, int itemId, Integer routeId){
		this(dates);
		this.itemId = itemId;
		
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("S/I(D/R)", 8), "ID"},
				{StringUtils.center("SERIES", 6), "String"},
				{StringUtils.center("DATE", 10), "Date"},
				{StringUtils.center("OUTLET", 40), "String"},
				{StringUtils.center("QUANTITY", 9), "BigDecimal"}
		};
		
		data = new Data().getDataArray(new Object[] {this.dates[0], this.dates[1], itemId}, "" 
				+ "WITH parameter AS\n" 
				+ "		 (SELECT cast (? AS date) AS start_date,\n"
				+ "              cast (? AS date) AS end_date,\n" 
				+ "              cast (? AS int) AS item_id),\n" 
				+ "   invoiced AS\n" 
				+ "		 (SELECT DISTINCT ON(ih.invoice_id, ih.series, id.item_id)\n" 
				+ "			     ih.invoice_id AS order_id, "
				+ "			     ih.series, "
				+ "			     ih.invoice_date AS order_date, "
				+ "			     ih.customer_id, "
				+ "				 last_value( a.route_id)\n" 
				+ "				     OVER (PARTITION BY ih.invoice_id, ih.series, id.item_id ORDER BY a.start_date desc)\n"
				+ "                  AS route_id,\n" 
				+ "				 id.item_id, (id.qty * qp.qty) AS qty\n" 
				+ "			FROM invoice_header AS ih\n" 
				+ "				 INNER JOIN invoice_detail AS id\n" 
				+ "					 ON ih.invoice_id = id.invoice_id AND ih.series = id.series\n" 
				+ "				 INNER JOIN qty_per AS qp ON id.uom = qp.uom AND id.item_id = qp.item_id\n" 
				+ "				 INNER JOIN account AS a\n" 
				+ "					 ON ih.customer_id = a.customer_id AND a.start_date <= ih.invoice_date\n" 
				+ "		   		 INNER JOIN parameter AS p "
				+ "                  ON     ih.invoice_date BETWEEN p.start_date AND p.end_date\n" 
				+ "                     AND id.item_id = p.item_id\n"
				+ "		   WHERE ih.actual > 0),\n" 
				+ "	 delivered AS\n" 
				+ "		 (SELECT DISTINCT ON(dh.delivery_id, dd.item_id)\n" 
				+ "			     -dh.delivery_id AS order_id, "
				+ "			     CAST('DR' AS text) AS series, "
				+ "			     dh.delivery_date AS order_date, "
				+ "			     dh.customer_id, "
				+ "				 last_value( a.route_id)\n" 
				+ "				     OVER (PARTITION BY dh.delivery_id, dd.item_id ORDER BY a.start_date DESC) AS route_id,\n" 
				+ "				 dd.item_id, dd.qty * qp.qty AS qty\n" 
				+ "			FROM delivery_header AS dh\n" 
				+ "				 INNER JOIN delivery_detail AS dd ON dh.delivery_id = dd.delivery_id\n" 
				+ "				 INNER JOIN qty_per AS qp ON dd.uom = qp.uom AND dd.item_id = qp.item_id\n" 
				+ "				 INNER JOIN account AS a\n" 
				+ "					 ON dh.customer_id = a.customer_id AND a.start_date <= dh.delivery_date\n" 
				+ "				 INNER JOIN parameter AS p\n"
				+ "                  ON     dh.delivery_date BETWEEN p.start_date AND p.end_date\n" 
				+ "                     AND dd.item_id = p.item_id\n"
				+ "		   WHERE dh.actual > 0),\n" 
				+ "	 sold AS\n" 
				+ "		 (SELECT * FROM invoiced\n" 
				+ "		  UNION\n" 
				+ "		  SELECT * FROM delivered)\n" 
				+ "SELECT ROW_NUMBER() OVER (ORDER BY order_id),\n"
				+ " 	  order_id,\n"
				+ " 	  series,\n"
				+ " 	  order_date,\n"
				+ "		  name,\n" 
				+ "		  qty\n" 
				+ "  FROM sold AS s\n"
				+ "       INNER JOIN customer_master AS cm\n"
				+ "	         ON s.customer_id = cm.id\n"
				+ " WHERE     qty <> 0\n"
				+ (routeId == null ? "" : ("       AND route_id = " + routeId + "\n"))
				+ " ORDER BY order_id;" 
				);
	}

	public int getProductLineId() {
		return productLineId;
	}
}

package ph.txtdis.windows;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class OutletList extends RoutedData {
	private int productLineId;

	public OutletList(Date[] dates, int routeId, int productLineId) {
		type = Type.OUTLET_LIST;
		this.dates = dates == null ? getDefaultDates() : dates;
		this.routeId = routeId;
		this.productLineId = productLineId;

		// @sql:on
		tableHeaders = new String[][] { 
				{ StringUtils.center("#", 3), "Line" }, 
				{ StringUtils.center("ID", 7), "ID" },
		        { StringUtils.center("OUTLET", 28), "String" }, 
		        { StringUtils.center("QUANTITY", 9), "BigDecimal" } };

		tableData = new Query().getTableData(new Object[] { dates[0], dates[1], productLineId, routeId }, ""
				+ Item.addParentChildCTE() + ",\n" 
				+ "	 order_dates AS (SELECT cast (? AS date) AS start_date, cast (? AS date) AS end_date),\n" 
				+ "	 invoices AS\n" 
				+ "		 (SELECT DISTINCT ON(ih.invoice_id, ih.series, id.item_id)\n" 
				+ "				ih.invoice_id AS order_id,\n" 
				+ "				 ih.invoice_date AS order_date,\n" 
				+ "				 ih.series,\n" 
				+ "				 ih.customer_id,\n" 
				+ "				 ih.actual,\n" 
				+ "				 id.item_id,\n" 
				+ "				 id.qty,\n" 
				+ "				 id.uom,\n" 
				+ "				 id.qty * qp.qty AS pcs,\n" 
				+ "				 qp.qty AS qty_per,\n" 
				+ "				 CASE\n" 
				+ "					 WHEN a.route_id IS NULL THEN\n" 
				+ "						 0\n" 
				+ "					 ELSE\n" 
				+ "						 last_value (\n" 
				+ "							 a.route_id)\n" 
				+ "						 OVER (PARTITION BY ih.invoice_id, ih.series, id.item_id\n" 
				+ "							   ORDER BY a.start_date DESC)\n" 
				+ "				 END\n" 
				+ "					 AS route_id\n" 
				+ "			FROM invoice_header AS ih\n" 
				+ "				 INNER JOIN customer_header AS cm ON ih.customer_id = cm.id\n" 
				+ "				 INNER JOIN channel AS ch ON cm.type_id = ch.id\n" 
				+ "				 INNER JOIN invoice_detail AS id\n" 
				+ "					 ON ih.invoice_id = id.invoice_id AND ih.series = id.series\n" 
				+ "				 INNER JOIN qty_per AS qp ON id.uom = qp.uom AND id.item_id = qp.item_id\n" 
				+ "				 INNER JOIN order_dates ON ih.invoice_date BETWEEN start_date AND end_date\n" 
				+ "				 LEFT JOIN account AS a\n" 
				+ "					 ON ih.customer_id = a.customer_id AND ih.invoice_date >= a.start_date\n" 
				+ "		   WHERE ih.actual >= 0 AND ch.name <> 'OTHERS'),\n" 
				+ "	 deliveries AS\n" 
				+ "		 (SELECT DISTINCT ON(ih.delivery_id, series, id.item_id)\n" 
				+ "				ih.delivery_id AS order_id,\n" 
				+ "				 ih.delivery_date AS order_date,\n" 
				+ "				 cast (' ' AS text) AS series,\n" 
				+ "				 ih.customer_id,\n" 
				+ "				 ih.actual,\n" 
				+ "				 id.item_id,\n" 
				+ "				 id.qty,\n" 
				+ "				 id.uom,\n" 
				+ "				 id.qty * qp.qty AS pcs,\n" 
				+ "				 qp.qty AS qty_per,\n" 
				+ "				 CASE\n" 
				+ "					 WHEN a.route_id IS NULL THEN\n" 
				+ "						 0\n" 
				+ "					 ELSE\n" 
				+ "						 last_value (a.route_id)\n" 
				+ "						 OVER (PARTITION BY ih.delivery_id, id.item_id ORDER BY a.start_date DESC)\n" 
				+ "				 END\n" 
				+ "					 AS route_id\n" 
				+ "			FROM delivery_header AS ih\n" 
				+ "				 INNER JOIN customer_header AS cm ON ih.customer_id = cm.id\n" 
				+ "				 INNER JOIN channel AS ch ON cm.type_id = ch.id\n" 
				+ "				 INNER JOIN delivery_detail AS id ON ih.delivery_id = id.delivery_id\n" 
				+ "				 INNER JOIN order_dates ON ih.delivery_date BETWEEN start_date AND end_date\n" 
				+ "				 INNER JOIN qty_per AS qp ON id.uom = qp.uom AND id.item_id = qp.item_id\n" 
				+ "				 LEFT JOIN account AS a\n" 
				+ "					 ON ih.customer_id = a.customer_id AND ih.delivery_date >= a.start_date\n" 
				+ "		   WHERE ih.actual >= 0 AND ch.name <> 'OTHERS'),\n" 
				+ "	 report_qty AS\n" 
				+ "		 (SELECT item_id, qty\n" 
				+ "			FROM qty_per\n" 
				+ "		   WHERE report = TRUE),\n" 
				+ "	 sold AS\n" 
				+ "		 (SELECT * FROM invoices\n" 
				+ "		  UNION\n" 
				+ "		  SELECT * FROM deliveries),\n" 
				+ "	 prod_line AS\n" 
				+ "		 (	SELECT cm.id, cm.name, sum (i.pcs / rq.qty) AS qty\n" 
				+ "			  FROM sold AS i\n" 
				+ "				   INNER JOIN customer_header AS cm ON i.customer_id = cm.id\n" 
				+ "				   INNER JOIN channel AS ch ON cm.type_id = ch.id\n" 
				+ "				   INNER JOIN parent_child AS pc "
				+ "                   ON i.item_id = pc.child_id AND pc.parent_id = ?\n" 
				+ "				   INNER JOIN report_qty AS rq ON i.item_id = rq.item_id\n" 
				+ "			 WHERE i.route_id = ? AND ch.name <> 'OTHERS'\n" 
				+ "		  GROUP BY cm.id, cm.name)\n" 
				+ "  SELECT DISTINCT row_number () OVER (ORDER BY qty DESC),\n" 
				+ "				  id,\n" 
				+ "				  name,\n" 
				+ "				  qty\n" 
				+ "	FROM prod_line\n" 
				+ "   WHERE qty <> 0\n" 
				+ "ORDER BY qty DESC\n");
		// @sql:off
	}

	private Date[] getDefaultDates() {
	    Date[] dates;
	    Date lastMonth = DIS.addMonths(DIS.TODAY, -1);
	    dates = new Date[] { DIS.getFirstOfMonth(lastMonth), DIS.getLastOfMonth(lastMonth) };
	    return dates;
    }

	public int getProductLineId() {
		return productLineId;
	}

	@Override
	public String getSubheading() {
		return Item.getFamily(productLineId) + " sold by " + Route.getName(routeId) + "\nfrom "
		        + DIS.LONG_DATE.format(dates[0]) + " to " + DIS.LONG_DATE.format(dates[1]);
	}
}

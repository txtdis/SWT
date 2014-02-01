package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class SalesReport extends Data implements Subheaded {
	private boolean isPerRoute;
	private String metric;
	private int categoryId;

	public SalesReport() {
		super();
	}

	public SalesReport(Date[] dates, String metric, int categoryId, boolean isPerRoute) {
		type = Type.SALES_REPORT;
		this.metric = metric;
		this.categoryId = categoryId;
		this.isPerRoute = isPerRoute;
		if (dates == null)
			dates = new Date[] { DIS.getFirstOfMonth(DIS.TODAY), DIS.getLastOfMonth(DIS.TODAY) };
		this.dates = dates;
		String[] prodLines = Item.getProductLines(categoryId);
		int arraySize = prodLines.length;
		prodLines = ArrayUtils.add(prodLines, 0, "TOTAL");

		String sqlColumn = "";
		String sqlQty = "";
		String sqlTable = "";

		Object[] totals = new Object[arraySize + 4];
		tableHeaders = new String[arraySize + 4][];
		tableHeaders[0] = new String[] { StringUtils.center("#", 4), "Line" };
		tableHeaders[1] = new String[] { StringUtils.center("ID", 4), "ID" };
		tableHeaders[2] = new String[] { StringUtils.center("NAME", 29), "String" };
		int familyId;
		for (int i = 0; i < arraySize + 1; i++) {
			if (i == 0) {
				familyId = categoryId;
			} else {
				familyId = Item.getFamilyId(prodLines[i]);
			}
			tableHeaders[i + 3] = new String[] { StringUtils.center(prodLines[i], 7),
			        (metric.equals("SALES TO TRADE") ? "Quantity" : "Long") };
			if (metric.equals("SALES TO TRADE")) {
				// Sales to Trade
				if (isPerRoute) {
					// per Route
					sqlColumn = sqlColumn + "p" + i + " AS ( " + "SELECT i.route_id AS id, "
					        + "		  sum (i.qty) AS qty " + "  FROM sold AS i " + "       INNER JOIN parent_child AS pc "
					        + "          ON i.item_id = pc.child_id AND pc.parent_id  = " + familyId
					        + " GROUP BY i.route_id " + (i == arraySize ? ") " : "), ");
				} else {
					// per Outlet
					sqlColumn = sqlColumn + "" + "p" + i + " AS ( " + "SELECT	i.customer_id AS id, "
					        + "		SUM(i.qty) AS qty " + "  FROM	sold AS i  " + "       INNER JOIN	parent_child AS pc "
					        + "	       ON     i.item_id = pc.child_id" + "	          AND pc.parent_id  = " + familyId
					        + " " + " GROUP BY i.customer_id " + (i == arraySize ? ") " : "), ") + "";
				}
			} else {
				// Productivity
				sqlColumn = sqlColumn + "" + "p" + i + " AS ( " + "SELECT	i.route_id AS id, "
				        + "		COUNT(DISTINCT i.customer_id) AS qty " + "  FROM	sold AS i  "
				        + "       INNER JOIN parent_child AS pc " + "          ON     i.item_id = pc.child_id"
				        + "	          AND pc.parent_id  = " + familyId + " " + " WHERE i.route_id <> 6 "
				        + " GROUP BY i.route_id " + (i == arraySize ? ") " : "), ") + "";
			}
			sqlQty = sqlQty + "CASE WHEN " + "p" + i + ".qty IS null " + "	THEN 0 " + "	ELSE p" + i + ".qty "
			        + "END AS p" + i + "_qty " + (i == arraySize ? " " : ", ");
			sqlTable = sqlTable + "LEFT OUTER JOIN p" + i + " " + "	ON row.id = p" + i + ".id ";
		}
		String row = "";
		if (isPerRoute) {
			row = "row AS ( " + "	SELECT	r.id, " + "			r.name " + "	FROM 	route AS r " + "), ";
		} else {
			row = "row AS ( " + "	SELECT	cm.id, " + "			cm.name " + "	FROM 	customer_header AS cm " + "), ";// /
		}
		tableData = sql.getTableData(dates,""
				
		                        + Item.addParentChildCTE()
		                        + ",\n"
		                        + "order_dates AS (\n"
		                        + "	SELECT	CAST (? AS date) AS start,\n"
		                        + "			CAST (? AS date) AS end\n"
		                        + "),\n"
		                        + "invoiced AS ( "
		                        + "SELECT DISTINCT ON(ih.invoice_id, ih.series, item_id)\n"
		                        + "       ih.invoice_id AS order_id,\n"
		                        + "       ih.series,\n"
		                        + "       ih.invoice_date AS order_date,\n"
		                        + "       ih.customer_id,\n"
		                        + "       CASE WHEN a.route_id IS NULL "
		                        + "			  THEN 0\n"
		                        + "           ELSE last_value( a.route_id)\n"
		                        + "               OVER (PARTITION BY ih.invoice_id, ih.series, id.item_id ORDER BY a.start_date DESC)\n"
		                        + "           END AS route_id,\n"
		                        + "       id.item_id,\n"
		                        + "       id.qty * qp.qty / rq.qty AS qty\n"
		                        + "  FROM invoice_header AS ih\n"
		                        + "       INNER JOIN invoice_detail AS id ON ih.invoice_id = id.invoice_id AND ih.series = id.series\n"
		                        + "       INNER JOIN customer_header AS cm ON ih.customer_id = cm.id\n"
		                        + "       INNER JOIN channel AS ch ON cm.type_id = ch.id "
		                        + "	      INNER JOIN qty_per as qp ON id.uom = qp.uom AND id.item_id = qp.item_id "
		                        + "       INNER JOIN qty_per AS rq ON id.item_id = rq.item_id AND rq.report IS true "
		                        + " 	  INNER JOIN order_dates as od ON ih.invoice_date BETWEEN od.start AND od.end\n"
		                        + "       LEFT JOIN account AS a ON ih.customer_id = a.customer_id AND ih.invoice_date >= a.start_date\n"
		                        + " WHERE ih.actual >= 0\n"
		                        + "       AND ch.name <> 'OTHERS'), "
		                        + "delivered AS ( "
		                        + "SELECT DISTINCT ON(ih.delivery_id, series, id.item_id)\n"
		                        + "       ih.delivery_id AS order_id,\n"
		                        + "       ' ' AS series,\n"
		                        + "       ih.delivery_date AS order_date,\n"
		                        + "       ih.customer_id,\n"
		                        + "       CASE\n"
		                        + "           WHEN a.route_id IS NULL THEN 0\n"
		                        + "           ELSE last_value( a.route_id)\n"
		                        + "               OVER (PARTITION BY ih.delivery_id, id.item_id ORDER BY a.start_date DESC)\n"
		                        + "       END\n" + "           AS route_id,\n" + "       id.item_id,\n"
		                        + "       id.qty * qp.qty / rq.qty AS qty\n" + "  FROM delivery_header AS ih\n"
		                        + "       INNER JOIN delivery_detail AS id ON ih.delivery_id = id.delivery_id\n"
		                        + "       INNER JOIN customer_header AS cm ON ih.customer_id = cm.id\n"
		                        + "       INNER JOIN channel AS ch ON cm.type_id = ch.id "
		                        + "	      INNER JOIN qty_per as qp " + "		    ON     id.uom = qp.uom "
		                        + "			   AND id.item_id = qp.item_id " + "       INNER JOIN qty_per AS rq "
		                        + "	        ON     id.item_id = rq.item_id " + "	           AND rq.report IS true "
		                        + " 		INNER JOIN order_dates as od\n"
		                        + "			ON	   ih.delivery_date BETWEEN od.start AND od.end\n"
		                        + "       LEFT JOIN account AS a\n"
		                        + "           ON     ih.customer_id = a.customer_id\n"
		                        + "              AND ih.delivery_date >= a.start_date\n" + " WHERE ih.actual >= 0\n"
		                        + "       AND ch.name <> 'OTHERS' " + "), " + "sold AS (\n"
		                        + "		SELECT * FROM invoiced\n" + "		UNION\n" + "		SELECT * FROM delivered\n" + "), "
		                        + row + sqlColumn + "SELECT DISTINCT CAST (0 AS smallint), " + " 		row.id, "
		                        + " 		row.name, " + sqlQty + "FROM 	row " + sqlTable + " " + "WHERE p0.qty > 0 "
		                        + (isPerRoute ? "ORDER BY 1" : "ORDER BY 4 DESC "));
		BigDecimal total;
		for (int i = 0, routeCount = tableData.length; i < routeCount; i++) {
			total = BigDecimal.ZERO;
			for (int j = 4, productLineCount = tableData[i].length; j < productLineCount; j++) {
				total = total.add((BigDecimal) tableData[i][j]);
			}
			tableData[i][3] = total;
		}
	}

	public Object[][] getDataDump() {
		// @sql:on
		Object[][] objectArray = sql.getTableData(dates, ""
				+ "WITH latest\n" 
				+ "     AS (  SELECT customer_id, max (start_date) AS start_date\n"
		        + "             FROM account\n" 
				+ "         GROUP BY customer_id),\n" 
				+ "     latest_route\n"
		        + "     AS (SELECT account.customer_id, account.route_id\n" 
				+ "           FROM latest\n"
		        + "                INNER JOIN account\n"
		        + "                   ON     latest.customer_id = account.customer_id\n"
		        + "                      AND latest.start_date = account.start_date)\n"
		        + "  SELECT outlet.name AS outlet,\n" 
		        + "         route.name AS route,\n" 
		        + "         addy.street,\n"
		        + "         barangay.name AS barangay,\n" 
		        + "         city.name AS city,\n"
		        + "         province.name AS province,\n" 
		        + "         header.invoice_id AS invoice_id,\n"
		        + "         header.invoice_date AS invoice_date,\n" 
		        + "         item.name AS item,\n"
		        + "         prod_line.name AS product_line,\n" 
		        + "         category.name AS category,\n"
		        + "         detail.qty * per_unit.qty / report.qty AS qty\n" 
		        + "    FROM invoice_header AS header\n"
		        + "         INNER JOIN invoice_detail AS detail\n"
		        + "            ON     header.invoice_id = detail.invoice_id\n"
		        + "               AND header.series = header.series\n" 
		        + "               AND header.actual > 0\n"
		        + "				AND header.invoice_date BETWEEN ? AND ?\n" 
		        + "         INNER JOIN customer_header AS outlet\n"
		        + "            ON header.customer_id = outlet.id\n"
		        + "         LEFT OUTER JOIN item_tree AS prod_tree\n"
		        + "            ON prod_tree.child_id = detail.item_id\n"
		        + "         LEFT OUTER JOIN item_family AS prod_line\n"
		        + "            ON prod_line.id = prod_tree.parent_id " 
		        + "				AND prod_line.tier_id = 3\n"
		        + "         LEFT OUTER JOIN item_tree AS cat_tree\n"
		        + "            ON cat_tree.child_id = prod_tree.parent_id\n"
		        + "         LEFT OUTER JOIN item_family AS category\n"
		        + "            ON category.id = cat_tree.parent_id " 
		        + "				AND category.tier_id = 2\n"
		        + "         LEFT OUTER JOIN qty_per AS per_unit\n" 
		        + "            ON per_unit.uom = detail.uom "
		        + "				AND per_unit.item_id = detail.item_id\n" 
		        + "         LEFT OUTER JOIN qty_per AS report\n"
		        + "            ON report.item_id = detail.item_id " 
		        + "				AND report.report = TRUE\n"
		        + "         LEFT OUTER JOIN latest_route " 
		        + "			 ON outlet.id = latest_route.customer_id\n"
		        + "         LEFT OUTER JOIN route ON latest_route.route_id = route.id\n"
		        + "         LEFT OUTER JOIN address AS addy\n"
		        + "            ON addy.customer_id = header.customer_id\n"
		        + "         LEFT OUTER JOIN area AS barangay " 
		        + "			 ON addy.district = barangay.id\n"
		        + "         LEFT OUTER JOIN area AS city ON addy.city = city.id\n"
		        + "         LEFT OUTER JOIN area AS province " 
		        + "			 ON addy.province = province.id\n"
		        + "         LEFT OUTER JOIN item_header AS item " 
		        + "			 ON detail.item_id = item.id\n"
		        + "ORDER BY outlet;\n" );
		// @sql:off
		return objectArray;
	}

	public String getMetric() {
		return metric;
	}

	public boolean isPerRoute() {
		return isPerRoute;
	}

	@Override
	public String getSubheading() {
		String measure;
		if (metric.contains("SALES"))
			measure = "Sales to Trade of ";
		else
			measure = "Productivity for ";
		return Item.getFamily(categoryId) + "'S" + measure + " from " + DIS.LONG_DATE.format(dates[0]) + " to "
		        + DIS.LONG_DATE.format(dates[1]);
	}

	public int getCategoryId() {
		return categoryId;
	}
}

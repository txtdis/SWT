package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class LoadSettlement extends RoutedData {
 
	public LoadSettlement(Date[] dates, int routeId) {
		super(dates, routeId);
		type = Type.LOAD_SETTLEMENT;

		// @sql:on
		tableHeaders = new String[][] {{
		                StringUtils.center("#", 3), "Line" }, {
		                StringUtils.center("ID", 4), "ID" }, {
		                StringUtils.center("PRODUCT NAME", 18), "String" }, {
		                StringUtils.center("LOADED", 8), "Quantity" }, {
		                StringUtils.center("SOLD", 8), "Quantity" }, {
		                StringUtils.center("RETURNED", 8), "Quantity" }, {
		                StringUtils.center("KEPT", 8), "Quantity" }, {
		                StringUtils.center("GAIN(LOSS)", 10), "Quantity" }, {
		                StringUtils.center(DIS.$ + " VALUE", 12), "BigDecimal" } 
		                };

		tableData = new Query().getTableData(routeId,""
				// @sql:on
				+ "WITH "
				+ Sales.addCTE(start, end) + "),\n" 
				+ Invoice.addCTE(start, end) + "),\n" 
				+ Delivery.addCTE(start, end) + "),\n" 
				+ "	 invoiced_delivered AS\n" 
				+ "		 (SELECT * FROM invoiced\n" 
				+ "		  UNION\n" 
				+ "		  SELECT * FROM delivered),\n" 
				+ "	 sold AS\n" 
				+ "		 (	SELECT route_id, item_id, sum (qty) AS qty\n" 
				+ "			  FROM invoiced_delivered\n" 
				+ "		  GROUP BY route_id, item_id),\n" 
				+ Receiving.addCTE(start, end) + "),\n" 
				+ Count.addCTE(end) + "),\n" 
				+ "	 summary AS\n" 
				+ "		 (	SELECT l.item_id,\n" 
				+ "				   l.qty AS loaded_qty,\n" 
				+ "				   CASE WHEN s.qty IS NULL THEN 0 ELSE s.qty END AS sold_qty,\n" 
				+ "				   CASE WHEN r.qty IS NULL THEN 0 ELSE r.qty END AS returned_qty,\n" 
				+ "				   CASE WHEN k.qty IS NULL THEN 0 ELSE k.qty END AS kept_qty\n" 
				+ "			  FROM route\n" 
				+ "				   LEFT JOIN loaded AS l ON route.id = l.route_id\n" 
				+ "				   LEFT JOIN sold AS s ON route.id = s.route_id AND l.item_id = s.item_id\n" 
				+ "				   LEFT JOIN returned AS r ON route.id = r.route_id AND l.item_id = r.item_id\n" 
				+ "				   LEFT JOIN kept AS k ON route.id = k.route_id AND l.item_id = k.item_id\n" 
				+ "			 WHERE route.id = ?),\n" 
				+ "	 mother_price AS\n" 
				+ "		 (  SELECT DISTINCT ON (item_id)\n" 
				+ "				   item_id,\n" 
				+ "			       last_value(price) OVER(PARTITION BY item_id ORDER BY start_date DESC) AS price\n" 
				+ "			  FROM price\n" 
				+ "		     WHERE tier_id = 0),\n" 
				+ "	 child_price AS\n" 
				+ "		 (  SELECT bom.item_id,\n" 
				+ "			       p.price\n" 
				+ "			  FROM mother_price AS p\n" 
				+ "				   LEFT JOIN bom ON bom.part_id = p.item_id),\n" 
				+ "	 latest_price AS\n" 
				+ "		 (  SELECT * FROM mother_price\n" 
				+ "			 UNION\n" 
				+ "		    SELECT * FROM child_price),\n" 
				+ "  combined AS\n" 
				+ "      (  SELECT s.item_id,\n" 
				+ "		           im.short_id AS name,\n" 
				+ "		 	       CASE WHEN loaded_qty IS NULL THEN 0 ELSE loaded_qty END AS loaded_qty,\n" 
				+ "		           CASE WHEN sold_qty IS NULL THEN 0 ELSE sold_qty END AS sold_qty,\n" 
				+ "		           CASE WHEN returned_qty IS NULL THEN 0 ELSE returned_qty END AS returned_qty,\n" 
				+ "		           CASE WHEN kept_qty IS NULL THEN 0 ELSE kept_qty END AS kept_qty,\n" 
				+ "		           CASE WHEN price IS NULL THEN 0 ELSE price END AS price\n" 
				+ "	          FROM summary AS s\n" 
				+ "		           INNER JOIN item_header AS im ON s.item_id = im.id\n" 
				+ "		           LEFT JOIN latest_price AS p ON s.item_id = p.item_id),\n" 
				+ "  computed AS\n" 
				+ "      (  SELECT item_id,\n" 				+ "		    	   name,\n" 
				+ "		           loaded_qty,\n" 
				+ "		           sold_qty,\n" 
				+ "		           returned_qty,\n" 
				+ "		           kept_qty,\n" 
				+ "		           sold_qty + returned_qty + kept_qty - loaded_qty AS variance,\n" 
				+ "		           (sold_qty + returned_qty + kept_qty - loaded_qty) * price AS value,\n" 
				+ "		           sum((sold_qty + returned_qty + kept_qty - loaded_qty) * price) OVER() AS balance\n" 
				+ "	   FROM combined)\n" 
				+ "  SELECT CAST (row_number() OVER (ORDER BY value) AS int),\n" 
				+ "		    item_id,\n" 
				+ "		    name,\n" 
				+ "		    loaded_qty,\n" 
				+ "		    sold_qty,\n" 
				+ "		    returned_qty,\n" 
				+ "		    kept_qty,\n" 
				+ "		    variance,\n" 
				+ "		    value,\n" 
				+ "		    balance\n" 
				+ "	   FROM computed\n" 
				+ "   ORDER BY value;" 
				// @sql:off
				);
	}

	public BigDecimal getTotalVariance() {
		return tableData == null || tableData[0][9] == null ? BigDecimal.ZERO : (BigDecimal) tableData[0][9];
	}
}

package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class LoadSettlement extends Report implements Startable {

	public LoadSettlement() {}

	public LoadSettlement(Date[] dates, int routeId) {
		super();
		module = "Load Settlement";
		dates = dates == null ? new Date[] { DIS.TODAY, DIS.TODAY } : dates;
		this.dates = dates;
		this.routeId = routeId;

		// @sql:on
		headers = new String[][] {{
		                StringUtils.center("#", 3), "Line" }, {
		                StringUtils.center("ID", 4), "ID" }, {
		                StringUtils.center("PRODUCT NAME", 18), "String" }, {
		                StringUtils.center("LOADED", 8), "Quantity" }, {
		                StringUtils.center("SOLD", 8), "Quantity" }, {
		                StringUtils.center("RETURNED", 8), "Quantity" }, {
		                StringUtils.center("KEPT", 8), "Quantity" }, {
		                StringUtils.center("GAIN(LOSS)", 10), "Quantity" }, {
		                StringUtils.center(DIS.CURRENCY_SIGN + " VALUE", 12), "BigDecimal" } 
		                };

		data = new Data().getDataArray(new Object[] { dates[0], dates[1], routeId },""
				// @sql:on
				+ "WITH parameter AS\n" 
				+ "		 (SELECT cast (? AS date) AS start_date, cast (? AS date) AS end_date),\n" 
				+ "	 booked AS\n" 
				+ "		 (SELECT DISTINCT ON(sh.sales_id, sd.item_id)\n" 
				+ "			     sh.sales_id, "
				+ "				 last_value( a.route_id)\n" 
				+ "				     OVER (PARTITION BY sh.sales_id, sd.item_id ORDER BY a.start_date desc) AS route_id,\n" 
				+ "				 sd.item_id, sd.qty * qp.qty AS qty\n" 
				+ "			FROM sales_header AS sh\n" 
				+ "				 INNER JOIN sales_detail AS sd ON sh.sales_id = sd.sales_id\n" 
				+ "				 INNER JOIN qty_per AS qp ON sd.uom = qp.uom AND sd.item_id = qp.item_id\n" 
				+ "				 INNER JOIN account AS a\n" 
				+ "					 ON sh.customer_id = a.customer_id AND a.start_date <= sh.sales_date\n" 
				+ "				 INNER JOIN parameter AS p ON sh.sales_date BETWEEN p.start_date AND p.end_date),\n" 
				+ "	 loaded AS\n" 
				+ "		 (	SELECT route_id, item_id, sum (qty) AS qty\n" 
				+ "			  FROM booked\n" 
				+ "		  GROUP BY route_id, item_id),\n" 
				+ "	 invoiced AS\n" 
				+ "		 (SELECT DISTINCT ON(ih.invoice_id, ih.series, id.item_id)\n" 
				+ "			     ih.invoice_id AS order_id, "
				+ "			     ih.series, "
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
				+ "				 INNER JOIN parameter AS p ON ih.invoice_date BETWEEN p.start_date AND p.end_date\n" 
				+ "		   WHERE ih.actual > 0),\n" 
				+ "	 delivered AS\n" 
				+ "		 (SELECT DISTINCT ON(dh.delivery_id, dd.item_id)\n" 
				+ "			     -dh.delivery_id AS order_id, "
				+ "			     CAST('DR' AS text) AS series, "
				+ "				 last_value( a.route_id)\n" 
				+ "				     OVER (PARTITION BY dh.delivery_id, dd.item_id ORDER BY a.start_date DESC) AS route_id,\n" 
				+ "				 dd.item_id, dd.qty * qp.qty AS qty\n" 
				+ "			FROM delivery_header AS dh\n" 
				+ "				 INNER JOIN delivery_detail AS dd ON dh.delivery_id = dd.delivery_id\n" 
				+ "				 INNER JOIN qty_per AS qp ON dd.uom = qp.uom AND dd.item_id = qp.item_id\n" 
				+ "				 INNER JOIN account AS a\n" 
				+ "					 ON dh.customer_id = a.customer_id AND a.start_date <= dh.delivery_date\n" 
				+ "				 INNER JOIN parameter AS p ON dh.delivery_date BETWEEN p.start_date AND p.end_date\n" 
				+ "		   WHERE dh.actual > 0),\n" 
				+ "	 invoiced_delivered AS\n" 
				+ "		 (SELECT * FROM invoiced\n" 
				+ "		  UNION\n" 
				+ "		  SELECT * FROM delivered),\n" 
				+ "	 sold AS\n" 
				+ "		 (	SELECT route_id, item_id, sum (qty) AS qty\n" 
				+ "			  FROM invoiced_delivered\n" 
				+ "		  GROUP BY route_id, item_id),\n" 
				+ "	 received_per_rr AS\n" 
				+ "		 (SELECT rh.receiving_id,\n"
				+ "			     rh.partner_id,\n"				
				+ "			     rh.receiving_date,\n"				
				+ "				 rd.item_id,\n"
				+ "				 sum(rd.qty * qp.qty) AS qty\n" 
				+ "			FROM receiving_header AS rh\n" 
				+ "				 INNER JOIN receiving_detail AS rd ON rh.receiving_id = rd.receiving_id\n" 
				+ "				 INNER JOIN qty_per AS qp ON rd.uom = qp.uom AND rd.item_id = qp.item_id\n" 
				+ "				 INNER JOIN parameter AS p ON rh.receiving_date BETWEEN p.start_date AND p.end_date\n" 
				+ "		   WHERE rh.ref_id > 0 AND rd.qc_id = 0\n" 
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
				+ "		  GROUP BY route_id, item_id),\n" 
				+ "	 counted AS\n" 
				+ "		 (SELECT DISTINCT ON(ch.count_id, cd.item_id)\n" 
				+ "			     ch.count_id, "
				+ "				 last_value( a.route_id)\n" 
				+ "				     OVER (PARTITION BY ch.count_id, cd.item_id ORDER BY a.start_date DESC) AS route_id,\n" 
				+ "				 cd.item_id, (cd.qty * qp.qty) AS qty\n" 
				+ "			FROM count_header AS ch\n" 
				+ "				 INNER JOIN count_detail AS cd ON ch.count_id = cd.count_id\n" 
				+ "				 INNER JOIN qty_per AS qp ON cd.uom = qp.uom AND cd.item_id = qp.item_id\n" 
				+ "				 INNER JOIN location ON location.id = ch.location_id\n" 
				+ "				 INNER JOIN customer_master AS cm ON location.name = cm.name\n" 
				+ "				 INNER JOIN account AS a ON cm.id = a.customer_id AND a.start_date <= ch.count_date\n" 
				+ "				 INNER JOIN parameter AS p ON ch.count_date BETWEEN p.start_date AND p.end_date),\n" 
				+ "	 kept AS\n" 
				+ "		 (	SELECT route_id, item_id, sum (qty) AS qty\n" 
				+ "			  FROM counted\n" 
				+ "		  GROUP BY route_id, item_id),\n" 
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
				+ "		           INNER JOIN item_master AS im ON s.item_id = im.id\n" 
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
				+ "  SELECT row_number() OVER (ORDER BY value),\n" 
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
		return data[0][9] == null ? BigDecimal.ZERO : (BigDecimal) data[0][9];
	}

	@Override
    public void start() {
		new SettlementView(new LoadSettlement(null, 1));
	}
}

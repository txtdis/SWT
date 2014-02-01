package ph.txtdis.windows;


public class SQL {
	public final static boolean NO_CUTOFF_DATES = false;
	public final static boolean WITH_CUTOFF_DATES = true;
	public final static boolean NO_ROUTES = false;
	public final static boolean PER_ROUTE = true;
	public final static int NO_REFERENCE = 0;

	public static String addCreditTermStmt() {
		return  // @sql:on
				"        latest_credit_term_date\n"
		        + "        AS (  SELECT customer_id, max (start_date) AS start_date\n"
		        + "                FROM credit\n" 
		        + "            GROUP BY customer_id),\n"
		        + "        latest_credit_term\n" 
		        + "        AS (SELECT cd.customer_id, cd.term\n"
		        + "              FROM credit AS cd\n"
		        + "                   INNER JOIN latest_credit_term_date AS lctd\n"
		        + "                      ON     cd.customer_id = lctd.customer_id\n"
		        + "                         AND cd.start_date = lctd.start_date)"
		        // @sql:off
		        ;
	}

	public static String addRouteLatestStmt() {
		return  // @sql:on
				"        latest_route_date\n"
		        + "        AS (  SELECT customer_id, max (start_date) AS start_date\n"
		        + "                FROM account\n" 
		        + "            GROUP BY customer_id),\n"
		        + "        latest_route\n" 
		        + "        AS (SELECT cd.customer_id, cd.route_id\n"
		        + "              FROM account AS cd\n"
		        + "                   INNER JOIN latest_route_date AS lctd\n"
		        + "                      ON     cd.customer_id = lctd.customer_id\n"
		        + "                         AND cd.start_date = lctd.start_date)"
		        // @sql:off
		        ;
	}

	public static String addPaymentStmt() {
		return  // @sql:on
				"  payment AS ( " 
				+ "      SELECT rd.order_id, " 
				+ "             rd.series, "
				+ "             max (rd.remit_id) as remit_id, "
		        + "             sum (CASE WHEN rd.payment IS NULL THEN 0 ELSE rd.payment END) AS payment "
		        + "        FROM remit_detail AS rd\n"
		        + "             INNER JOIN remit_header AS rh\n"
		        + "                ON rh.remit_id = rd.remit_id\n"
		        + "       WHERE remit_date <= current_date\n"
		        + "    GROUP BY order_id, " 
		        + "             series "
		        + ") "
		        // @sql:off
		        ;
	}

	
	public static String addSTTperDayStmt() {
		return // @sql:on
				  "     dates\n" 
				+ "     AS (SELECT current_date - 30 AS past_start,\n" 
				+ "                current_date AS past_end,\n" 
				+ "                current_date - 30 - INTERVAL '1 year' AS forecast_start,\n" 
				+ "                current_date - INTERVAL '1 year' AS forecast_end),\n" 
				+ "     stt_sold_bundled\n" 
				+ "     AS (  SELECT ih.invoice_date AS order_date, bom.part_id AS item_id,\n" 
				+ "                  sum (id.qty * bom.qty * qp.qty) AS qty\n" 
				+ "             FROM invoice_header AS ih\n" 
				+ "                  INNER JOIN invoice_detail AS id\n" 
				+ "                     ON ih.invoice_id = id.invoice_id AND ih.series = id.series AND ih.actual > 0\n" 
				+ "                  INNER JOIN dates\n" 
				+ "                     ON ih.invoice_date BETWEEN dates.past_start\n" 
				+ "                                            AND past_end\n" 
				+ "                     OR ih.invoice_date BETWEEN dates.forecast_start\n" 
				+ "                                            AND forecast_end\n" 
				+ "                  INNER JOIN bom ON id.item_id = bom.item_id AND bom.is_free IS NOT TRUE\n" 
				+ "                  INNER JOIN qty_per AS qp\n" 
				+ "                     ON bom.uom = qp.uom AND bom.part_id = qp.item_id\n" 
				+ "         GROUP BY ih.invoice_date, bom.part_id),\n" 
				+ "     stt_sold_as_is\n" 
				+ "     AS (  SELECT ih.invoice_date AS order_date, id.item_id, sum (id.qty * qp.qty) AS qty\n" 
				+ "             FROM invoice_header AS ih\n" 
				+ "                  INNER JOIN invoice_detail AS id\n" 
				+ "                     ON ih.invoice_id = id.invoice_id AND ih.series = id.series AND ih.actual > 0\n" 
				+ "                  INNER JOIN qty_per AS qp\n" 
				+ "                     ON id.uom = qp.uom AND id.item_id = qp.item_id\n" 
				+ "                  INNER JOIN dates\n" 
				+ "                     ON ih.invoice_date BETWEEN dates.past_start\n" 
				+ "                                            AND past_end\n" 
				+ "                     OR ih.invoice_date BETWEEN dates.forecast_start\n" 
				+ "                                            AND forecast_end\n" 
				+ "                  INNER JOIN item_header AS im\n" 
				+ "                     ON id.item_id = im.id AND im.type_id <> 2\n" 
				+ "         GROUP BY ih.invoice_date, id.item_id),\n" 
				+ "     stt_delivered_bundled\n" 
				+ "     AS (  SELECT ih.delivery_date AS order_date, bom.part_id AS item_id,\n" 
				+ "                  sum (id.qty * bom.qty * qp.qty) AS qty\n" 
				+ "             FROM delivery_header AS ih\n" 
				+ "                  INNER JOIN delivery_detail AS id\n" 
				+ "                     ON ih.delivery_id = id.delivery_id AND ih.actual > 0\n" 
				+ "                  INNER JOIN dates\n" 
				+ "                     ON ih.delivery_date BETWEEN dates.past_start\n" 
				+ "                                            AND past_end\n" 
				+ "                     OR ih.delivery_date BETWEEN dates.forecast_start\n" 
				+ "                                            AND forecast_end\n" 
				+ "                  INNER JOIN bom ON id.item_id = bom.item_id AND bom.is_free IS NOT TRUE\n" 
				+ "                  INNER JOIN qty_per AS qp\n" 
				+ "                     ON bom.uom = qp.uom AND bom.part_id = qp.item_id\n" 
				+ "         GROUP BY ih.delivery_date, bom.part_id),\n" 
				+ "     stt_delivered_as_is\n" 
				+ "     AS (  SELECT ih.delivery_date AS order_date, id.item_id, sum (id.qty * qp.qty) AS qty\n" 
				+ "             FROM delivery_header AS ih\n" 
				+ "                  INNER JOIN delivery_detail AS id\n" 
				+ "                     ON ih.delivery_id = id.delivery_id AND ih.actual > 0\n" 
				+ "                  INNER JOIN qty_per AS qp\n" 
				+ "                     ON id.uom = qp.uom AND id.item_id = qp.item_id\n" 
				+ "                  INNER JOIN dates\n" 
				+ "                     ON ih.delivery_date BETWEEN dates.past_start\n" 
				+ "                                            AND past_end\n" 
				+ "                     OR ih.delivery_date BETWEEN dates.forecast_start\n" 
				+ "                                            AND forecast_end\n" 
				+ "                  INNER JOIN item_header AS im\n" 
				+ "                     ON id.item_id = im.id AND im.type_id <> 2\n" 
				+ "         GROUP BY ih.delivery_date, id.item_id),\n" 
				+ "     stt_combined\n" 
				+ "     AS (SELECT * FROM stt_sold_bundled\n" 
				+ "         UNION\n" 
				+ "         SELECT * FROM stt_sold_as_is\n" 
				+ "         UNION\n" 
				+ "         SELECT * FROM stt_delivered_bundled\n" 
				+ "         UNION\n" 
				+ "         SELECT * FROM stt_delivered_as_is),\n" 
				+ "     selling_day\n" 
				+ "     AS (  SELECT count(DISTINCT order_date) AS selling_days\n" 
				+ "             FROM stt_combined),\n" 
				+ "     stt_per_day AS (\n" 
				+ "     SELECT item_id AS id, sum(qty) / selling_days AS qty\n" 
				+ "             FROM stt_combined, selling_day\n" 
				+ "         GROUP BY item_id, selling_days\n" 
				+ "     ORDER BY item_id)\n" 
				// @sql:off
				;
		
	}

	public static String addLatestPriceStmt(boolean isWithCutoffDate) {
		String cutoffDate = "";
		if (isWithCutoffDate)
			cutoffDate = "" 
				// @sql:on
		        + "INNER JOIN parameter AS pm\n" 
				+ "   ON price.start_date <= pm.start_date\n";
		return    "price_cutoff_date\n"
				+ "AS (SELECT price.item_id AS cutoff_item_id,\n"
				+ "           max(price.start_date) AS cutoff_date\n"
				+ "      FROM price\n"
				+ cutoffDate
				+ "     WHERE tier_id = 1\n"
				+ "  GROUP BY item_id\n),\n"
				+ "latest_price\n"
				+ "AS (SELECT item_id,\n"
				+ "           price\n"
				+ "      FROM price\n"
				+ "	          INNER JOIN price_cutoff_date AS cutoff\n"
				+ "              ON     price.start_date = cutoff_date\n"
				+ "				    AND price.item_id = cutoff_item_id\n"
				+ "     WHERE tier_id = 1)\n";
				// @sql:off
	}
}

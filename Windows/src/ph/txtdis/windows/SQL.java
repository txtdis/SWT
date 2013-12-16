package ph.txtdis.windows;

import java.sql.Date;

public class SQL {
	public final static boolean NO_CUTOFF_DATES = false;
	public final static boolean WITH_CUTOFF_DATES = true;
	public final static boolean NO_ROUTES = false;
	public final static boolean PER_ROUTE = true;
	public final static int NO_REFERENCE = 0;

	public static String addLatestPriceStmt() {
		return addLatestPriceStmt(WITH_CUTOFF_DATES);
	}

	public static String addCreditTermStmt() {
		return  // @sql:on
				"        latest_credit_term_date\n"
		        + "        AS (  SELECT customer_id, max (start_date) AS start_date\n"
		        + "                FROM credit_detail\n" 
		        + "            GROUP BY customer_id),\n"
		        + "        latest_credit_term\n" 
		        + "        AS (SELECT cd.customer_id, cd.term\n"
		        + "              FROM credit_detail AS cd\n"
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

	public static String addItemParentStmt() {
		return  // @sql:on
				"WITH RECURSIVE parent_child (child_id, parent_id) " 
				+ "AS ( " 
				+ " SELECT it.child_id, "
		        + "        it.parent_id " 
				+ "   FROM item_tree AS it " 
		        + "  UNION ALL "
		        + " SELECT parent_child.child_id, " 
		        + "        it.parent_id " 
		        + "   FROM item_tree it "
		        + "   JOIN parent_child " 
		        + "     ON it.child_id = parent_child.parent_id " 
		        + " ) "
		        // @sql:off
		        ;
	}

	public static String addPaymentStmt() {
		return  // @sql:on
				"payment " 
				+ "AS ( " 
				+ " SELECT order_id, " 
				+ "        series, "
		        + "        sum (CASE WHEN payment IS NULL THEN 0 ELSE payment END) AS payment "
		        + "   FROM remittance_detail " 
		        + "  GROUP BY order_id, " 
		        + "           series "
		        + "  ORDER BY order_id, " 
		        + "           series " 
		        + ") "
		        // @sql:off
		        ;
	}

	public static String addInventoryStmt() {
		return  // @sql:on
				"        last_count\n" 
				+ "        AS (SELECT max (count_date) AS count_date FROM count_closure),\n"
		        + "        stock_take\n" 
				+ "        AS (  SELECT id.item_id, qc_id, sum (id.qty * qp.qty) AS qty\n"
		        + "                FROM count_header AS ih\n"
		        + "                     INNER JOIN count_detail AS id ON ih.count_id = id.count_id\n"
		        + "                     INNER JOIN qty_per AS qp\n"
		        + "                        ON id.uom = qp.uom AND id.item_id = qp.item_id\n"
		        + "                     INNER JOIN last_count\n"
		        + "                        ON ih.count_date = last_count.count_date\n"
		        + "                     INNER JOIN item_master AS im\n"
		        + "                        ON id.item_id = im.id AND im.type_id <> 2\n"
		        + "            GROUP BY id.item_id, qc_id),\n" 
		        + "        adjustment\n"
		        + "        AS (SELECT item_id, qc_id, qty\n" 
		        + "              FROM count_adjustment\n"
		        + "                   INNER JOIN last_count\n"
		        + "                      ON count_adjustment.count_date = last_count.count_date),\n"
		        + "        adjusted\n" 
		        + "        AS (SELECT * FROM stock_take\n" 
		        + "            UNION\n"
		        + "            SELECT * FROM adjustment),\n" 
		        + "        beginning\n"
		        + "        AS (  SELECT item_id, qc_id, sum (qty) AS qty\n" 
		        + "                FROM adjusted\n"
		        + "            GROUP BY item_id, qc_id),\n" 
		        + "        brought_in\n"
		        + "        AS (  SELECT id.item_id, qc_id, sum (id.qty * qp.qty) AS qty\n"
		        + "                FROM receiving_header AS ih\n"
		        + "                     INNER JOIN receiving_detail AS id ON ih.receiving_id = id.receiving_id\n"
		        + "                     INNER JOIN qty_per AS qp\n"
		        + "                        ON id.uom = qp.uom AND id.item_id = qp.item_id\n"
		        + "                     INNER JOIN last_count\n"
		        + "                        ON ih.receiving_date BETWEEN last_count.count_date\n"
		        + "                                          AND current_date\n"
		        + "               WHERE partner_id = 488 OR ref_id < 0 OR qc_id <> 0\n"
		        + "            GROUP BY id.item_id, qc_id),\n" 
		        + "        sold_bundled\n"
		        + "        AS (  SELECT bom.part_id AS item_id,\n"
		        + "                     sum (id.qty * bom.qty * qp.qty) AS qty\n"
		        + "                FROM invoice_header AS ih\n"
		        + "                     INNER JOIN invoice_detail AS id\n"
		        + "                        ON ih.invoice_id = id.invoice_id AND ih.series = id.series\n"
		        + "                     INNER JOIN last_count\n"
		        + "                        ON ih.invoice_date BETWEEN last_count.count_date\n"
		        + "                                               AND current_date\n"
		        + "                     INNER JOIN bom ON id.item_id = bom.item_id\n"
		        + "                     INNER JOIN qty_per AS qp\n"
		        + "                        ON bom.uom = qp.uom AND bom.part_id = qp.item_id\n"
		        + "            GROUP BY bom.part_id),\n" + "        sold_as_is\n"
		        + "        AS (  SELECT id.item_id, sum (id.qty * qp.qty) AS qty\n"
		        + "                FROM invoice_header AS ih\n"
		        + "                     INNER JOIN invoice_detail AS id\n"
		        + "                        ON ih.invoice_id = id.invoice_id AND ih.series = id.series\n"
		        + "                     INNER JOIN qty_per AS qp\n"
		        + "                        ON id.uom = qp.uom AND id.item_id = qp.item_id\n"
		        + "                     INNER JOIN last_count\n"
		        + "                        ON ih.invoice_date BETWEEN last_count.count_date\n"
		        + "                                               AND current_date\n"
		        + "                     INNER JOIN item_master AS im\n"
		        + "                        ON id.item_id = im.id AND im.type_id <> 2\n"
		        + "            GROUP BY id.item_id),\n" 
		        + "        sold_combined\n"
		        + "        AS (SELECT * FROM sold_bundled\n" 
		        + "            UNION\n"
		        + "            SELECT * FROM sold_as_is),\n" 
		        + "        sold\n"
		        + "        AS (  SELECT item_id, sum (qty) AS qty\n" 
		        + "                FROM sold_combined\n"
		        + "            GROUP BY item_id),\n" 
		        + "        delivered\n"
		        + "        AS (  SELECT id.item_id, sum (id.qty * qp.qty) AS qty\n"
		        + "                FROM delivery_header AS ih\n"
		        + "                     INNER JOIN delivery_detail AS id\n"
		        + "                        ON ih.delivery_id = id.delivery_id\n"
		        + "                     INNER JOIN qty_per AS qp\n"
		        + "                        ON id.uom = qp.uom AND id.item_id = qp.item_id\n"
		        + "                     INNER JOIN last_count\n"
		        + "                        ON ih.delivery_date BETWEEN last_count.count_date\n"
		        + "                                                AND current_date\n"
		        + "            GROUP BY id.item_id),\n" 
		        + "        sent_out_combined\n"
		        + "        AS (SELECT * FROM sold\n" 
		        + "            UNION\n"
		        + "            SELECT * FROM delivered),\n" 
		        + "        sent_out\n"
		        + "        AS (  SELECT item_id, 0 AS qc_id, sum (qty) AS qty\n"
		        + "                FROM sent_out_combined\n" 
		        + "            GROUP BY item_id, qc_id),\n"
		        + "        good\n" + "        AS (SELECT im.id,\n" 
		        + "                   0 AS qc_id,\n"
		        + "                     CASE\n" 
		        + "                        WHEN beginning.qty IS NULL THEN 0\n"
		        + "                        ELSE beginning.qty\n" 
		        + "                     END\n"
		        + "                   + CASE\n" 
		        + "                        WHEN brought_in.qty IS NULL THEN 0\n"
		        + "                        ELSE brought_in.qty\n" 
		        + "                     END\n"
		        + "                   - CASE WHEN sent_out.qty IS NULL THEN 0 ELSE sent_out.qty END\n"
		        + "                      AS ending\n" 
		        + "              FROM item_master AS im\n"
		        + "                   LEFT JOIN beginning\n"
		        + "                      ON im.id = beginning.item_id AND beginning.qc_id = 0\n"
		        + "                   LEFT JOIN brought_in\n"
		        + "                      ON im.id = brought_in.item_id AND brought_in.qc_id = 0\n"
		        + "                   LEFT JOIN sent_out\n"
		        + "                      ON im.id = sent_out.item_id AND sent_out.qc_id = 0\n"
		        + "             WHERE    beginning.qty IS NOT NULL\n"
		        + "                   OR brought_in.qty IS NOT NULL\n"
		        + "                   OR sent_out.qty IS NOT NULL),\n" 
		        + "        on_hold\n"
		        + "        AS (SELECT im.id,\n" 
		        + "                   1 AS qc_id,\n" 
		        + "                     CASE\n"
		        + "                        WHEN beginning.qty IS NULL THEN 0\n"
		        + "                        ELSE beginning.qty\n" 
		        + "                     END\n"
		        + "                   + CASE\n" 
		        + "                        WHEN brought_in.qty IS NULL THEN 0\n"
		        + "                        ELSE brought_in.qty\n" 
		        + "                     END\n"
		        + "                   - CASE WHEN sent_out.qty IS NULL THEN 0 ELSE sent_out.qty END\n"
		        + "                      AS ending\n" 
		        + "              FROM item_master AS im\n"
		        + "                   LEFT JOIN beginning\n"
		        + "                      ON im.id = beginning.item_id AND beginning.qc_id = 1\n"
		        + "                   LEFT JOIN brought_in\n"
		        + "                      ON im.id = brought_in.item_id AND brought_in.qc_id = 1\n"
		        + "                   LEFT JOIN sent_out\n"
		        + "                      ON im.id = sent_out.item_id AND sent_out.qc_id = 1\n"
		        + "             WHERE    beginning.qty IS NOT NULL\n"
		        + "                   OR brought_in.qty IS NOT NULL\n"
		        + "                   OR sent_out.qty IS NOT NULL),\n" 
		        + "        bad\n"
		        + "        AS (SELECT im.id,\n" 
		        + "                   2 AS qc_id,\n" 
		        + "                     CASE\n"
		        + "                        WHEN beginning.qty IS NULL THEN 0\n"
		        + "                        ELSE beginning.qty\n" 
		        + "                     END\n"
		        + "                   + CASE\n" 
		        + "                        WHEN brought_in.qty IS NULL THEN 0\n"
		        + "                        ELSE brought_in.qty\n" 
		        + "                     END\n"
		        + "                   - CASE WHEN sent_out.qty IS NULL THEN 0 ELSE sent_out.qty END\n"
		        + "                      AS ending\n" 
		        + "              FROM item_master AS im\n"
		        + "                   LEFT JOIN beginning\n"
		        + "                      ON im.id = beginning.item_id AND beginning.qc_id = 2\n"
		        + "                   LEFT JOIN brought_in\n"
		        + "                      ON im.id = brought_in.item_id AND brought_in.qc_id = 2\n"
		        + "                   LEFT JOIN sent_out\n"
		        + "                      ON im.id = sent_out.item_id AND sent_out.qc_id = 2\n"
		        + "             WHERE    beginning.qty IS NOT NULL\n"
		        + "                   OR brought_in.qty IS NOT NULL\n"
		        + "                   OR sent_out.qty IS NOT NULL),\n" 
		        + "        inventory\n"
		        + "        AS (  SELECT row_number ()\n" 
		        + "                     OVER (\n"
		        + "                        ORDER BY\n"
		        + "                           CASE WHEN bad.ending IS NULL THEN 0 ELSE bad.ending END DESC,\n"
		        + "                           CASE\n"
		        + "                              WHEN on_hold.ending IS NULL THEN 0\n"
		        + "                              ELSE on_hold.ending\n" 
		        + "                           END DESC,\n"
		        + "                           CASE\n"
		        + "                              WHEN good.ending IS NULL THEN 0\n"
		        + "                              ELSE good.ending\n" 
		        + "                           END DESC)\n"
		        + "                        AS line,\n" 
		        + "                     im.id,\n"
		        + "                     im.name,\n" 
		        + "                     good.ending AS good,\n"
		        + "                     on_hold.ending AS on_hold,\n" 
		        + "                     bad.ending AS bad\n"
		        + "                FROM item_master AS im\n"
		        + "                     LEFT JOIN good ON im.id = good.id\n"
		        + "                     LEFT JOIN on_hold ON im.id = on_hold.id\n"
		        + "                     LEFT JOIN bad ON im.id = bad.id\n"
		        + "               WHERE good.ending > 0 OR on_hold.ending > 0 OR bad.ending > 0\n"
		        + "            ORDER BY CASE WHEN bad.ending IS NULL THEN 0 ELSE bad.ending END DESC,\n"
		        + "                     CASE\n" 
		        + "                        WHEN on_hold.ending IS NULL THEN 0\n"
		        + "                        ELSE on_hold.ending\n" 
		        + "                     END DESC,\n"
		        + "                     CASE WHEN good.ending IS NULL THEN 0 ELSE good.ending END DESC)\n";
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

	public static String addLatestRouteStmt() {
		return addLatestRouteStmt(DIS.FAR_PAST);
	}

	public static String addParametizedLatestRouteStmt() {
		return addLatestRouteStmt(null);
	}

	public static String addLatestRouteStmt(Date cutoff) {
		String cutoffDate;
		if (cutoff == null)
			// @sql:on
			cutoffDate = ""
				+ "INNER JOIN parameter AS pm\n"
				+ "   ON account.start_date <= pm.start_date\n";
			// @sql:off
		else
			cutoffDate = "WHERE account.start_date <= '" + cutoff.toString() + "'\n";
		return	""
				// @sql:on
				+ "  route_cutoff_date\n"
				+ "AS (SELECT account.customer_id AS cutoff_customer_id,\n"
				+ "           max(account.start_date) AS cutoff_date\n"
				+ "      FROM account\n"
				+ cutoffDate
				+ "  GROUP BY account.customer_id),\n"
				+ "latest_route\n"
				+ "AS (SELECT route_id as id,\n"
				+ "           customer_id\n"
				+ "      FROM account\n"
				+ "	          INNER JOIN route_cutoff_date AS cutoff\n"
				+ "              ON     account.start_date = cutoff_date\n"
				+ "				    AND account.customer_id = cutoff_customer_id)\n";
				// @sql:off
	}

	public static String addMovedQtyStmt(String order, boolean isWithCutoffDates, boolean isPerRoute, int referenceId) {
		String cutoffDates = "";
		String series = "";
		String route = "";
		String reference = "";
		String notAnRRfromPO = "";

		if (isWithCutoffDates)
			cutoffDates = ""
				// @sql:on
				+ "INNER JOIN parameter AS pm\n"
				+ "       ON ih." + order + "_date BETWEEN pm.start_date AND pm.end_date\n"
				// @sql:off
				;
		
		if (isPerRoute) {
			String customer = "AND ih.customer_id = latest_route.customer_id\n";
			if (order.equals("receiving")) {
				notAnRRfromPO = "AND ih.ref_id > 0";
				customer = "AND ih.partner_id = latest_route.customer_id\n";
			} else if (order.equals("count")) {
				customer = ""
				// @sql:on
				+ " INNER JOIN customer_master as customer\n"
				+ "	   ON customer.id = latest_route.customer_id\n"
				+ " INNER JOIN location\n"
				+ "    ON     location.id = ih.location_id\n" 
				+ "	      AND customer.name = location.name\n"
				// @sql:on
				;
			}

			route = ""
				// @sql:on
				+ "INNER JOIN latest_route\n"
				+ " ON pm.route_id = latest_route.id\n"
				+ customer			
				// @sql:off
				;
		}

		if (referenceId != 0) {
			if (order.equals("count")) {
				String referenceType = referenceId < 0 ? "purchase" : "sales";
				reference = "" 
				// @sql:on
				+ "INNER JOIN location AS loc\n" 
				+ "	    ON loc.id = ih.location_id\n"
			    + "  INNER JOIN customer_master AS cm\n" 
				+ "		ON loc.name = cm.name\n" 
			    + "  INNER JOIN " + referenceType + "_header AS so\n" 
			    + "		ON cm.id = so.customer_id\n" 
			    + "WHERE so." + referenceType + "_id = " + referenceId + "\n"
			    // @sql:off
			    ;
			} else {
				reference = ""
				// @sql:on
				+ "WHERE ref_id = " + referenceId + "\n"
				// @sql:off
				;
				
			}
		}

		switch (order) {
		case "invoice":
			series = "AND ih.series = id.series AND ih.actual > 0 ";
			break;
		case "receiving":
			series = "AND id.qc_id = 0 ";
			break;
		default:
			series = "";
			break;
		}

		return  // @sql:on
				" " + order + "_bundled\n" 
				+ "AS (SELECT bom.part_id AS item_id,\n"
				+ "           sum (id.qty * bom.qty * qp.qty) AS qty\n"
				+ "      FROM " + order + "_header AS ih\n"
				+ "           INNER JOIN " + order + "_detail AS id\n"
				+ "              ON ih." + order + "_id = id." + order + "_id\n"
				+ series + cutoffDates + route + notAnRRfromPO
				+ "           INNER JOIN bom ON id.item_id = bom.item_id\n"
				+ "           INNER JOIN qty_per AS qp\n"
				+ "              ON bom.uom = qp.uom AND bom.part_id = qp.item_id\n"
				+ reference
				+ "  GROUP BY bom.part_id),\n"
				+ " " + order + "_as_is\n"
				+ "AS (SELECT id.item_id,\n"
				+ "           sum (id.qty * qp.qty) AS qty\n"
				+ "      FROM " + order + "_header AS ih\n"
				+ "           INNER JOIN " + order + "_detail AS id\n"
				+ "              ON ih." + order + "_id = id." + order + "_id\n"
				+ series + cutoffDates + route
				+ "           INNER JOIN qty_per AS qp\n"
				+ "              ON id.uom = qp.uom AND id.item_id = qp.item_id\n"
				+ "           INNER JOIN item_master AS im\n"
				+ "              ON id.item_id = im.id AND im.type_id <> 2\n"
				+ reference
				+ "  GROUP BY id.item_id),\n"
				+ SQL.addCombinedQtyStmt(order, order + "_bundled ", order + "_as_is")
				; 
				// @sql:off
	}

	public static String addCombinedQtyStmt(String order, String table1, String table2) {
		return// @sql:on
				" " + order + "_combined\n"
				+ "AS (SELECT * FROM " +  table1 + "\n"
				+ "     UNION\n"
				+ "    SELECT * FROM " +  table2 + "),\n"
				+ "" + order + "d\n"
				+ "AS (SELECT item_id,\n"
				+ "           sum (qty) AS qty\n"
				+ "      FROM " +  order + "_combined\n"
				+ "  GROUP BY item_id)\n"
				;
				// @sql:off
	}

	public static String addBookedQtyStmt() {
		return SQL.addMovedQtyStmt("sales", WITH_CUTOFF_DATES, PER_ROUTE, NO_REFERENCE);
	}

	public static String addReceivedQtyStmt() {
		return SQL.addMovedQtyStmt("receiving", WITH_CUTOFF_DATES, PER_ROUTE, NO_REFERENCE);
	}

	public static String addReceivedQtyStmt(int referenceId) {
		return SQL.addMovedQtyStmt("receiving", NO_CUTOFF_DATES, NO_ROUTES, referenceId);
	}

	public static String addInvoicedQtyStmt() {
		return SQL.addMovedQtyStmt("invoice", WITH_CUTOFF_DATES, PER_ROUTE, NO_REFERENCE);
	}

	public static String addInvoicedQtyStmt(int referenceId) {
		return SQL.addMovedQtyStmt("invoice", NO_CUTOFF_DATES, NO_ROUTES, referenceId);
	}

	public static String addDeliveredQtyStmt() {
		return SQL.addMovedQtyStmt("delivery", WITH_CUTOFF_DATES, PER_ROUTE, NO_REFERENCE);
	}

	public static String addDeliveredQtyStmt(int referenceId) {
		return SQL.addMovedQtyStmt("delivery", NO_CUTOFF_DATES, NO_ROUTES, referenceId);
	}

	public static String addKeptQtyStmt() {
		return SQL.addMovedQtyStmt("count", WITH_CUTOFF_DATES, PER_ROUTE, NO_REFERENCE);
	}

	public static String addKeptQtyStmt(int referenceId) {
		return SQL.addMovedQtyStmt("count", NO_CUTOFF_DATES, NO_ROUTES, referenceId);
	}

	public static String addSoldQtyStmt() {
		// @sql:on
		return addInvoicedQtyStmt() + ", " 
			+ addDeliveredQtyStmt() + ", " 
			+ addCombinedQtyStmt("sol", "invoiced", "deliveryd");
		// @sql:off
	}

	public static String addSoldQtyStmt(int referenceId) {
		// @sql:on
		return addInvoicedQtyStmt(referenceId) + ", " 
			+ addDeliveredQtyStmt(referenceId) + ", " 
			+ addCombinedQtyStmt("sol", "invoiced", "deliveryd");
		// @sql:off
	}
}

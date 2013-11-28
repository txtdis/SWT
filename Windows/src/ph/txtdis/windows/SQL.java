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
	
	public static String addItemParentStmt() {
		 return  "WITH RECURSIVE parent_child (child_id, parent_id) " +
				 "AS ( " +
				 " SELECT it.child_id, " +
				 "     it.parent_id " +
				 "   FROM item_tree AS it " +
				 " UNION ALL " +
				 " SELECT parent_child.child_id, " +
				 "        it.parent_id " +
				 "   FROM item_tree it " +
				 "   JOIN parent_child " +
				 "     ON it.child_id = parent_child.parent_id " +
				 " ) ";
	}

	public static String addLatestPriceStmt(boolean isWithCutoffDate) {
		String cutoffDate = "";
		if (isWithCutoffDate) 
			cutoffDate = ""
					+ "INNER JOIN parameter AS pm\n"
					+ "   ON price.start_date <= pm.start_date\n";
		return// @sql:on
				  "price_cutoff_date\n"
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
		return addLatestRouteStmt(null);
	}

	public static String addLatestRouteStmt(Date cutoff) {
		String cutoffDate;
		if (cutoff == null)
			// @sql:on
			cutoffDate = "INNER JOIN parameter AS pm\n"
					+ "      ON account.start_date <= pm.start_date\n";
			// @sql:off
		else
			cutoffDate = "WHERE account.start_date <= '" + cutoff.toString() + "'\n";
		return// @sql:on
				"  route_cutoff_date\n"
				+ "AS (SELECT account.customer_id AS cutoff_customer_id,\n"
				+ "           max(account.start_date) AS cutoff_date\n"
				+ "      FROM account\n"
				+ cutoffDate
				+ "  GROUP BY account.customer_id),\n"
				+ "latest_route\n"
				+ "AS (SELECT route_id AS id,\n"
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

		if (isWithCutoffDates)
			// @sql:on
			cutoffDates = "INNER JOIN parameter AS pm\n"
					+ "       ON ih." + order + "_date BETWEEN pm.start_date AND pm.end_date\n";
			// @sql:ff
		
		if (isPerRoute) {
			String customer = "AND ih.customer_id = latest_route.customer_id\n";
			if (order.equals("receiving")) {
				customer = "AND ih.partner_id = latest_route.customer_id\n";
			} else if (order.equals("count")) {
				customer = ""
						+ " INNER JOIN customer_master as customer\n"
						+ "	   ON customer.id = latest_route.customer_id\n"
						+ " INNER JOIN location\n"
						+ "    ON     location.id = ih.location_id\n" 
						+ "	      AND customer.name = location.name\n"
						;
			}

			// @sql:on
			route =   "INNER JOIN latest_route\n"
					+ " ON pm.route_id = latest_route.id\n"
					+ customer;			
			// @sql:off
		}
		
		if (referenceId != 0) {
			if(order.equals("count")) {
				String referenceType = referenceId < 0 ? "purchase" : "sales"; 
				reference = ""
						+ "INNER JOIN location AS loc\n"
						+ "	    ON loc.id = ih.location_id\n"
						+ "  INNER JOIN customer_master AS cm\n"
						+ "		ON loc.name = cm.name\n"
						+ "  INNER JOIN " + referenceType + "_header AS so\n"
						+ "		ON cm.id = so.customer_id\n"
						+ "WHERE so." + referenceType + "_id = " + referenceId + "\n";
			} else {
				reference = "WHERE ref_id = " + referenceId + "\n";				
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
				+ series + cutoffDates + route
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
		return  // @sql:on
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

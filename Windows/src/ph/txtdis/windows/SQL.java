package ph.txtdis.windows;

public class SQL {

	public static String addLatestPriceStmt(boolean isWithCutoffDate) {
		String cutoffDate = "";
		if (isWithCutoffDate) {
			cutoffDate = "INNER JOIN parameter AS pm ON price.start_date <= pm.start_date ";
		}
		return// @sql:on
				"  price_cutoff_date "
				+ "AS (SELECT price.item_id AS cutoff_item_id, "
				+ "           max(price.start_date) AS cutoff_date "
				+ "      FROM price "
				+ cutoffDate
				+ "     WHERE tier_id = 1 "
				+ "  GROUP BY item_id "
				+ "), "
				+ "latest_price "
				+ "AS (SELECT item_id, "
				+ "           price "
				+ "      FROM price "
				+ "	          INNER JOIN price_cutoff_date AS cutoff "
				+ "              ON     price.start_date = cutoff_date "
				+ "				    AND price.item_id = cutoff_item_id "
				+ "     WHERE tier_id = 1 "
				+ ") "
				;
				// @sql:off
	}

	public static String addMovedQtyStmt(String order, boolean isWithCutoffDates) {
		String cutoffDates = "";
		String series = "";

		if (isWithCutoffDates)
			cutoffDates = " INNER JOIN parameter AS pm ON ih." + order + "_date BETWEEN pm.start_date AND pm.end_date ";

		switch (order) {
			case "invoice":
				series = " AND ih.series = id.series AND ih.actual > 0 ";
				break;
			case "sales":
				//series = " AND ih.actual > 0 ";
				break;
			case "rr":
				series = " AND id.qc_id = 0 ";
				break;
			default:
				break;
		}

		return// @sql:on
				" " + order + "_bundled " 
				+ "AS (SELECT bom.part_id AS item_id, "
				+ "           sum (id.qty * bom.qty * qp.qty) AS qty "
				+ "      FROM " + order + "_header AS ih "
				+ "           INNER JOIN " + order + "_detail AS id "
				+ "              ON ih." + order + "_id = id." + order + "_id "
				+ series + cutoffDates
				+ "           INNER JOIN bom ON id.item_id = bom.item_id "
				+ "           INNER JOIN qty_per AS qp "
				+ "              ON bom.uom = qp.uom AND bom.part_id = qp.item_id "
				+ "  GROUP BY bom.part_id), "
				+ "" + order + "_as_is "
				+ "AS (SELECT id.item_id, "
				+ "           sum (id.qty * qp.qty) AS qty "
				+ "      FROM " + order + "_header AS ih "
				+ "           INNER JOIN " + order + "_detail AS id "
				+ "              ON ih." + order + "_id = id." + order + "_id "
				+ series + cutoffDates
				+ "           INNER JOIN qty_per AS qp "
				+ "              ON id.uom = qp.uom AND id.item_id = qp.item_id "
				+ "           INNER JOIN item_master AS im "
				+ "              ON id.item_id = im.id AND im.type_id <> 2 "
				+ "  GROUP BY id.item_id), "
				+ SQL.addCombinedQtyStmt(order, order + "_bundled ", order + "_as_is")
				; 
				// @sql:off
	}

	public static String addCombinedQtyStmt(String order, String table1, String table2) {
		return// @sql:on
				"" + order + "_combined "
				+ "AS (SELECT * FROM " +  table1
				+ "     UNION "
				+ "    SELECT * FROM " +  table2 + "), "
				+ "" + order + "d "
				+ "AS (SELECT item_id, "
				+ "           sum (qty) AS qty "
				+ "      FROM " +  order + "_combined "
				+ "  GROUP BY item_id) "
				;
				// @sql:off
	}

	public static String addBookedQtyStmt(boolean isWithCutoffDates) {
		return SQL.addMovedQtyStmt("sales", isWithCutoffDates);
	}

	public static String addReceivedQtyStmt(boolean isWithCutoffDates) {
		return SQL.addMovedQtyStmt("receiving", isWithCutoffDates);
	}

	public static String addInvoicedQtyStmt(boolean isWithCutoffDates) {
		return SQL.addMovedQtyStmt("invoice", isWithCutoffDates);
	}

	public static String addDeliveredQtyStmt(boolean isWithCutoffDates) {
		return SQL.addMovedQtyStmt("delivery", isWithCutoffDates);
	}

	public static String addSoldQtyStmt(boolean isWithCutoffDates) {
		// @sql:on
		return addInvoicedQtyStmt(isWithCutoffDates) + ", " 
			+ addDeliveredQtyStmt(isWithCutoffDates) + ", " 
			+ addCombinedQtyStmt("sol", "invoiced", "deliveryd");
		// @sql:off
	}
}

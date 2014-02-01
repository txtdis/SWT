package ph.txtdis.windows;

import java.sql.Date;

public class Count {
	private static Query sql = new Query();

	public Count() {}
	
	public static boolean isOnFile(int id) {
		return (boolean) sql.getDatum(id, "SELECT EXISTS(SELECT 1 FROM count_header WHERE count_id = ?);");
	}

	public static boolean isDone(Date date) {
		return (boolean) sql.getDatum(date, "SELECT EXISTS(SELECT 1 FROM count_header WHERE count_date = ?);");
	}

	public static boolean isClosed(Date date) {
		return (boolean) sql.getDatum(date, "SELECT EXISTS(SELECT 1 FROM count_completion WHERE count_date = ?);");
	}

	public static boolean isReconciled(Date date) {
		return (boolean) sql.getDatum(date, "SELECT EXISTS(SELECT 1 FROM count_closure WHERE count_date = ?);");
    }

	public static Date getLatestDate() {
		return (Date) sql.getDatum("SELECT max(count_date) FROM count_header;");
	}

	public static Date getLatestReconciledDate() {
		return getLastReconciledDate(getLatestDate());
	}

	public static Date getLastReconciledDate(Date cutoff) {
		return (Date) sql.getDatum(cutoff, "SELECT max(count_date) FROM count_closure WHERE count_date <= ?;\n");
	}

	public static int getLatestCountedBizUnit() {
		return getLatestCountedBizUnit(getLatestDate());
	}

	public static int getLatestCountedBizUnit(Date cutoff) {
		return (int) sql.getDatum(cutoff, ""
				// @sql:on
				+ Item.addParentChildCTE() + "\n"
				+ "  SELECT DISTINCT if.id AS biz_unit\n" 
				+ "	FROM count_header AS ch\n" 
				+ "		 INNER JOIN count_detail AS cd ON ch.count_id = cd.count_id\n" 
				+ "		 INNER JOIN item_header AS ih ON ih.id = cd.item_id\n" 
				+ "		 INNER JOIN parent_child AS pc ON ih.id = pc.child_id\n" 
				+ "		 INNER JOIN item_family AS if ON pc.parent_id = if.id\n" 
				+ "   WHERE tier_id = 1 AND -if.id IN (1, 2) AND ch.count_date = ?\n" 
				// @sql:off
				);
	}

	public static Date getLatestReconciledDate(int bizUnit) {
		return (Date) sql.getDatum(bizUnit, ""
				// @sql:on
				+ Item.addParentChildCTE() + "\n"
				+ "  SELECT max (cc.count_date) AS count_date\n" 
				+ " FROM count_header AS ch\n" 
				+ "      INNER JOIN count_detail AS cd ON ch.count_id = cd.count_id\n" 
				+ "      INNER JOIN item_header AS ih ON ih.id = cd.item_id\n" 
				+ "      INNER JOIN parent_child AS pc ON ih.id = pc.child_id\n" 
				+ "      INNER JOIN item_family AS if ON pc.parent_id = if.id\n" 
				+ "      INNER JOIN count_closure AS cc ON ch.count_date = cc.count_date\n" 
				+ "   WHERE tier_id = 1 AND if.id = ?\n" 
				+ "GROUP BY if.id\n" 
				// @sql:off
				);
	}

	public static String addCTE(Date date) {
		// @sql:on
		return    "	 counted AS\n" 
				+ "		 (SELECT DISTINCT ON(ch.count_id, cd.item_id)\n" 
				+ "			     ch.count_id, "
		        + "				 last_value( a.route_id)\n"
		        + "				     OVER (PARTITION BY ch.count_id, cd.item_id ORDER BY a.start_date DESC) AS route_id,\n"
		        + "				 cd.item_id, (cd.qty * qp.qty) AS qty\n" 
		        + "			FROM count_header AS ch\n"
		        + "				 INNER JOIN count_detail AS cd\n" 
		        + "                 ON     ch.count_id = cd.count_id\n"
		        + "				       AND ch.count_date = '" + date + "'\n"
		        + "				 INNER JOIN qty_per AS qp ON cd.uom = qp.uom AND cd.item_id = qp.item_id\n"
		        + "				 INNER JOIN location ON location.id = ch.location_id\n"
		        + "				 INNER JOIN customer_header AS cm ON location.name = cm.name\n"
		        + "				 INNER JOIN account AS a ON cm.id = a.customer_id AND a.start_date <= ch.count_date),\n"
		        + "	 kept AS\n" 
		        + "		 (	SELECT route_id, item_id, sum (qty) AS qty\n" 
		        + "			  FROM counted\n"
		        + "		  GROUP BY route_id, item_id";
		// @sql:off
	}
}

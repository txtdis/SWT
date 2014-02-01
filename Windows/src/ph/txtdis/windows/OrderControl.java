package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;

public class OrderControl {
	private static Query sql = new Query();

	public static String[] getSeries() {
		Object[] series = sql.getList("SELECT DISTINCT series FROM invoice_booklet ORDER BY series");
		return (series != null ? Arrays.copyOf(series, series.length, String[].class) : null);
	}

	public static boolean isOnFile(String series) {
		return (boolean) sql.getDatum(series, "SELECT EXISTS (SELECT 1 FROM invoice_booklet WHERE series = ?)");
	}

	public static boolean isOnFile(Type type, Date date, int id) {
		return (boolean) sql.getDatum(new Object[] { date, id }, "" 
				+ "SELECT EXISTS (SELECT 1 FROM " + type + " WHERE start_date = ? AND customer_id = ?);");
	}

	public static boolean isOnFile(int id, String series) {
		if (series.equals("R"))
			return (boolean) sql.getDatum(id, "SELECT EXISTS (SELECT 1 FROM remit_header WHERE remit_id = ?)");
		else
			return (boolean) sql.getDatum(new Object[] { id, series }, ""
					+ "SELECT EXISTS (SELECT 1 FROM invoice_header WHERE invoice_id = ? AND series = ?)");
	}

	public static boolean isOnFile(Type type, int id) {
		String idText = "id";
		if (type != Type.CUSTOMER && type != Type.ITEM)
			idText = type + "_id";
		System.out.println("SELECT EXISTS (SELECT 1 FROM " + type + "_header WHERE " + idText + " = ?)");
		return (boolean) sql.getDatum(id, "SELECT EXISTS (SELECT 1 FROM " + type + "_header WHERE " + idText + " = ?)");
	}

	public static boolean isOnFile(Type type, int id, String series) {
		return series == null ? isOnFile(type, id) : isOnFile(id, series);
	}

	public static int getFirstLineItemId(int id, String series) {
		Object itemId = sql.getDatum(new Object[] { id, series },""
				// @sql:on
				+ "SELECT id.item_id "
				+ "  FROM invoice_header AS ih "
				+ "       INNER JOIN invoice_detail AS id "
				+ "          ON ih.invoice_id = id.invoice_id "
				+ " WHERE     ih.invoice_id = ? "
				+ "       AND ih.series = ? "
				+ "       AND line_id = 1; "
				// @sql:off
				);
		return itemId != null ? (int) itemId : 0;
	}

	public static int getFirstLineItemId(Type type, int id) {
		Object itemId = sql.getDatum(Math.abs(id),""
				// @sql:on
				+ "SELECT id.item_id "
				+ "  FROM " + type + "_header AS ih "
				+ "       INNER JOIN " + type + "_detail AS id "
				+ "          ON ih." + type + "_id = id." + type + "_id "
				+ " WHERE     ih." + type + "_id = ? "
				+ "       AND line_id = 1;"
				// @sql:off
				);
		return itemId != null ? (int) itemId : 0;
	}

	public static int getLastId(int id, String series) {
		// @sql:on
		Object lastId = sql.getDatum(new Object[] { id, id, series }, ""
				+ "WITH booklet "
				+ "     AS (SELECT start_id, end_id, series "
				+ "           FROM invoice_booklet "
				+ "          	   WHERE start_id <= ? "
				+ "			       AND end_id >= ? "
				+ "			       AND series LIKE ?), "
				+ "     max_id "
				+ "     AS (SELECT max (invoice_id) AS id "
				+ "           FROM invoice_header AS ih, booklet AS b "
				+ "          	   WHERE invoice_id "
				+ "						BETWEEN start_id AND end_id "
				+ "                AND ih.series = b.series) "
				+ "SELECT CASE WHEN id IS NULL THEN start_id - 1 ELSE id END "
				+ "  FROM max_id, booklet ");
		// @sql:off
		return (lastId == null ? 0 : (int) lastId);
	}

	public static boolean isIdStartOfBooklet(int id, String series) {
		// @sql:on
		return (boolean) sql.getDatum(new Object[] { id, series }, ""
				+ "SELECT EXISTS ("
				+ "SELECT start_id "
				+ "  FROM invoice_booklet "
				+ " WHERE     start_id = ? "
				+ "       AND series = ?)");
		// @sql:off
	}

	public static boolean wasPrinted(int id) {
		return (boolean) sql.getDatum(id, "SELECT EXISTS (SELECT 1 FROM print WHERE so_id = ?);");
	}

	public static int getOrderId(int referenceId) {
		Object id = null;
		Type[] types = { Type.INVOICE, Type.DELIVERY };
		for (Type type : types) {
			id = sql.getDatum(referenceId, "SELECT " + type + "_id FROM " + type + "_header WHERE ref_id = ? ");
			if (id != null)
				break;
		}
		return id == null ? 0 : (int) id;
	}

	public static int getReceivingId(int referenceId) {
		Object receivingId = sql.getDatum(referenceId, "SELECT receiving_id FROM receiving_header WHERE ref_id = ?;");
		return receivingId == null ? 0 : (int) receivingId;
	}

	public static int getSalesId(Date postDate, int outletId) {
		// @sql:on
		Object salesId = sql.getDatum(new Object[] { postDate, outletId }, ""
				+ "SELECT sales_id "
				+ "  FROM sales_header AS sh "
				+ "  INNER JOIN customer_header AS cm "
				+ "ON sh.customer_id = cm.id "
				+ " WHERE     sales_date = ? " 
				+ "       AND sh.customer_id = ? ");
		// @sql:off
		return salesId == null ? 0 : (int) salesId;
	}

	public static int getPartnerId(int referenceId) {
		String type = referenceId < 0 ? "purchase" : "sales";
		Object partnerId = new Query().getDatum(Math.abs(referenceId), ""
				+ "SELECT customer_id FROM " + type + "_header WHERE " + type + "_id = ?;");
		return partnerId == null ? 0 : (int) partnerId;
	}

	public static Date getDate(int id) {
		// @sql:on
		return (Date) sql.getDatum(id,""
				+ "SELECT CASE WHEN invoice_date IS NULL "
				+ "			THEN 'epoch' ELSE invoice_date END AS invoice_date "
				+ "  FROM invoice_header "
				+ "       WHERE invoice_id = ? ");
		// @sql:off
	}

	public static boolean isFromExTruck(int salesId) {
		// @sql:on
		return (boolean) sql.getDatum(salesId, ""
				+ "SELECT EXISTS (SELECT 1 "
				+ "       FROM channel AS c "
				+ "       INNER JOIN customer_header AS cm ON c.id = cm.type_id "
				+ "       INNER JOIN sales_header AS sh ON cm.id = sh.customer_id "
				+  "WHERE     sales_id = ? "
				+ "       AND c.name = 'ROUTE');");
		// @sql:off
	}

	public static Date getReferenceDate(int id) {
		String type = id < 0 ? "purchase" : "sales";
		return (Date) sql.getDatum(Math.abs(id), "SELECT " + type + "_date FROM " + type + "_header WHERE  " + type
				+ "_id = ? ");
	}

	public static Date getReferenceDueDate(int id) {
		Date referenceDate = getReferenceDate(id);
		int creditTerm = Credit.getTerm(getPartnerId(id), referenceDate);
		return DIS.addDays(referenceDate, creditTerm);
	}

	public static Date getTransferDate(int referenceId) {
		return (Date) sql.getDatum(referenceId, "SELECT delivery_date FROM delivery_header WHERE  delivery_id = ?;");
	}

	public static int getOpenBadOrderId(int outletId) {
		// @sql:on
		Object badId = sql.getDatum(new Object[] { outletId, outletId },""
				+ "WITH invoices "
				+ "		AS (SELECT ref_id AS id "
				+ "			  FROM invoice_header "
				+ " 		 WHERE 	   customer_id = ? "
				+ "				   AND actual < 0 ), "
				+ "		sales_orders "
				+ "		AS (SELECT DISTINCT sd.sales_id AS id "
				+ "			  FROM sales_header AS sh	"
				+ "				   INNER JOIN sales_detail AS sd "
				+ "					  ON sd.sales_id = sh.sales_id "
				+ "			 WHERE 	   sh.customer_id = ? "
				+ "				   AND sd.item_id < 0 ) "
				+ "  SELECT s.id "
				+ "    FROM sales_orders AS s "
				+ "         LEFT JOIN invoices AS i "
				+ "           ON s.id = i.id "
				+ "  WHERE i.id IS null");
		// @sql:off
		return badId == null ? 0 : (int) badId;
	}

	public static boolean isBadOrder(int salesId) {
		return (boolean) sql.getDatum(salesId,
				"SELECT EXISTS (SELECT 1 FROM sales_detail WHERE item_id < 0 AND sales_id = ?)");
	}

	public static BigDecimal getBadOrderRefundLimit(int outletId, Date date) {
		Date start = DIS.addYears(date, -1);
		// @sql:on
		Object refund = sql.getDatum(new Object[] { outletId, start, date },""
				+ "WITH invoices "
				+ "     AS (SELECT invoice_id AS id "
				+ "			  FROM invoice_header "
				+ "		 	 WHERE	   customer_id = ? "
				+ "				   AND invoice_date BETWEEN ? AND ? ), "
				+ "		sold "
				+ "		AS (SELECT sum(CASE WHEN actual IS NULL "
				+ "						  THEN 0 ELSE actual END) AS sale "
				+ "			  FROM invoice_header AS ih "
				+ "			  	   INNER JOIN invoices AS i ON	ih.invoice_id = i.id "
				+ "	WHERE	actual >= 0 "
				+ "), returned AS ( "
				+ "	SELECT 	sum(CASE WHEN actual IS NULL "
				+ "				THEN 0 ELSE actual END) AS rebate    "
				+ "	FROM 	invoice_header AS ih"
				+ "	INNER JOIN invoices AS i ON	ih.invoice_id = i.id "
				+ "	WHERE	actual < 0 "
				+ ") "
				+ "SELECT CASE WHEN sale IS NULL THEN 0 ELSE sale END "
				+ "		* 0.01 "
				+ "		- CASE WHEN rebate IS NULL "
				+ "			THEN 0 ELSE rebate END "
				+ "FROM sold, returned");
		// @sql:off
		return refund == null ? BigDecimal.ZERO : (BigDecimal) refund;
	}

	public static int getOrderIdWithSameDiscount(Type type, int itemId, int customerId, Date date) {
		// @sql:on
		Object id = sql.getDatum(new Object[] {itemId, customerId, date }, ""
				+ Item.addParentChildCTE() + ", "
				+ "  parameter " 
				+ "     AS (SELECT cast (? AS int) AS item_id, "
				+ "                cast (? AS int) AS customer_id, "
				+ "                cast (? AS date) AS post_date), "
				+ "     latest_discount_date "
				+ "     AS (  SELECT child_id AS item_id, "
				+ "                  d.customer_id, "
				+ "                  max (start_date) AS max_date "
				+ "             FROM parent_child AS ip "
				+ "                  INNER JOIN discount AS d ON ip.parent_id = d.family_id "
				+ "                  INNER JOIN parameter AS p "
				+ "                     ON     d.customer_id = p.customer_id "
				+ "                        AND start_date <= p.post_date "
				+ "         GROUP BY child_id, "
				+ "                  d.customer_id), "
				+ "     latest_discount "
				+ "     AS (SELECT item_id, "
				+ "                CASE WHEN im.not_discounted IS TRUE THEN 0 ELSE level_1 END "
				+ "                   AS level_1, "
				+ "                CASE WHEN im.not_discounted IS TRUE THEN 0 ELSE level_2 END "
				+ "                   AS level_2 "
				+ "           FROM parent_child AS ip "
				+ "                INNER JOIN item_header AS im ON im.id = ip.child_id "
				+ "                INNER JOIN discount AS d ON ip.parent_id = d.family_id "
				+ "                INNER JOIN latest_discount_date AS ldd "
				+ "                   ON     start_date = ldd.max_date "
				+ "                      AND ldd.item_id = ip.child_id "
				+ "                      AND ldd.customer_id = d.customer_id), "
				+ "     " + type + "_order "
				+ "     AS (SELECT sd." + type + "_id, "
				+ "                CASE WHEN level_1 IS NULL THEN 0 ELSE level_1 END AS level_1, "
				+ "                CASE WHEN level_2 IS NULL THEN 0 ELSE level_2 END AS level_2 "
				+ "           FROM " + type + "_header AS sh "
				+ "                INNER JOIN " + type + "_detail AS sd ON sd." + type + "_id = sh." + type + "_id "
				+ "                INNER JOIN parameter AS p "
				+ "                   ON     p.post_date = sh." + type + "_date "
				+ "                      AND p.customer_id = sh.customer_id "
				+ "                LEFT JOIN latest_discount AS ld ON sd.item_id = ld.item_id "
				+ "          WHERE line_id = 1), "
				+ "     item_id "
				+ "     AS (SELECT CASE WHEN level_1 IS NULL THEN 0 ELSE level_1 END AS level_1, "
				+ "                CASE WHEN level_2 IS NULL THEN 0 ELSE level_2 END AS level_2 "
				+ "           FROM parameter AS p "
				+ "                LEFT JOIN latest_discount AS ld ON p.item_id = ld.item_id) "
				+ "SELECT " + type + "_id "
				+ "  FROM " + type + "_order AS so "
				+ "       INNER JOIN item_id AS ii "
				+ "          ON so.level_1 = ii.level_1 AND so.level_2 = ii.level_2; "
				);
		// @sql:off
		// @sql:off
		return id == null ? 0 : (int) id;
	}

	public static boolean hasOpenPO(Date date, int vendorId) {
		// @sql:on
		return (boolean) sql.getDatum(new Object[] { date, vendorId }, ""
				+ "SELECT EXISTS ("
				+ "SELECT 1 FROM purchase_header AS ph "
				+ "              INNER JOIN vendor_specific AS vs "
				+ "                 ON ph.customer_id = vs.vendor_id "
				+ "    WHERE     purchase_date <= ? "
				+ "   	     AND vs.vendor_id = ?)");
		// @sql:off
	}

	public static Object[][] getReceivedReturnedMaterials(int soId) {
		// @sql:on
		return sql.getTableData(soId,""
				+ "SELECT rd.item_id,"
				+ "		  sum(rd.qty * qp.qty) "
				+ "  FROM receiving_detail AS rd "
				+ "       INNER JOIN receiving_header AS rh "
				+ "	         ON rd.receiving_id = rh.receiving_id "
				+ "       INNER JOIN qty_per AS qp "
				+ "	         ON     rd.item_id = qp.item_id "
				+ "	            AND rd.uom = qp.uom "
				+ (soId < 0 ? 
						"WHERE -ref_id = ? " : 
							"INNER JOIN sales_header AS sh "
							+ "	         ON     rh.ref_id = sh.sales_id "
							+ "	            AND sh.sales_id =  ? "
							+ "       INNER JOIN sales_detail AS sd "
							+ "	         ON     rd.item_id = -sd.item_id "
							+ "	            AND qp.item_id = -sd.item_id "
							+ "	            AND rh.ref_id = sd.sales_id "
							+ "	            AND sh.sales_id = sd.sales_id ")
							+ "	            AND sd.uom = qp.uom "
							+ "GROUP BY rd.item_id "
							+ "ORDER BY rd.item_id ");
		// @sql:off
	}

	public static Object[][] getReceivedMaterials(int soId) {
		// @sql:on
		return sql.getTableData(soId, ""
				+ "SELECT rd.item_id, "
				+ "       sum(rd.qty * qp.qty) "
				+ "  FROM receiving_detail AS rd "
				+ "       INNER JOIN receiving_header AS rh "
				+ "          ON rd.receiving_id = rh.receiving_id "
				+ "       INNER JOIN qty_per AS qp "
				+ "          ON 	rd.item_id = qp.item_id "
				+ "				AND rd.uom = qp.uom "
				+ " WHERE ref_id = ? "
				+ " GROUP BY rd.item_id "
				+ " ORDER BY rd.item_id ");
		// @sql:off
	}

	public static Object[][] getNetItemQtyToLoad(int soID) {
		// @sql:on
		return sql.getTableData(soID, "" 
				+ "WITH sales AS (SELECT ? AS id),\n"
				+ "     booked\n" 
				+ "     AS (  SELECT sd.line_id,\n" 
				+ "                  sd.item_id AS id,\n"
				+ "                  uom.unit,\n" 
				+ "                  qp.qty AS qty_per,\n"
				+ "                  sum (sd.qty * qp.qty) AS qty\n" 
				+ "             FROM sales_header AS sh\n"
				+ "                  INNER JOIN sales_detail AS sd ON sd.sales_id = sh.sales_id\n"
				+ "                  INNER JOIN qty_per AS qp\n"
				+ "                     ON sd.uom = qp.uom AND sd.item_id = qp.item_id\n"
				+ "                  INNER JOIN uom ON uom.id = sd.uom\n"
				+ "                  INNER JOIN sales AS s ON sd.sales_id = s.id\n"
				+ "         GROUP BY sd.item_id,\n" 
				+ "                  uom.unit,\n"
				+ "                  qp.qty,\n" 
				+ "                  line_id),\n" 
				+ "     counted\n"
				+ "     AS (  SELECT cd.item_id AS id, sum (cd.qty * qp.qty) AS qty\n"
				+ "             FROM count_detail AS cd\n"
				+ "                  INNER JOIN count_header AS ch ON cd.count_id = ch.count_id\n"
				+ "                  INNER JOIN location AS loc ON ch.location_id = loc.id\n"
				+ "                  INNER JOIN qty_per AS qp\n"
				+ "                     ON cd.uom = qp.uom AND cd.item_id = qp.item_id\n"
				+ "                  INNER JOIN sales_header AS sh\n"
				+ "                     ON ch.count_date = (sh.sales_date - 1)\n"
				+ "                  INNER JOIN customer_header AS cm\n"
				+ "                     ON cm.id = sh.customer_id AND loc.name = cm.name\n"
				+ "                  INNER JOIN sales AS s ON s.id = sh.sales_id\n"
				+ "         GROUP BY cd.item_id),\n" 
				+ "     to_load\n" 
				+ "     AS (SELECT b.line_id,\n"
				+ "                b.qty - CASE WHEN c.qty IS NULL THEN 0 ELSE c.qty END AS qty,\n"
				+ "                b.unit,\n" 
				+ "                b.qty_per\n"
				+ "           FROM booked AS b LEFT JOIN counted AS c ON b.id = c.id)\n"
				+ "  SELECT CASE WHEN qty % qty_per <> 0 THEN qty ELSE qty / qty_per END AS qty,\n"
				+ "         CASE WHEN qty % qty_per <> 0 THEN uom.unit ELSE to_load.unit END\n"
				+ "            AS unit\n" 
				+ "    FROM to_load INNER JOIN uom ON uom.id = 0\n"
				+ "ORDER BY line_id\n" 
				);
		// @sql:off
	}

	public static boolean hasUnpaidIncentives(int partnerId, Date postDate) {
		return false;
	}

	public static int getMaximumId(Type type) {
		return getTipId(Type.MAX, type);
	}

	public static int getMinimumId(Type type) {
		return getTipId(Type.MIN, type);
	}

	private static int getTipId(Type tip, Type type) {
		String id = type == Type.REMIT ? "remit_" : "";
		Object tipId = sql.getDatum("SELECT " + tip + "(" + id + "id)" + " FROM " + type + "_header");
		return tipId == null ? 1 : (int) tipId;

	}

	public static int getIdWithSameDiscount(Type type, int itemId, int partnerId, Date date) {
		Object id = sql.getDatum(new Object[] { type, itemId, partnerId, date },""
				// @sql:on 
				+ Item.addParentChildCTE() + ", "
				+ "parameter " 
				+ "     AS (SELECT cast (? AS int) AS item_id, "
				+ "                cast (? AS int) AS customer_id, "
				+ "                cast (? AS date) AS post_date), "
				+ "     latest_discount_date "
				+ "     AS (  SELECT child_id AS item_id, "
				+ "                  d.customer_id, "
				+ "                  max (start_date) AS max_date "
				+ "             FROM parent_child AS ip "
				+ "                  INNER JOIN discount AS d ON ip.parent_id = d.family_id "
				+ "                  INNER JOIN parameter AS p "
				+ "                     ON     d.customer_id = p.customer_id "
				+ "                        AND start_date <= p.post_date "
				+ "         GROUP BY child_id, "
				+ "                  d.customer_id), "
				+ "     latest_discount "
				+ "     AS (SELECT item_id, "
				+ "                CASE WHEN im.not_discounted IS TRUE THEN 0 ELSE level_1 END "
				+ "                   AS level_1, "
				+ "                CASE WHEN im.not_discounted IS TRUE THEN 0 ELSE level_2 END "
				+ "                   AS level_2 "
				+ "           FROM parent_child AS ip "
				+ "                INNER JOIN item_header AS im ON im.id = ip.child_id "
				+ "                INNER JOIN discount AS d ON ip.parent_id = d.family_id "
				+ "                INNER JOIN latest_discount_date AS ldd "
				+ "                   ON     start_date = ldd.max_date "
				+ "                      AND ldd.item_id = ip.child_id "
				+ "                      AND ldd.customer_id = d.customer_id), "
				+ "     " + type + "_order "
				+ "     AS (SELECT sd." + type + "_id, "
				+ "                CASE WHEN level_1 IS NULL THEN 0 ELSE level_1 END AS level_1, "
				+ "                CASE WHEN level_2 IS NULL THEN 0 ELSE level_2 END AS level_2 "
				+ "           FROM " + type + "_header AS sh "
				+ "                INNER JOIN " + type + "_detail AS sd ON sd." + type + "_id = sh." + type + "_id "
				+ "                INNER JOIN parameter AS p "
				+ "                   ON     p.post_date = sh." + type + "_date "
				+ "                      AND p.customer_id = sh.customer_id "
				+ "                LEFT JOIN latest_discount AS ld ON sd.item_id = ld.item_id "
				+ "          WHERE line_id = 1), "
				+ "     item_id "
				+ "     AS (SELECT CASE WHEN level_1 IS NULL THEN 0 ELSE level_1 END AS level_1, "
				+ "                CASE WHEN level_2 IS NULL THEN 0 ELSE level_2 END AS level_2 "
				+ "           FROM parameter AS p "
				+ "                LEFT JOIN latest_discount AS ld ON p.item_id = ld.item_id) "
				+ "SELECT " + type + "_id "
				+ "  FROM " + type + "_order AS so "
				+ "       INNER JOIN item_id AS ii "
				+ "          ON so.level_1 = ii.level_1 AND so.level_2 = ii.level_2; "
				// @sql:off
				);
		return id == null ? 0 : (int) id;
	}
}

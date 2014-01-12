package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;
import java.util.Calendar;

public class OrderHelper {
	private String type;
	private Data sql;
	private Object object;
	private Object[] objects;
	private int id;

	public OrderHelper() {
		sql = new Data();
	}

	public OrderHelper(int id) {
		this();
		if (id < 0) {
			type = "delivery";
			this.id = -id;
		} else {
			type = "invoice";
			this.id = id;
		}
	}

	public String[] getSeries() {
		// @sql:on
		objects = sql.getData(""
				+ "SELECT DISTINCT series "
				+ "  FROM invoice_booklet "
				+ " ORDER BY series");
		// @sql:off
		return (objects != null ? Arrays.copyOf(objects, objects.length, String[].class) : null);
	}

	public boolean hasSeries(String series) {
		// @sql:on
		object = sql.getDatum(series, ""
				+ "SELECT series "
				+ "  FROM invoice_booklet "
				+ " WHERE series = ? "
				+ " LIMIT 1 ");
		// @sql:off
		return (object == null ? false : true);
	}

	public boolean hasDetail() {
		// @sql:on
		object = sql.getDatum(id,""
				+ "SELECT " + type + "_id " 
				+ "  FROM " + type + "_detail "
				+ " WHERE " + type + "_id = ? "
				+ "LIMIT 1 ");
		// @sql:off
		return (object == null ? false : true);
	}

	public int getFirstLineItemId(String series) {
		// @sql:on
		if (type.equals("invoice")) {
			object = sql.getDatum(new Object[] {id, series },""
					+ "SELECT id.item_id "
					+ "  FROM invoice_header AS ih "
					+ "       INNER JOIN invoice_detail AS id "
					+ "          ON ih.invoice_id = id.invoice_id "
					+ " WHERE     ih.invoice_id = ? "
					+ "       AND ih.series = ? "
					+ "       AND line_id = 1; "
					);
		} else {
			object = sql.getDatum(Math.abs(id),""
					+ "SELECT id.item_id "
					+ "  FROM " + type + "_header AS ih "
					+ "       INNER JOIN " + type + "_detail AS id "
					+ "          ON ih." + type + "_id = id." + type + "_id "
					+ " WHERE     ih." + type + "_id = ? "
					+ "       AND line_id = 1;"
					);
		}
		// @sql:off
		return object != null ? (int) object : 0;
	}

	public boolean isOnFile(String series) {
		// @sql:on
		if(series.equals("R")) {
			object = sql.getDatum(id, ""
					+ "SELECT remit_id "
					+ "FROM   remittance_header "
					+ "WHERE  remit_id = ?;");			
		} else if (type.equals("invoice")) {
			object = sql.getDatum(new Object[] {
			        id, series },""
					+ "SELECT invoice_id "
					+ "FROM invoice_header "
					+ "     WHERE invoice_id = ? "
					+ "     AND series = ?;");
		} else {
			object = sql.getDatum(id, ""
					+ "SELECT " + type + "_id "
					+ "FROM   " + type + "_header "
					+ "WHERE  " + type + "_id = ?;");
		}
		// @sql:off
		return (object == null ? false : true);
	}

	public int getLastId(String series) {
		// @sql:on
		object = sql.getDatum(new Object[] { id, id, series }, ""
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
				+ "SELECT CASE WHEN id IS NULL "
				+ "			THEN start_id - 1 ELSE id END "
				+ "  FROM max_id, booklet ");
		// @sql:off
		return (object == null ? 0 : (int) object);
	}

	public boolean isIdStartOfBooklet(String series) {
		// @sql:on
		object = sql.getDatum(new Object[] { id, series }, ""
				+ "SELECT start_id "
				+ "  FROM invoice_booklet "
				+ " WHERE     start_id = ? "
				+ "       AND series = ?");
		// @sql:off
		return (object == null ? false : true);
	}

	public BigDecimal getInvoiceBalance() {
		// @sql:on
		return (BigDecimal) sql.getDatum(id,""
				+ "SELECT CASE WHEN ih.actual IS null "
				+ "			THEN 0 ELSE ih.actual END "
				+ "		  - CASE WHEN p.payment IS null "
				+ "			THEN 0 ELSE p.payment END "
				+ "FROM " + type + "_header AS ih "
				+ "LEFT JOIN payment AS p "
				+ "ON " + type + "_id = p.order_id "
				+ "WHERE ih." + type + "_id = ? ");
		// @sql:off
	}

	public boolean wasPrinted() {
		// @sql:on
		object = sql.getDatum(id, ""
				+ "SELECT copy "
				+ "  FROM print "
				+ "  WHERE so_id = ? ");
		// @sql:off
		return (object == null ? false : true);
	}

	public int getOrderId(int soId) {
		// @sql:on
		Object invoiceId = sql.getDatum(soId, ""
				+ "SELECT invoice_id "
				+ "FROM invoice_header "
				+ "WHERE ref_id = ? ");
		Object deliveryId = sql.getDatum(soId, ""
				+ "SELECT delivery_id "
				+ "FROM delivery_header "
				+ "WHERE ref_id = ? ");
		// @sql:off
		int orderId = 0;
		if (invoiceId != null) {
			orderId = (int) invoiceId;
		} else if (deliveryId != null) {
			orderId = -(int) deliveryId;
		}
		return orderId;
	}

	public int getRRid(int refId) {
		// @sql:on
		object = sql.getDatum(refId, ""
				+ "SELECT receiving_id "
				+ "  FROM receiving_header "
				+ "WHERE ref_id = ? ");
		// @sql:off
		return object == null ? 0 : (int) object;
	}

	public int getSoId(Date postDate, int outletId) {
		// @sql:on
		object = sql.getDatum(new Object[] { postDate, outletId }, ""
				+ "SELECT sales_id "
				+ "  FROM sales_header AS sh "
				+ "  INNER JOIN customer_master AS cm "
				+ "ON sh.customer_id = cm.id "
				+ " WHERE     sales_date = ? " 
				+ "       AND sh.customer_id = ? ");
		// @sql:off
		return object == null ? 0 : (int) object;
	}

	public int getPartnerId(int refID) {
		String type = refID < 0 ? "purchase" : "sales";
		// @sql:on
		object = new Data().getDatum(Math.abs(refID), ""
				+ "SELECT customer_id "
				+ "  FROM " + type + "_header "
				+ " WHERE " + type + "_id = ? ");
		// @sql:off
		return object == null ? 0 : (int) object;
	}

	public Date getDate() {
		Date date = (Date) sql.getDatum(id,""
				// @sql:on
				+ "SELECT CASE WHEN invoice_date IS NULL "
				+ "			THEN 'epoch' ELSE invoice_date END AS invoice_date "
				+ "  FROM invoice_header "
				+ "       WHERE invoice_id = ? ");
				// @sql:off
		return date;
	}

	public boolean isFromExTruck(int soId) {
		// @sql:on
		object = sql.getDatum(soId,""
				+ "SELECT c.id "
				+ "  FROM channel AS c "
				+ "INNER JOIN customer_master AS cm "
				+ "ON c.id = cm.type_id "
				+ "INNER JOIN sales_header AS sh "
				+ "ON cm.id = sh.customer_id "
				+ "WHERE sales_id = ? "
				+ "AND c.name = 'ROUTE' ");
		// @sql:off
		return object == null ? false : true;
	}

	public Date getReferenceDate(int id) {
		if (id < 0) {
			type = "purchase";
			id = -id;
		} else {
			type = "sales";
		}			
		// @sql:on
		object = sql.getDatum(id,""
				+ "SELECT " + type + "_date "
				+ "  FROM  " + type + "_header "
				+ "WHERE  " + type + "_id = ? "
				);
		// @sql:off
		return (Date) object;
	}
	
	public Date getReferenceDueDate(int id) {
		Date referenceDate = getReferenceDate(id);
		int creditTerm = new Credit().getTerm(getPartnerId(id), referenceDate);
		return DIS.addDays(referenceDate, creditTerm);
	}

	public Date getTransferDate(int referenceId) {
		// @sql:on
		object = sql.getDatum(referenceId, ""
				+ "SELECT delivery_date "
				+ "  FROM  delivery_header "
				+ "WHERE  delivery_id = ? "
				);
		// @sql:off
		return (Date) object;
    }
	
	public int getOpenRmaId(int outletId) {
		// @sql:on
		object = sql.getDatum(new Object[] { outletId, outletId },""
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
		return object == null ? 0 : (int) object;
	}

	public boolean isRMA(int soId) {
		// @sql:on
		object = sql.getDatum(soId, ""
				+ "SELECT sales_id "
				+ "  FROM sales_detail "
				+ " WHERE     item_id < 0 "
				+ "       AND sales_id = ? "
				+ " LIMIT 1;");
		// @sql:off
		return object == null ? false : true;
	}

	public BigDecimal getRmaLimit(int outletId, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.YEAR, -1);
		Date start = new Date(cal.getTimeInMillis());
		// @sql:on
		object = sql.getDatum(new Object[] { outletId, start, date },""
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
		return object == null ? BigDecimal.ZERO : (BigDecimal) object;
	}

	public int getOrderIdWithSameDiscount(int itemId, int customerId, Date date, String type) {
		// @sql:on
		object = sql.getDatum(new Object[] {itemId, customerId, date }, ""
				+ SQL.addItemParentStmt() 
				+ ",\n"
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
				+ "                INNER JOIN item_master AS im ON im.id = ip.child_id "
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
		return object == null ? 0 : (int) object;
	}

	public boolean hasOpenPO(Date date, int vendorId) {
		// @sql:on
		object = sql.getDatum(new Object[] { date, vendorId },""
				+ "SELECT purchase_id " 
				+ "  FROM purchase_header AS ph "
				+ "       INNER JOIN vendor_specific AS vs "
				+ "          ON ph.customer_id = vs.vendor_id "
				+ " WHERE     purchase_date <= ? "
				+ "   	  AND vs.vendor_id = ? "
				+ " ORDER BY purchase_date DESC " 
				+ " LIMIT 1;");
		// @sql:off
		return object == null ? false : true;
	}

	public Object[][] getReceivedReturnedMaterials(int soId) {
		// @sql:on
		return sql.getDataArray(soId,""
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

	public Object[][] getReceivedMaterials(int soId) {
		// @sql:on
		return sql.getDataArray(soId, ""
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

	public Object[][] getNetItemQtyToLoad(int soID) {
	    // @sql:on
	    return sql.getDataArray(soID, "" 
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
	            + "                  INNER JOIN customer_master AS cm\n"
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

	public boolean hasUnpaidIncentives(int partnerId, Date postDate) {
	    return false;
    }
	
	public int getMaxId(String type) {
		String id = "id";
		String table = "_master";
		if (type.equals("remittance")) {
			id = "remit_id";
			table = "_header";
		}
		object = sql.getDatum(""
				+ "SELECT max(" + id + ")\n"
				+ "  FROM " + type + table
				);
		return object == null ? 1 : (int) object;
	}

	public int getMinId(String type) {
		String id = "id";
		String table = "_master";
		if (type.equals("remittance")) {
			id = "remit_id";
			table = "_header";
		}
		object = sql.getDatum(""
				+ "SELECT min(" + id + ")\n"
				+ "  FROM " + type + table
				);
		return object == null ? 1 : (int) object;
	}
}

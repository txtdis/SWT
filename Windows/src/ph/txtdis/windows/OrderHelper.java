package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;
import java.util.Calendar;

public class OrderHelper {
	private String type;
	private SQL sql;
	private int orderId;

	public OrderHelper() {
		super();
		sql = new SQL();
	}

	public OrderHelper(int orderId) {
		this();
		if (orderId < 0) {
			type = "delivery";
			this.orderId = -orderId;
		} else {
			type = "invoice";
			this.orderId = orderId;
		}
	}

	public String[] getSeries() {
		Object[] ao = sql.getData("SELECT DISTINCT series "
				+ "FROM	invoice_booklet ORDER BY series");
		return (ao != null ? Arrays.copyOf(ao, ao.length, String[].class)
				: null);
	}

	public boolean hasSeries(String series) {
		Object o = sql.getDatum(series,
				"SELECT series FROM	invoice_booklet WHERE series = ? LIMIT 1 ");
		return (o != null ? true : false);
	}

	public boolean hasDetail() {
		Object o = sql.getDatum(orderId, "SELECT " + type + "_id " + "FROM "
				+ type + "_detail WHERE " + type + "_id = ? LIMIT 1 ");
		return (o == null ? false : true);
	}

	public int getFirstLineItemId(String series) {
		Object o;
		if (type.equals("invoice")) {
			o = sql.getDatum(new Object[] { orderId, series }, ""
					+ "SELECT id.item_id FROM invoice_header AS ih "
					+ "INNER JOIN invoice_detail AS id "
					+ "ON ih.invoice_id = id.invoice_id "
					+ "WHERE ih.invoice_id = ? AND ih.series = ? "
					+ "AND line_id = 1;");
		} else {
			o = sql.getDatum(orderId, "SELECT id.item_id FROM " + type
					+ "_header AS ih INNER JOIN " + type
					+ "_detail AS id ON	ih." + type + "_id = id." + type
					+ "_id WHERE ih." + type + "_id = ?	AND line_id = 1;");
		}
		return o != null ? (int) o : 0;
	}

	public boolean hasBeenUsed(String series) {
		Object o;
		if (type.equals("invoice")) {
			o = sql.getDatum(new Object[] { orderId, series }, ""
					+ "SELECT invoice_id FROM invoice_header "
					+ "WHERE invoice_id = ? AND	series = ?;");
		} else {
			o = sql.getDatum(orderId, "SELECT " + type + "_id FROM 	" + type
					+ "_header WHERE " + type + "_id = ?;");
		}
		return (o == null ? false : true);
	}

	public int getLastId(String series) {
		Object o = sql
				.getDatum(
						new Object[] { orderId, orderId, series },
						""
								+ "WITH booklet\n"
								+ "     AS (SELECT start_id, end_id, series\n"
								+ "           FROM invoice_booklet\n"
								+ "          WHERE start_id <= ? AND end_id >= ? AND series LIKE ?),\n"
								+ "     max_id\n"
								+ "     AS (SELECT max (invoice_id) AS id\n"
								+ "           FROM invoice_header AS ih, booklet AS b\n"
								+ "          	   WHERE invoice_id BETWEEN start_id AND end_id\n"
								+ "                AND ih.series = b.series)\n"
								+ "SELECT CASE WHEN id IS NULL THEN start_id - 1 ELSE id END\n"
								+ "  FROM max_id, booklet\n");
		return (o == null ? 0 : (int) o);
	}

	public boolean isIdStartOfBooklet(String series) {
		Object o = sql.getDatum(new Object[] { orderId, series }, ""
				+ "SELECT start_id FROM invoice_booklet "
				+ "WHERE start_id = ? AND series = ?");
		return (o != null ? true : false);
	}

	public BigDecimal getInvoiceBalance() {
		return (BigDecimal) sql
				.getDatum(
						orderId,
						""
								+ "SELECT CASE WHEN ih.actual IS null THEN 0 ELSE ih.actual END - "
								+ "       CASE WHEN p.payment IS null THEN 0 ELSE p.payment END "
								+ "FROM " + type + "_header AS ih "
								+ "LEFT JOIN payment AS p " + "ON " + type
								+ "_id = p.order_id WHERE ih." + type
								+ "_id = ? ");
	}

	public boolean wasPrinted() {
		Object o = sql.getDatum(orderId,
				"SELECT copy FROM print WHERE so_id = ? ");
		return (o == null ? false : true);
	}

	public int getOrderId(int soId) {
		Object i = sql.getDatum(soId, "SELECT invoice_id "
				+ "FROM invoice_header WHERE 	ref_id = ? ");
		Object d = sql.getDatum(soId, "SELECT delivery_id "
				+ "FROM delivery_header WHERE ref_id = ? ");
		return (i == null ? (d == null ? 0 : -(int) d) : (int) i);
	}

	public int getSoId(Date postDate, int outletId) {
		Object o = sql
				.getDatum(
						new Object[] { postDate, outletId },
						""
								+ "select sales_id\n"
								+ "  from sales_header as sh\n"
								+ "  inner join customer_master as cm on sh.customer_id = cm.id\n"
								+ " where sales_date = ? and sh.customer_id = ?\n");
		return o == null ? 0 : (int) o;
	}

	public int getPartnerId(int soID, String type) {
		Object o = sql.getDatum(soID, "" + "SELECT customer_id FROM " + type
				+ "_header\n" + "WHERE " + type + "_id = ?\n");
		return o == null ? 0 : (int) o;
	}

	public Object[] getSoIdAndItemDiscountGroup(int itemId, int outletId,
			Date postDate) {
		return sql.getData(new Object[] { itemId, outletId, postDate }, ""
				+ "SELECT sales_detail.sales_id, item_family.name "
				+ "FROM sales_header INNER JOIN sales_detail "
				+ "ON sales_header.sales_id = sales_detail.sales_id "
				+ "INNER JOIN item_parent "
				+ "ON sales_detail.item_id = item_parent.child_id "
				+ "INNER JOIN item_family "
				+ "ON item_parent.parent_id = item_family.id "
				+ "WHERE item_family.tier_id = 1 "
				+ "AND item_family.id = ? "
				+ "AND sales_header.customer_id = ? " + "AND sales_date = ? ");
	}

	public Date getDate() {
		Date date = (Date) sql.getDatum(orderId, ""
				+ "SELECT 	CASE WHEN invoice_date IS NULL "
				+ "			THEN 'epoch' ELSE invoice_date END AS invoice_date "
				+ "FROM		invoice_header " + "WHERE 	invoice_id = ? ");
		return date;
	}

	public boolean isFromExTruck(int soId) {
		Object o = sql.getDatum(soId, "SELECT c.id "
				+ "FROM channel AS c INNER JOIN customer_master AS cm "
				+ "ON c.id = cm.type_id INNER JOIN sales_header AS sh "
				+ "ON cm.id = sh.customer_id WHERE 	sales_id = ? "
				+ "AND c.name = 'ROUTE' ");
		return o == null ? false : true;
	}

	public int getOpenRMA(int outletId) {
		Object o = sql.getDatum(new Object[] { outletId, outletId }, ""
				+ "WITH invoices AS ( "
				+ " SELECT ref_id AS id FROM invoice_header "
				+ " WHERE customer_id = ? AND actual < 0 "
				+ "), sales_orders AS ( "
				+ "	SELECT DISTINCT sd.sales_id AS id "
				+ "	FROM sales_header AS sh	INNER JOIN sales_detail AS sd "
				+ "	ON sd.sales_id = sh.sales_id "
				+ "	WHERE sh.customer_id = ? AND sd.item_id < 0 "
				+ ") SELECT s.id FROM sales_orders AS s "
				+ "LEFT OUTER JOIN invoices AS i ON s.id = i.id "
				+ "WHERE i.id IS null");
		return o == null ? 0 : (int) o;
	}

	public BigDecimal getReturnedMaterialBalance(int outletId, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.YEAR, -1);
		Date start = new Date(cal.getTimeInMillis());
		Object object = sql
				.getDatum(
						new Object[] { outletId, start, date },
						"WITH invoices AS ( "
								+ "	SELECT 	invoice_id AS id "
								+ "	FROM 	invoice_header "
								+ "	WHERE 	customer_id = ? "
								+ "	AND 	invoice_date BETWEEN ? AND ? "
								+ "), "
								+ "sold AS ( "
								+ "	SELECT 	sum(CASE WHEN actual IS NULL THEN 0 ELSE actual END) AS "
								+ "			sale "
								+ "	FROM 	invoice_header AS ih"
								+ "	INNER JOIN invoices AS i "
								+ "		ON	ih.invoice_id = i.id "
								+ "	WHERE	actual >= 0 "
								+ "), "
								+ "returned AS ( "
								+ "	SELECT 	sum(CASE WHEN actual IS NULL THEN 0 ELSE actual END) AS "
								+ "			rebate "
								+ "	FROM 	invoice_header AS ih"
								+ "	INNER JOIN invoices AS i "
								+ "		ON	ih.invoice_id = i.id "
								+ "	WHERE	actual < 0 "
								+ ") "
								+ "SELECT CASE WHEN sale IS NULL THEN 0 ELSE sale END "
								+ "		* 0.01 "
								+ "		- CASE WHEN rebate IS NULL THEN 0 ELSE rebate END "
								+ "FROM sold, returned");
		return object == null ? BigDecimal.ZERO : (BigDecimal) object;
	}
}

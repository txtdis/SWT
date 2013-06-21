package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;

public class OrderHelper {
	private String log, update, update1, update2, type;
	private Object param;
	private SQL sql;
	private int orderId, oldId, newId;
	private BigDecimal actual, payment;
	private Connection conn;

	public OrderHelper() {
		super();
		sql = new SQL();
		conn = Database.getInstance().getConnection();
	}

	public OrderHelper(int orderId) {
		this();
		if(orderId < 0) {
			type = "delivery";
			this.orderId = -orderId;
		} else {
			type = "invoice";
			this.orderId = orderId;
		}
	}

	public String[] getSeries() {
		Object[] ao = sql.getData("" +
				"SELECT	DISTINCT series " +
				"FROM	invoice_booklet " +
				"ORDER BY series" );
		return (ao != null ? Arrays.copyOf(ao, ao.length, String[].class) : null);
	}

	public boolean hasSeries(String series) {
		Object o = sql.getDatum(series, "" +
				"SELECT	series " +
				"FROM	invoice_booklet " +
				"WHERE	series = ? " +
				"LIMIT 1 ");
		return (o != null ? true : false);
	}

	public boolean hasDetail() {
		Object o = sql.getDatum(orderId, "" + 
				"SELECT " + type + "_id " +
				"FROM 	" + type + "_detail " +
				"WHERE 	" + type + "_id = ? " +
				"LIMIT 1 "
				);
		return (o == null ? false : true);
	}

	public int getFirstLineItemId(String series) {
		Object o;
		System.out.println(type);
		if(type.equals("invoice")) {
			o = sql.getDatum(new Object[] {orderId, series}, "" + 
					"SELECT id.item_id " +
					"FROM 	invoice_header AS ih " +
					"INNER JOIN invoice_detail AS id " +
					"	 ON	ih.invoice_id = id.invoice_id " +
					" WHERE	ih.invoice_id = ? " +
					"	AND	ih.series = ? " +
					"	AND line_id = 1;");
		} else {
			o = sql.getDatum(orderId, "" + 
					"SELECT id.item_id " +
					"FROM 	" + type + "_header AS ih " +
					"INNER JOIN " + type + "_detail AS id " +
					"	 ON	ih." + type + "_id = id." + type + "_id " +
					" WHERE	ih." + type + "_id = ? " +
					"	AND line_id = 1;");
		}
		return o != null ? (int) o : 0;
	}

	public boolean hasBeenUsed(String series) {
		Object o;
		if(type.equals("invoice")) {
			o = sql.getDatum(new Object[] {orderId, series}, "" + 
					"SELECT invoice_id " +
					"FROM 	invoice_header " +
					"WHERE 	invoice_id = ? " +
					"	AND	series = ?;");
		} else {
			o = sql.getDatum(orderId, "" + 
					"SELECT " + type + "_id " +
					"FROM 	" + type + "_header " +
					"WHERE 	" + type + "_id = ?;");			
		}
		return (o == null ? false : true);
	}

	public int getLastId(String series) {
		Object o = sql.getDatum(new Object[] {orderId, orderId, series}, "" + 
				"WITH booklet\n" +
				"     AS (SELECT start_id, end_id, series\n" +
				"           FROM invoice_booklet\n" +
				"          WHERE start_id <= ? AND end_id >= ? AND series LIKE ?),\n" +
				"     max_id\n" +
				"     AS (SELECT max (invoice_id) AS id\n" +
				"           FROM invoice_header AS ih, booklet AS b\n" +
				"          	   WHERE invoice_id BETWEEN start_id AND end_id\n" +
				"                AND ih.series = b.series)\n" +
				"SELECT CASE WHEN id IS NULL THEN start_id - 1 ELSE id END\n" +
				"  FROM max_id, booklet\n" 
				);
		return (o == null ? 0 : (int) o);
	}
	
	public boolean isIdStartOfBooklet(String series) {
		Object o = sql.getDatum(new Object[] {orderId, series}, "" + 
		" SELECT start_id\n" +
		" 	FROM invoice_booklet\n" +
		"  WHERE start_id = ?\n" +
		"	 AND series = ?\n" +
		"");		
		return (o != null ? true : false);
	}

	public BigDecimal getInvoiceBalance() {
		return (BigDecimal) sql.getDatum(orderId, "" + 
				"SELECT CASE WHEN ih.actual IS null THEN 0 ELSE ih.actual END - " +
				" 		CASE WHEN p.payment IS null THEN 0 ELSE p.payment END " +
				"FROM 	" + type + "_header AS ih " +
				"LEFT JOIN payment AS p " +
				"ON " + type + "_id = p.order_id " +
				"WHERE	ih." + type + "_id = ? "
				);
	}

	public boolean wasPrinted() {
		Object o = sql.getDatum(orderId, "" + 
				"SELECT copy  " +
				"FROM 	print " +
				"WHERE 	so_id = ? " 
				);
		return (o == null ? false : true);
	}

	public int getOrderId(int soId) {
		Object i = sql.getDatum(soId, "" + 
				"SELECT invoice_id " +
				"FROM 	invoice_header " +
				"WHERE 	ref_id = ? " 
				);
		Object d = sql.getDatum(soId, "" + 
				"SELECT delivery_id " +
				"FROM 	delivery_header " +
				"WHERE 	ref_id = ? " 
				);
		return (i == null ? (d == null ? 0 : -(int) d) : (int) i);
	}

	public Date getDate() {
		Date date = (Date) sql.getDatum(orderId, "" + 
				"SELECT 	CASE WHEN invoice_date IS NULL " +
				"			THEN 'epoch' ELSE invoice_date END AS " +
				"		invoice_date " +
				"FROM 	invoice_header " +
				"WHERE 	invoice_id = ? " 
				);
		return date;
	}

	public boolean isFromExTruck(int soId) {
		Object o = sql.getDatum(soId, "" + 
				"SELECT c.id " +
				"FROM 	channel AS c " +
				"INNER JOIN customer_master AS cm " +
				"ON c.id = cm.type_id " +
				"INNER JOIN sales_header AS sh " +
				"ON cm.id = sh.customer_id " +
				"WHERE 	sales_id = ? " +
				"	AND c.name = 'ROUTE' " 
				);
		return o == null ? false : true;
	}

	public int getOpenRMA(int outletId) {
		Object o = sql.getDatum(new Object[] {outletId, outletId}, "" + 
				"WITH " +
				"invoices AS ( " +
				"	SELECT 	ref_id AS id " +
				"	FROM 	invoice_header " +
				"	WHERE 	customer_id = ? " +
				"		AND	actual < 0 " +
				"), " +
				"sales_orders AS ( " +
				"	SELECT DISTINCT " +
				"			sd.sales_id AS id " +
				"	FROM 	sales_header AS sh " +
				"	INNER JOIN sales_detail AS sd " +
				"		ON	sd.sales_id = sh.sales_id " +
				"	WHERE 	sh.customer_id = ? " +
				"		AND sd.item_id < 0 " +
				") " +
				"SELECT s.id " +
				"FROM 	sales_orders AS s " +
				"LEFT OUTER JOIN invoices AS i " +
				"	ON 	s.id = i.id " +
				"WHERE i.id IS null " + 
				"");
		return o == null ? 0 : (int) o;
	}

	public BigDecimal getReturnedMaterialBalance(int outletId, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.YEAR, -1);
		Date start = new Date(cal.getTimeInMillis());
		Object o = sql.getDatum(new Object[] {outletId, start, date}, "" + 
				"WITH " +
				"invoices AS ( " +
				"	SELECT 	invoice_id AS id " +
				"	FROM 	invoice_header " +
				"	WHERE 	customer_id = ? " +
				"	AND 	invoice_date BETWEEN ? AND ? " + 
				"), " +
				"sold AS ( " +
				"	SELECT 	sum(CASE WHEN actual IS NULL THEN 0 ELSE actual END) AS " +
				"			sale "+
				"	FROM 	invoice_header AS ih" +
				"	INNER JOIN invoices AS i " +
				"		ON	ih.invoice_id = i.id " +
				"	WHERE	actual >= 0 " + 
				"), " +
				"returned AS ( " +
				"	SELECT 	sum(CASE WHEN actual IS NULL THEN 0 ELSE actual END) AS " +
				"			rebate " +
				"	FROM 	invoice_header AS ih" +
				"	INNER JOIN invoices AS i " +
				"		ON	ih.invoice_id = i.id " +
				"	WHERE	actual < 0 " + 
				") " +
				"SELECT CASE WHEN sale IS NULL THEN 0 ELSE sale END " +
				"		* 0.01 " +
				"		- CASE WHEN rebate IS NULL THEN 0 ELSE rebate END " +
				"FROM 	sold, " +
				"		returned " + 
				"");
		return o == null ? BigDecimal.ZERO : (BigDecimal) o;
	}

	private void logActivity() {
		PreparedStatement psl = null;
		PreparedStatement psu = null;
		try {
			conn.setAutoCommit(false);
			if (log != null) {
				psl = conn.prepareStatement("" +
						"INSERT INTO irregular_log (activity) VALUES (?) ");
				psl.setString(1, log.replace("\n", " "));
				psl.execute();
			}
			psu = conn.prepareStatement(update);
			psu.setObject(1, param);
			psu.setInt(2, orderId);
			psu.execute();
			conn.commit();
			new InfoDialog(log); 
			if (View.display.getShells() != null)
				View.display.getShells()[0].dispose();
			new InvoiceView(orderId);
		} catch (Exception e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException er) {
					e.printStackTrace();
					new ErrorDialog(er);
				}
			}
			e.printStackTrace();
			new ErrorDialog(e);
		} finally {
			try {
				if (psl != null) psl.close();
				if (psu != null) psu.close();
			} catch (Exception e) {
				e.printStackTrace();
				new ErrorDialog(e);
			}
		}
	}

	private void logActivity2() {
		PreparedStatement psl = null;
		PreparedStatement psh = null;
		PreparedStatement psd = null;
		try {
			conn.setAutoCommit(false);
			psl = conn.prepareStatement("INSERT INTO irregular_log (activity) VALUES (?) ");
			psl.setString(1, log.replace("\n", " "));
			psl.execute();
			psh = conn.prepareStatement(update1);
			psh.setInt(1, newId);
			psh.setInt(2, oldId);
			psh.execute();
			psd = conn.prepareStatement(update2);
			psd.setInt(1, newId);
			psd.setInt(2, oldId);
			psd.execute();
			conn.commit();
			new InfoDialog(log); 
			if (View.display.getShells() != null)
				View.display.getShells()[0].dispose();
			new InvoiceView(orderId);
		} catch (Exception e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException er) {
					e.printStackTrace();
					new ErrorDialog(er);
				}
			}
			e.printStackTrace();
			new ErrorDialog(e);
		} finally {
			try {
				if (psl != null) psl.close();
				if (psh != null) psh.close();
				if (psd != null) psd.close();
				conn.setAutoCommit(true);
			} catch (Exception e) {
				e.printStackTrace();
				new ErrorDialog(e);
			}
		}
	}

	public void changeOrderId(int oldOrderId) {
		log = 	"Invoice ID "+ oldOrderId + 
				"\nwas changed to " + orderId;
		oldId = oldOrderId;
		newId = orderId;
		update1 = "" +
				"UPDATE " + type + "_header " +
				"SET 	" + type + "_id = ? " +
				"WHERE 	" + type + "_id = ? ";
		update2 = "" +
				"UPDATE " + type + "_detail " +
				"SET 	" + type + "_id = ? " +
				"WHERE 	" + type + "_id = ? ";
		logActivity2();
	}

	public void changePartnerId(int oldPartnerId, int partnerId) {
		log = 	"Customer ID of\nInvoice ID "+ orderId + 
				"\nwas changed from\n" + oldPartnerId + " to " + partnerId;
		param = partnerId;
		update = "UPDATE " + type + "_header " +
				"SET 	customer_id = ? " +
				"WHERE 	" + type + "_id = ? ";
		logActivity();
	}

	public void changePostDate(Date oldPostDate, Date postDate) {
		log = 	"Invoice Date of\n ID "+ orderId + 
				"\nwas changed from\n" + oldPostDate + " to " + postDate;
		param = postDate;
		update = "UPDATE " + type + "_header " +
				"SET 	" + type + "_date = ? " +
				"WHERE 	" + type + "_id = ? " ;
		logActivity();
	}	

	public void changeActual(BigDecimal oldActual, BigDecimal actual) {
		log = 	"Written total of\nInvoice ID "+ orderId + 
				"\nwas changed from\n" + oldActual + " to " + actual;
		param = actual;
		update = "UPDATE " + type + "_header " +
				"SET actual = ? " +
				"WHERE " + type + "_id = ? " ;
		logActivity();
	}

	public void deleteRow(int itemId) {
		log = 	"Item ID "+ itemId + 
				"\nin Invoice ID " + orderId + 
				"\nwas deleted";
		param = itemId;
		update= "DELETE FROM " + type + "_detail " +
				"WHERE 	item_id = ? " +
				"	AND " + type + "_id = ? ";
		logActivity();
	}

	public BigDecimal getActual() {
		return actual;
	}

	public BigDecimal getPayment() {
		return payment;
	}
}

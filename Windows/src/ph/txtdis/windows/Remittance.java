package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public class Remittance extends Order {

	private int receiptId;
	private ArrayList<Integer> orderIds;
	private ArrayList<String> seriesList;
	private ArrayList<BigDecimal> payments;
	private BigDecimal revenueSubtotal, paymentSubtotal, balance;
	private Date statusDate;
	private String status, tagger;
	private Time time;

	public Remittance() {
		super();		
	}
	
	public Remittance(int remitId) {
		this();
		this.id = remitId;
		module = "Remittance";
		type = "remit";
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("SERIES", 6), "String"},
				{StringUtils.center("SI/(DR)", 7), "ID"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("CUSTOMER NAME", 42), "String"},
				{StringUtils.center("DATE", 10), "Date"},
				{StringUtils.center("DUE", 10), "Date"},
				{StringUtils.center("BALANCE", 10), "BigDecimal"},
				{StringUtils.center("PAYMENT", 10), "BigDecimal"}
		};
		if (remitId != 0)
		// @sql:on
		objects = sql.getData(remitId, "" +
				"SELECT	rh.bank_id, " +
				"		rh.remit_date, " +
				"		rh.remit_time, " +
				"		rh.ref_id," +
				"		rh.or_id, " +
				"		rh.total," +
				"		upper(rh.user_id), " +
				"		cast(rh.time_stamp AS date)," +
				"		cast(rh.time_stamp AS time)," +
				"		CASE WHEN rc.remit_id IS NULL THEN 'ACTIVE' ELSE 'CANCELLED' END AS status, " +
				"		CASE WHEN rc.user_id IS NULL THEN upper(rh.user_id) ELSE upper(rc.user_id) END AS tagger," +
				"		CASE WHEN rc.time_stamp IS NULL "
				+ "			THEN cast(rh.time_stamp AS date) ELSE cast(rc.time_stamp AS date) END AS status_date " +
				"  FROM	remittance_header AS rh " +
				"       LEFT OUTER JOIN remittance_cancellation AS rc " +
				"          ON rh.remit_id = rc.remit_id " +
				" WHERE	rh.remit_id = ? ");
		// @sql:off
		if(objects != null) {
			partnerId = (int) objects[0];
			setPartnerId(partnerId);
			date = (Date) objects[1];
			time = (Time) objects[2];
			referenceId = (int) objects[3];
			receiptId = objects[4] == null ? 0 : (int) objects[4];
			enteredTotal = (BigDecimal) objects[5];
			inputter = (String) objects[6];
			inputDate = (Date) objects[7];
			inputTime = (Time) objects[8];
			status = (String) objects[8];
			tagger = (String) objects[9];
			statusDate = (Date) objects[10];
			// @sql:on
			data = sql.getDataArray(remitId, "" +
					"WITH remit "
					+ "   AS ( SELECT * " +
					"	 		FROM remittance_detail " +
					"	 	   WHERE remit_id = ? ), " +
					"	  si " +
					"	  AS (SELECT 0, " +
					"				 r.series, " +
					"				 r.order_id, " +
					"				 ih.customer_id, " +
					"				 cm.name, " +
					"				 ih.invoice_date, " +
					"				 ih.invoice_date + " +
					"				 CASE WHEN cd.term IS null THEN 0 ELSE cd.term END AS due_date, " +
					"				  CASE WHEN ih.actual IS null THEN 0 ELSE ih.actual END " +
					"				- CASE WHEN p.payment IS null THEN 0 ELSE p.payment END AS balance," +
					"				 p.payment," +
					"				 r.line_id " +
					"			FROM remit AS r " +
					"				 INNER JOIN invoice_header AS ih " +
					"					ON     r.order_id = ih.invoice_id " +
					"					   AND r.series = ih.series " +
					"				 INNER JOIN payment AS p " +
					"					ON     p.order_id = ih.invoice_id " +
					"					   AND p.series = ih.series " +
					"				 INNER JOIN customer_master AS cm " +
					"					ON ih.customer_id = cm.id " +
					"				 LEFT OUTER JOIN credit_detail AS cd " +
					"					ON ih.customer_id = cd.customer_id)," +
					"	  dr " +
					"	  AS (SELECT 0, " +
					"				 CAST('DR' AS TEXT) AS series, "  +
					"		 		 r.order_id, " +
					"				 dh.customer_id, " +
					"				 cm.name, " +
					"				 dh.delivery_date, " +
					"				 dh.delivery_date + " +
					"				 CASE WHEN cd.term IS null THEN 0 ELSE cd.term END AS due_date, " +
					"			  	  CASE WHEN dh.actual IS null THEN 0 ELSE dh.actual END " +
					"		    	 - CASE WHEN p.payment IS null THEN 0 ELSE p.payment END AS balance, " +
					"				 p.payment," +
					"				 r.line_id " +
					"		    FROM remit AS r " +
					"				 INNER JOIN delivery_header AS dh " +
					"				 	ON -r.order_id = dh.delivery_id " +
					"				 INNER JOIN payment AS p " +
					"					ON -p.order_id = dh.delivery_id " +
					"				 INNER JOIN customer_master AS cm " +
					"					 ON dh.customer_id = cm.id " +
					"				 LEFT OUTER JOIN credit_detail AS cd " +
					"					 ON dh.customer_id = cd.customer_id ) " +
					"SELECT * FROM si " +
					" UNION " +
					"SELECT * FROM dr ");
			// @sql:off
			for (int i = 0, size = data.length; i < size; i++) 
				balance = balance.add((BigDecimal) data[i][8]); 
			balance = enteredTotal.subtract(balance);
		} else {
			balance = BigDecimal.ZERO;
			orderIds = new ArrayList<>();
			seriesList = new ArrayList<>();
			payments = new ArrayList<>();
			revenueSubtotal = BigDecimal.ZERO;
			paymentSubtotal = BigDecimal.ZERO;
			inputter = Login.getUser().toUpperCase();
			tagger = Login.getUser().toUpperCase();
			status = "NEW";
			statusDate = inputDate = DIS.TODAY;
			inputTime = DIS.NOW;
			date = DIS.TOMORROW;
			time = DIS.ZERO_TIME;
		}
	}
	
	public int getId(int bankId, Date date, Time time, int refId) {
		// @sql:on
		object = sql.getDatum(new Object[] { bankId, date, time, refId }, ""
				+ "SELECT remit_id "
				+ "  FROM remittance_header "
				+ " WHERE     bank_id = ? "
				+ "       AND remit_date = ? "
				+ "	      AND remit_time = ? "
				+ "       AND ref_id = ?; ");
		// @sql:off
		return (object == null ? 0 : (int) object);
	}

	public boolean isIdOnFile(int remitId) {
		// @sql:on
		object = sql.getDatum(remitId, ""
				+ "SELECT remit_id "
				+ "  FROM remittance_header "
				+ " WHERE remit_id = ?; ");
		// @sql:off
		return (object == null ? false : true);
	}

	public BigDecimal getPayment(String series, int orderId) {
		// @sql:on
		object = sql.getDatum(new Object[] { series, orderId }, ""
				+ "SELECT payment "
				+ " FROM payment "
				+ "WHERE     series = ? "
				+ "		 AND order_id = ?;");
		// @sql:off
		return object == null ? BigDecimal.ZERO : (BigDecimal) object;
	}

	public boolean isPaymentByCheck(int remitId) {
		// @sql:on
		object = sql.getDatum(remitId, ""
				+ "SELECT remit_id "
				+ "  FROM remittance_header "
				+ " WHERE remit_id = ? "
				+ "      AND remit_time = '00:00:00';");
		// @sql:off
		return (object == null ? false : true);
	}

	public Integer[] getRemitIds(int orderId) {
		// @sql:on
		objects = sql.getData(orderId, ""
				+ "SELECT remit_id "
				+ "  FROM remittance_detail "
				+ " WHERE order_id = ?;");
		// @sql:off
		return Arrays.copyOf(objects, objects.length, Integer[].class);
	}

	public BigDecimal getCashPaymentVersusRemittanceVariance(Date[] beginAndEndDates, int routeId) {
		object = sql.getDatum(new Object[] { beginAndEndDates[0], beginAndEndDates[1], routeId }, ""
				+ "SELECT payment "
				+ "  FROM payment "
				+ " WHERE     series = ? "
				+ "       AND order_id = ?;");
		// @sql:off
		return object == null ? BigDecimal.ZERO : (BigDecimal) object;
	}

	public Time getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
	}

	public Date getStatusDate() {
		return statusDate;
	}

	public String getStatus() {
		return status;
	}

	public String getTagger() {
		return tagger;
	}

	public ArrayList<Integer> getOrderIds() {
		if(orderIds == null)
			orderIds = new ArrayList<>();
		return orderIds;
	}

	public ArrayList<String> getSeriesList() {
		return seriesList;
	}

	public int getReceiptId() {
		return receiptId;
	}

	public void setReceiptId(int receiptId) {
		this.receiptId = receiptId;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public ArrayList<BigDecimal> getPayments() {
		return payments;
	}

	public BigDecimal getRevenueSubtotal() {
		return revenueSubtotal;
	}

	public void setRevenueSubtotal(BigDecimal revenueSubtotal) {
		this.revenueSubtotal = revenueSubtotal;
	}

	public BigDecimal getPaymentSubtotal() {
		return paymentSubtotal;
	}

	public void setPaymentSubtotal(BigDecimal paymentSubtotal) {
		this.paymentSubtotal = paymentSubtotal;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin","localhost");
		Database.getInstance().getConnection("irene","ayin","192,168.1.100");
		new Remittance(953);
		Database.getInstance().closeConnection();
	}
}

package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public class RemitData extends OrderData {

	private int receiptId;
	private ArrayList<Integer> orderIds;
	private ArrayList<String> seriesList;
	private ArrayList<BigDecimal> payments;
	private BigDecimal revenueSubtotal, paymentSubtotal, balance;
	private Date statusDate;
	private Object object;
	private String status, tagger;
	private Time time;

	public RemitData() {
		super();
    }

	public RemitData(Date date) {
		this();		
		type = Type.TRANSMIT;
		tableHeaders = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("DATE", 10), "Date"},
				{StringUtils.center("NAME", 42), "String"},
				{StringUtils.center("TYPE", 16), "String"},
				{StringUtils.center("REFERENCE", 10), "ID"},
				{StringUtils.center("AMOUNT", 12), "BigDecimal"}
		};
		setPartnerId(DIS.MAIN_CASHIER);
		date = statusDate = inputDate = DIS.TODAY;
		time = inputTime = DIS.getServerTime();
		inputter = tagger = Login.user().toUpperCase();
		status = "NEW";
		Object[] parameters = new Object[] {
			DIS.CLOSED_DSR_BEFORE_SO_CUTOFF,
			DIS.MONETARY,
			DIS.MAIN_CASHIER,
			DIS.BRANCH_CASHIER,
			DIS.SALARY_DEDUCTION,
			DIS.SALARY_CREDIT,
			DIS.EWT,
			DIS.LISTING_FEE,
			DIS.DISPLAY_ALLOWANCE,
			DIS.DEALERS_INCENTIVE
		};
		tableData = sql.getTableData(parameters, ""
				// @sql:on
				+ "WITH parameter AS\n" 
				+ "		 (SELECT cast (? AS date) AS start_date,\n" 
				+ "				 ? AS monetary,\n" 
				+ "				 ? AS main_cashier,\n" 
				+ "				 ? AS branch_cashier,\n" 
				+ "				 ? AS deduction,\n" 
				+ "				 ? AS excess,\n" 
				+ "				 ? AS ewt,\n" 
				+ "				 ? AS listing,\n" 
				+ "				 ? AS display,\n" 
				+ "				 ? AS incentive),\n" 
				+ "	 undeposited AS\n" 
				+ "		 (SELECT DISTINCT\n" 
				+ "				 brh.remit_id AS brh_remit_id,\n" 
				+ "				 brh.bank_id AS brh_bank_id,\n" 
				+ "				 brh.ref_id AS brh_ref_id,\n" 
				+ "				 brh.total,\n" 
				+ "				 brh.time_stamp AS brh_timestamp,\n" 
				+ "				 CASE\n" 
				+ "					 WHEN brh.remit_time = '00:00:00' THEN cast ('CHECK' AS text)\n" 
				+ "					 ELSE cast ('CASH' AS text)\n" 
				+ "				 END\n" 
				+ "					 AS brh_type,\n" 
				+ "				 brd.order_id AS brd_order_id,\n" 
				+ "				 brd.series AS brd_series,\n" 
				+ "				 brd.payment AS brd_payment\n" 
				+ "			FROM parameter AS p\n" 
				+ "				 INNER JOIN remit_header AS brh\n" 
				+ "					 ON brh.bank_id <> p.main_cashier AND brh.time_stamp > p.start_date\n" 
				+ "				 INNER JOIN remit_detail AS brd ON brh.remit_id = brd.remit_id\n" 
				+ "				 LEFT JOIN remit_detail AS mrd ON brh.remit_id = mrd.order_id\n" 
				+ "				 LEFT JOIN remit_header AS mrh\n" 
				+ "					 ON mrh.remit_id = mrd.remit_id AND mrh.bank_id = p.main_cashier\n" 
				+ "		   WHERE mrd.remit_id IS NULL),\n" 
				+ "	 summary AS\n" 
				+ "		 (SELECT DISTINCT\n" 
				+ "				 brh_remit_id AS id,\n" 
				+ "				 cast (brh_timestamp AS date) AS remit_date,\n" 
				+ "				 CASE\n" 
				+ "					 WHEN im.name IS NOT NULL THEN\n" 
				+ "						 cmd.name\n" 
				+ "					 ELSE\n" 
				+ "						 CASE\n" 
				+ "							 WHEN id.item_id < 0 OR id.item_id = p.incentive THEN cmi.name\n" 
				+ "							 ELSE cm.name\n" 
				+ "						 END\n" 
				+ "				 END\n" 
				+ "					 AS bank,\n" 
				+ "				 CASE\n" 
				+ "					 WHEN im.id = p.deduction THEN\n" 
				+ "						 'SALARY DEDUCTION'\n" 
				+ "					 ELSE\n" 
				+ "						 CASE\n" 
				+ "							 WHEN im.id = p.excess THEN\n" 
				+ "								 'SALARY CREDIT'\n" 
				+ "							 ELSE\n" 
				+ "								 CASE\n" 
				+ "									 WHEN im.id = p.ewt THEN\n" 
				+ "										 'WITHOLDING TAX'\n" 
				+ "									 ELSE\n" 
				+ "										 CASE\n" 
				+ "											 WHEN im.id in (p.listing, p.display) OR id.item_id = p.incentive THEN\n" 
				+ "												 'CREDIT MEMO'\n" 
				+ "											 ELSE\n" 
				+ "												 CASE\n" 
				+ "													 WHEN id.item_id < 0 THEN 'REFUND/REBATE'\n" 
				+ "													 ELSE brh_type\n" 
				+ "												 END\n" 
				+ "										 END\n" 
				+ "								 END\n" 
				+ "						 END\n" 
				+ "				 END\n" 
				+ "					 AS type,\n" 
				+ "				 CASE\n" 
				+ "					 WHEN im.name IS NOT NULL OR id.item_id < 0 OR id.item_id = p.incentive THEN\n" 
				+ "						 brd_order_id\n" 
				+ "					 ELSE\n" 
				+ "						 brh_ref_id\n" 
				+ "				 END\n" 
				+ "					 AS ref_id,\n" 
				+ "				 CASE\n" 
				+ "					 WHEN im.name IS NOT NULL OR id.item_id < 0 OR id.item_id = p.incentive THEN\n" 
				+ "						 abs (brd_payment)\n" 
				+ "					 ELSE\n" 
				+ "						 total\n" 
				+ "				 END\n" 
				+ "					 AS total\n" 
				+ "			FROM undeposited\n" 
				+ "				 INNER JOIN parameter AS p\n" 
				+ "					 ON    brh_bank_id = p.branch_cashier\n" 
				+ "						OR (brh_bank_id <> p.branch_cashier AND brh_type <> 'CASH')\n" 
				+ "						OR brd_payment < 0\n" 
				+ "				 INNER JOIN customer_header AS cm ON brh_bank_id = cm.id\n" 
				+ "				 LEFT JOIN delivery_header AS dh ON -brd_order_id = dh.delivery_id\n" 
				+ "				 LEFT JOIN delivery_detail AS dd\n" 
				+ "					 ON dh.delivery_id = dd.delivery_id AND dd.line_id = 1\n" 
				+ "				 LEFT JOIN item_header AS im ON dd.item_id = im.id AND im.type_id = p.monetary\n" 
				+ "				 LEFT JOIN customer_header AS cmd ON dh.customer_id = cmd.id\n" 
				+ "				 LEFT JOIN invoice_header AS ih\n" 
				+ "					 ON brd_order_id = ih.invoice_id AND brd_series = ih.series\n" 
				+ "				 LEFT JOIN invoice_detail AS id\n" 
				+ "					 ON ih.invoice_id = id.invoice_id AND ih.series = id.series AND id.line_id = 1\n" 
				+ "				 LEFT JOIN customer_header AS cmi ON ih.customer_id = cmi.id)\n" 
				+ "  SELECT row_number () OVER (ORDER BY id),\n" 
				+ "		 id,\n" 
				+ "		 remit_date,\n" 
				+ "		 bank,\n" 
				+ "		 type,\n" 
				+ "		 ref_id,\n" 
				+ "		 total,\n" 
				+ "		 sum (total) OVER ()\n" 
				+ "	FROM summary\n" 
				+ "ORDER BY id\n" 
				// @sql:off
				);
		enteredTotal = tableData == null ? BigDecimal.ZERO : (BigDecimal) tableData[0][7];
		balance = BigDecimal.ZERO;
	}

	public RemitData(int remitId) {
		this();		
		type = Type.REMIT;
		// @sql:on
		tableHeaders = new String[][] {
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
		this.id = remitId;
		if (remitId != 0)
			headerData = sql.getList(remitId, "" +
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
					"  FROM	remit_header AS rh " +
					"       LEFT OUTER JOIN remit_cancellation AS rc " +
					"          ON rh.remit_id = rc.remit_id " +
					" WHERE	rh.remit_id = ? ");
		// @sql:off
		if(headerData != null) {
			setPartnerId((int) headerData[0]);
			date = (Date) headerData[1];
			time = (Time) headerData[2];
			referenceId = (int) headerData[3];
			receiptId = headerData[4] == null ? 0 : (int) headerData[4];
			enteredTotal = (BigDecimal) headerData[5];
			inputter = (String) headerData[6];
			inputDate = (Date) headerData[7];
			inputTime = (Time) headerData[8];
			status = (String) headerData[9];
			tagger = (String) headerData[10];
			statusDate = (Date) headerData[11];
			tableData = sql.getTableData(remitId, ""
					// @sql:on
					+ "WITH remit AS\n" 
					+ "		 (SELECT *\n" 
					+ "			FROM remit_detail\n" 
					+ "		   WHERE remit_id = ?),\n" 
					+ "	 latest_credit_term_date AS\n" 
					+ "		 (	SELECT customer_id, max (start_date) AS start_date\n" 
					+ "			  FROM credit\n" 
					+ "		  GROUP BY customer_id),\n" 
					+ "	 latest_credit_term AS\n" 
					+ "		 (SELECT cd.customer_id, cd.term\n" 
					+ "			FROM credit AS cd\n" 
					+ "				 INNER JOIN latest_credit_term_date AS lctd\n" 
					+ "					 ON cd.customer_id = lctd.customer_id AND cd.start_date = lctd.start_date),\n" 
					+ "	 si AS\n" 
					+ "		 (SELECT 0,\n" 
					+ "				 r.series,\n" 
					+ "				 r.order_id,\n" 
					+ "				 ih.customer_id,\n" 
					+ "				 cm.name,\n" 
					+ "				 ih.invoice_date,\n" 
					+ "				 ih.invoice_date + CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END AS due_date,\n" 
					+ "				   CASE WHEN ih.actual IS NULL THEN 0 ELSE ih.actual END\n" 
					+ "				 - CASE WHEN r.payment IS NULL THEN 0 ELSE r.payment END\n" 
					+ "					 AS balance,\n" 
					+ "				 r.payment,\n" 
					+ "				 r.line_id\n" 
					+ "			FROM remit AS r\n" 
					+ "				 INNER JOIN invoice_header AS ih\n" 
					+ "					 ON r.order_id = ih.invoice_id AND r.series = ih.series\n" 
					+ "				 INNER JOIN customer_header AS cm ON ih.customer_id = cm.id\n" 
					+ "				 LEFT OUTER JOIN latest_credit_term AS cd ON ih.customer_id = cd.customer_id),\n" 
					+ "	 dr AS\n" 
					+ "		 (SELECT 0,\n" 
					+ "				 CAST ('DR' AS TEXT) AS series,\n" 
					+ "				 r.order_id,\n" 
					+ "				 dh.customer_id,\n" 
					+ "				 cm.name,\n" 
					+ "				 dh.delivery_date,\n" 
					+ "				 dh.delivery_date + CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END AS due_date,\n" 
					+ "				   CASE WHEN dh.actual IS NULL THEN 0 ELSE dh.actual END\n" 
					+ "				 - CASE WHEN r.payment IS NULL THEN 0 ELSE r.payment END\n" 
					+ "					 AS balance,\n" 
					+ "				 r.payment,\n" 
					+ "				 r.line_id\n" 
					+ "			FROM remit AS r\n" 
					+ "				 INNER JOIN delivery_header AS dh ON -r.order_id = dh.delivery_id\n" 
					+ "				 INNER JOIN customer_header AS cm ON dh.customer_id = cm.id\n" 
					+ "				 LEFT OUTER JOIN latest_credit_term AS cd ON dh.customer_id = cd.customer_id),\n" 
					+ "	 deposit AS\n" 
					+ "		 (SELECT 0,\n" 
					+ "				 r.series,\n" 
					+ "				 r.order_id,\n" 
					+ "				 rh.bank_id,\n" 
					+ "				 cm.name,\n" 
					+ "				 rh.remit_date,\n" 
					+ "				 rh.remit_date,\n" 
					+ "				 rh.total - r.payment,\n" 
					+ "				 r.payment,\n" 
					+ "				 r.line_id\n" 
					+ "			FROM remit AS r\n" 
					+ "				 INNER JOIN remit_header AS rh ON r.order_id = rh.remit_id\n" 
					+ "				 INNER JOIN customer_header AS cm ON rh.bank_id = cm.id)\n" 
					+ "SELECT * FROM si\n" 
					+ "UNION\n" 
					+ "SELECT * FROM dr\n" 
					+ "UNION\n" 
					+ "SELECT * FROM deposit\n" 
					// @sql:off
					);
			// @sql:off
			balance = BigDecimal.ZERO;
			for (int i = 0, size = tableData.length; i < size; i++) 
				balance = balance.add((BigDecimal) tableData[i][8]); 
			balance = enteredTotal.subtract(balance);
		} else {
			balance = BigDecimal.ZERO;
			orderIds = new ArrayList<>();
			seriesList = new ArrayList<>();
			payments = new ArrayList<>();
			revenueSubtotal = BigDecimal.ZERO;
			paymentSubtotal = BigDecimal.ZERO;
			inputter = Login.user().toUpperCase();
			tagger = Login.user().toUpperCase();
			status = "NEW";
			statusDate = inputDate = DIS.TODAY;
			date = DIS.TOMORROW;
			inputTime = time = DIS.getServerTime();
		}
	}

	public int getId(int bankId, Date date, Time time, int refId) {
		// @sql:on
		object = sql.getDatum(new Object[] { bankId, date, time, refId }, ""
				+ "SELECT remit_id "
				+ "  FROM remit_header "
				+ " WHERE     bank_id = ? "
				+ "       AND remit_date = ? "
				+ "	      AND remit_time = ? "
				+ "       AND ref_id = ?; ");
		// @sql:off
		return (object == null ? 0 : (int) object);
	}

	public Date getLatestDate() {
		// @sql:on
		object = sql.getDatum(""
				+ "SELECT max(transmit_date) "
				+ "  FROM transmittal_header");
		// @sql:off
		return object == null ? null : (Date) object;
	}

	public boolean isIdOnFile(int remitId) {
		// @sql:on
		object = sql.getDatum(remitId, ""
				+ "SELECT remit_id "
				+ "  FROM remit_header "
				+ " WHERE remit_id = ?; ");
		// @sql:off
		return (object == null ? false : true);
	}

	public BigDecimal getPayment(String series, int orderId) {
		// @sql:on
		object = sql.getDatum(new Object[] { series, orderId }, ""
				+ "SELECT sum(payment) "
				+ " FROM remit_detail "
				+ "WHERE     series = ? "
				+ "		 AND order_id = ?;");
		// @sql:off
		return object == null ? BigDecimal.ZERO : (BigDecimal) object;
	}

	public boolean isPaymentByCheck(int remitId) {
		// @sql:on
		object = sql.getDatum(remitId, ""
				+ "SELECT remit_id "
				+ "  FROM remit_header "
				+ " WHERE remit_id = ? "
				+ "      AND remit_time = '00:00:00';");
		// @sql:off
		return (object == null ? false : true);
	}

	public Integer[] getRemitIds(int orderId) {
		// @sql:on
		headerData = sql.getList(orderId, ""
				+ "SELECT remit_id "
				+ "  FROM remit_detail "
				+ " WHERE order_id = ?;");
		// @sql:off
		return Arrays.copyOf(headerData, headerData.length, Integer[].class);
	}

	public BigDecimal getCashPaymentVersusRemittanceVariance(Date[] beginAndEndDates, int routeId) {
		// @sql:on
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
}

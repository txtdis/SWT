package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class Remittance extends Report {

	private int partnerId, refId, orId, remitId;
	private Date postDate, inputDate, statusDate;
	private Time postTime, inputTime;
	private ArrayList<Integer> orderIds;
	private ArrayList<String> seriesList;
	private ArrayList<BigDecimal> payments;
	private String name, user, status, tagger;
	private BigDecimal runningOrderTotal, runningPaymentTotal, balance, totalPayment;

	public Remittance(int remitId) {
		this.remitId = remitId;
		try {
			postTime = new Time(DIS.TF.parse("00:00").getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		module = "Remittance";
		name = "";
		orderIds = new ArrayList<>();
		seriesList = new ArrayList<>();
		payments = new ArrayList<>();
		runningOrderTotal = BigDecimal.ZERO;
		totalPayment = BigDecimal.ZERO;
		runningPaymentTotal = BigDecimal.ZERO;
		user = Login.user.toUpperCase();
		tagger = Login.user.toUpperCase();
		status = "NEW";
		Calendar cal = Calendar.getInstance();
		statusDate = new Date(cal.getTimeInMillis());
		inputDate = new Date(cal.getTimeInMillis());
		inputTime = new Time(cal.getTimeInMillis());
		cal.add(Calendar.DATE, 1);
		postDate = new Date(cal.getTimeInMillis());
		balance = BigDecimal.ZERO;
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("SERIES", 6), "String"},
				{StringUtils.center("SI/DR", 7), "ID"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("CUSTOMER NAME", 42), "String"},
				{StringUtils.center("DATE", 10), "Date"},
				{StringUtils.center("DUE", 10), "Date"},
				{StringUtils.center("BALANCE", 10), "BigDecimal"},
				{StringUtils.center("PAYMENT", 10), "BigDecimal"}
		};
		if (remitId == 0)
			return;
		Object[] os = new SQL().getData(remitId, "" +
				"SELECT	rh.bank_id, " +
				"		cm.name, " +
				"		rh.remit_date, " +
				"		rh.remit_time, " +
				"		rh.ref_id," +
				"		rh.or_id, " +
				"		rh.total," +
				"		rh.user_id, " +
				"		rh.time_stamp," +
				"		CASE WHEN rc.remit_id IS NULL THEN 'ACTIVE' ELSE 'CANCELLED' END AS status, " +
				"		rc.user_id AS tagger," +
				"		rc.time_stamp AS status_date " +
				"FROM	remittance_header AS rh " +
				"INNER JOIN customer_master AS cm " +
				"ON rh.bank_id = cm.id " +
				"LEFT OUTER JOIN remittance_cancellation AS rc " +
				"ON rh.remit_id = rc.remit_id " +
				"WHERE	rh.remit_id = ? "
				);
		if(os != null) {
			partnerId = os[0] == null ? 0 : (int) os[0];
			name = (String) os[1];
			postDate = (Date) os[2];
			postTime = (Time) os[3];
			refId = os[4] == null ? 0 : (int) os[4];
			orId = os[5] == null ? 0 : (int) os[5];
			totalPayment = os[6] == null ? BigDecimal.ZERO : (BigDecimal) os[6];
			user = ((String) os[7]).toUpperCase();
			long ts = ((Timestamp) os[8]).getTime();
			inputDate = new Date(ts);
			inputTime = new Time(ts);
			status = (String) os[9];
			tagger = os[10] == null ? user : ((String) os[10]).toUpperCase();
			statusDate = os[11] == null ? statusDate : new Date (((Timestamp) os[11]).getTime());

			data = new SQL().getDataArray(remitId, "" +
					"WITH " +
					"remit AS ( " +
					"	SELECT 	* " +
					"	FROM 	remittance_detail " +
					"	WHERE 	remit_id = ? " +
					"), " +
					"si AS (" +
					"	SELECT 	0, " +
					"			r.series, " +
					"			r.order_id, " +
					"			ih.customer_id, " +
					"			cm.name, " +
					"			ih.invoice_date, " +
					"			ih.invoice_date + " +
					"				CASE WHEN cd.term IS null " +
					"					THEN 0 ELSE cd.term END AS " +
					"			due_date, " +
					"				CASE WHEN ih.actual IS null " +
					"					THEN 0 ELSE ih.actual END - " +
					"				CASE WHEN p.payment IS null " +
					"					THEN 0 ELSE p.payment END AS " +
					"			balance," +
					"			p.payment," +
					"			r.line_id " +
					"	FROM	remit AS r " +
					"	INNER JOIN	invoice_header AS ih " +
					"		ON 	r.order_id = ih.invoice_id " +
					"		AND r.series = ih.series " +
					"	INNER JOIN payment AS p " +
					"		ON 	p.order_id = ih.invoice_id " +
					"		AND p.series = ih.series " +
					"	INNER JOIN customer_master AS cm " +
					"		ON	ih.customer_id = cm.id " +
					"	LEFT OUTER JOIN credit_detail AS cd " +
					"		ON ih.customer_id = cd.customer_id " +
					")," +
					"dr AS ( " +
					"	SELECT 	0, " +
					"			CAST('DR' AS TEXT) AS series, "  +
					"			r.order_id, " +
					"			dh.customer_id, " +
					"			cm.name, " +
					"			dh.delivery_date, " +
					"			dh.delivery_date + " +
					"				CASE WHEN cd.term IS null " +
					"					THEN 0 ELSE cd.term END AS " +
					"			due_date, " +
					"				CASE WHEN dh.actual IS null " +
					"					THEN 0 ELSE dh.actual END - " +
					"				CASE WHEN p.payment IS null " +
					"					THEN 0 ELSE p.payment END AS " +
					"			balance," +
					"			p.payment," +
					"			r.line_id " +
					"	FROM	remit AS r " +
					"	INNER JOIN	delivery_header AS dh " +
					"		ON 	-r.order_id = dh.delivery_id " +
					"	INNER JOIN payment AS p " +
					"		ON 	-p.order_id = dh.delivery_id " +
					"	INNER JOIN customer_master AS cm " +
					"		ON	dh.customer_id = cm.id " +
					"	LEFT OUTER JOIN credit_detail AS cd " +
					"		ON dh.customer_id = cd.customer_id " +
					") " +
					"SELECT * " +
					"FROM si " +
					"UNION " +
					"SELECT *" +
					"FROM dr " +
					"ORDER BY line_id ");
			for (int i = 0; i < data.length; i++) 
				balance = balance.add((BigDecimal) data[i][8]); 
			balance = totalPayment.subtract(balance);
		}
	}

	public int getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(int partnerId) {
		this.partnerId = partnerId;
	}

	public Date getPostDate() {
		return postDate;
	}

	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}

	public Time getPostTime() {
		return postTime;
	}

	public void setPostTime(Time postTime) {
		this.postTime = postTime;
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

	public String getUser() {
		return user;
	}

	public Date getInputDate() {
		return inputDate;
	}

	public Time getInputTime() {
		return inputTime;
	}

	public ArrayList<Integer> getOrderIds() {
		return orderIds;
	}

	public ArrayList<String> getSeriesList() {
		return seriesList;
	}

	public String getName() {
		return name;
	}

	public int getRefId() {
		return refId;
	}

	public void setRefId(int refId) {
		this.refId = refId;
	}

	public int getOrId() {
		return orId;
	}

	public void setOrId(int orId) {
		this.orId = orId;
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

	public BigDecimal getTotalPayment() {
		return totalPayment;
	}

	public void setTotalPayment(BigDecimal totalPayment) {
		this.totalPayment = totalPayment;
	}

	public BigDecimal getRunningOrderTotal() {
		return runningOrderTotal;
	}

	public void setRunningOrderTotal(BigDecimal runningOrderTotal) {
		this.runningOrderTotal = runningOrderTotal;
	}

	public BigDecimal getRunningPaymentTotal() {
		return runningPaymentTotal;
	}

	public void setRunningPaymentTotal(BigDecimal runningPaymentTotal) {
		this.runningPaymentTotal = runningPaymentTotal;
	}

	public int getRemitId() {
		return remitId;
	}

	public void setRemitId(int remitId) {
		this.remitId = remitId;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		new Remittance(953);
		Database.getInstance().closeConnection();
	}
}

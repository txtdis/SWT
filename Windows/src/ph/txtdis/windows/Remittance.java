package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class Remittance extends Report {

	private int partnerId, refId, orId, remitId;
	private Date date;
	private Time time;
	private ArrayList<Integer> orderIds;
	private ArrayList<String> seriesList;
	private ArrayList<BigDecimal> payments;
	private String name;
	private BigDecimal runningOrderTotal, runningPaymentTotal, balance, totalPayment;

	public Remittance(int remitId) {
		this.remitId = remitId;
		try {
			time = new Time(DIS.TF.parse("00:00").getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		module = "Remittance";
		name = "";
		date = new DateAdder().plus(1);
		orderIds = new ArrayList<>();
		seriesList = new ArrayList<>();
		payments = new ArrayList<>();
		runningOrderTotal = BigDecimal.ZERO;
		totalPayment = BigDecimal.ZERO;
		runningPaymentTotal = BigDecimal.ZERO;
		balance = BigDecimal.ZERO;
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("SERIES", 6), "String"},
				{StringUtils.center("SI/DR", 6), "ID"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("CUSTOMER NAME", 32), "String"},
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
				"		rh.total " +
				"FROM	remittance_header AS rh " +
				"INNER JOIN customer_master AS cm " +
				"ON rh.bank_id = cm.id " +
				"WHERE	rh.remit_id = ? "
				);
		if(os != null) {
			partnerId = os[0] == null ? 0 : (int) os[0];
			name = (String) os[1];
			date = (Date) os[2];
			time = (Time) os[3];
			refId = os[4] == null ? 0 : (int) os[4];
			orId = os[5] == null ? 0 : (int) os[5];
			totalPayment = os[6] == null ? BigDecimal.ZERO : (BigDecimal) os[6];
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
					"		ON 	p.order_id = dh.delivery_id " +
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Time getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
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

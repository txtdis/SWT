package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

public class Credit {
	private Object[][] data;
	private BigDecimal creditLimit;
	private int term, gracePeriod;
	private Date start;
	
	public Credit() {
	}

	public Credit(BigDecimal creditLimit, int term, int gracePeriod, Date start) {
		this.creditLimit = creditLimit;
		this.term = term;
		this.gracePeriod = gracePeriod;
		this.start = start;
	}

	public Credit(int customer_id) {
		data = new Data().getDataArray(customer_id, "" +
				"SELECT	row_number() OVER(ORDER BY start_date), " +
				"		credit_limit, " +
				"		term, " +
				"			CASE WHEN grace_period IS NULL " +
				"			THEN 0 ELSE grace_period END AS " +
				"		grace_period, " +
				"		start_date " +
				"FROM	credit_detail " +
				"WHERE	customer_id = ? " +
				"ORDER BY start_date "
				);
	}

	public Object[][] getData() {
		return data;
	}
		
	public BigDecimal getCreditLimit() {
		return creditLimit;
	}

	public int getTerm() {
		return term;
	}
	
	public int getTerm(int outletId, Date startDate) {
		Object object = new Data().getDatum(new Object[] {startDate, outletId},"" +
				"SELECT term " +
				"FROM 	credit_detail AS cd1 " +
				"INNER JOIN (" +
				"	SELECT	cd.customer_id, " +
				"			MAX(cd.start_date) AS latest_date " +
				"	FROM	credit_detail AS cd " +
				"	WHERE	cd.start_date <= ? " +
				"	GROUP BY cd.customer_id " +
				") AS cd2 " +
				"ON cd1.customer_id = cd2.customer_id " +
				"	AND cd1.start_date = cd2.latest_date " +
				"WHERE cd1.customer_id = ? "
				);
		return object == null ? 0 : (int) object;
	}	

	public int getGracePeriod() {
		return gracePeriod;
	}

	public Date getDate() {
		return start;
	}
}

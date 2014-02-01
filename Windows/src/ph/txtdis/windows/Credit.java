package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

public class Credit {
	private BigDecimal creditLimit;
	private Date start;
	private int term, gracePeriod;
	private static Query sql = new Query();

	public Credit(BigDecimal creditLimit, int term, int gracePeriod, Date start) {
		this.creditLimit = creditLimit;
		this.term = term;
		this.gracePeriod = gracePeriod;
		this.start = start;
	}

	public static Object[][] getData(int customer_id) {
		return sql.getTableData(customer_id,""
				// @sql:on
				+ "SELECT	row_number() OVER(ORDER BY start_date), "
		        + "		credit_limit, " 
				+ "		term, " 
				+ "			CASE WHEN grace_period IS NULL "
		        + "			THEN 0 ELSE grace_period END AS " 
				+ "		grace_period, " 
		        + "		start_date, " 
				+ " 		upper(user_id)	"
		        + "FROM	credit " 
				+ "WHERE	customer_id = ? " 
		        + "ORDER BY start_date "
				// @sql:off
		        );
	}

	public static int getTerm(int outletId, Date startDate) {
		return (int) sql.getDatum(new Object[] { startDate, outletId },"" 
				// @sql:on
				+ "SELECT term "
				+ "  FROM credit AS cd1 " 
				+ "INNER JOIN (" 
				+ "	SELECT	cd.customer_id, "
				+ "			MAX(cd.start_date) AS latest_date " 
				+ "	FROM	credit AS cd " 
				+ "	WHERE	cd.start_date <= ? "
				+ "	GROUP BY cd.customer_id " 
				+ ") AS cd2 " 
				+ "ON cd1.customer_id = cd2.customer_id "
				+ "	AND cd1.start_date = cd2.latest_date " 
				+ "WHERE cd1.customer_id = ? "
				// @sql:off
		        );
	}

	public Date getStartDate(Date date, int partnerId) {
		return (Date) sql.getDatum(new Object[] { date, partnerId }, ""
		        + "SELECT start_date FROM credit WHERE start_date =< ?  AND customer_id = ? ");
	}

	public BigDecimal getCreditLimit() {
		return creditLimit;
	}

	public int getTerm() {
		return term;
	}

	public int getGracePeriod() {
		return gracePeriod;
	}

	public Date getDate() {
		return start;
	}
}

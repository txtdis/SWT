package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

public class Overdue {
	private Query sql;
	private String cte = "";
	protected Date[] dates;

	public Overdue() {
		sql = new Query();
		dates = new Date[] {DIS.NO_SO_WITH_OVERDUE_CUTOFF, DIS.TODAY};
		// @sql:on
		cte = "WITH cutoff AS\n" 
				+ "		 (	SELECT CAST(? AS date) AS start_date,\n" 
				+ "		           CAST(? AS date) AS end_date),\n" 
				+ SQL.addPaymentStmt() + ", "
				+ "     overdue_invoice " 
				+ "     AS (SELECT invoice_id AS order_id, "
				+ "                ih.series, "
				+ "                ih.customer_id, "
				+ "                invoice_date AS order_date, "
				+ "                  invoice_date "
				+ "                + CASE WHEN term IS NULL THEN 0 ELSE term END "
				+ "                + CASE WHEN grace_period IS NULL THEN 0 ELSE grace_period END "
				+ "                   AS due_date, "
				+ "                  cf.end_date "
				+ "                - invoice_date "
				+ "                - CASE WHEN term IS NULL THEN 0 ELSE term END "
				+ "                - CASE WHEN grace_period IS NULL THEN 0 ELSE grace_period END "
				+ "                   AS days_over, "
				+ "                  CASE WHEN actual IS NULL THEN 0 ELSE actual END "
				+ "                - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END "
				+ "                   AS balance "
				+ "           FROM invoice_header AS ih "
				+ "				   INNER JOIN cutoff AS cf ON ih.invoice_date BETWEEN cf.start_date AND cf.end_date\n" 
				+ "                LEFT JOIN payment AS p "
				+ "                   ON     ih.invoice_id = p.order_id "
				+ "                      AND ih.series = p.series "
				+ "                LEFT OUTER JOIN credit AS cd "
				+ "                   ON ih.customer_id = cd.customer_id "
				+ "          WHERE     ih.actual > 0 ),"
				+ "     overdue_delivery "
				+ "     AS (SELECT delivery_id AS order_id, "
				+ "                cast (' ' AS text) AS series, "
				+ "                dh.customer_id, "
				+ "                delivery_date AS order_date, "
				+ "                  delivery_date "
				+ "                + CASE WHEN term IS NULL THEN 0 ELSE term END "
				+ "                + CASE WHEN grace_period IS NULL THEN 0 ELSE grace_period END "
				+ "                   AS due_date, "
				+ "                  cf.end_date "
				+ "                - delivery_date "
				+ "                - CASE WHEN term IS NULL THEN 0 ELSE term END "
				+ "                - CASE WHEN grace_period IS NULL THEN 0 ELSE grace_period END "
				+ "                   AS days_over, "
				+ "                  CASE WHEN actual IS NULL THEN 0 ELSE actual END "
				+ "                - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END "
				+ "                   AS balance "
				+ "           FROM delivery_header AS dh "
				+ "				   INNER JOIN cutoff AS cf ON dh.delivery_date BETWEEN cf.start_date AND cf.end_date\n" 
				+ "                LEFT JOIN payment AS p ON dh.delivery_id = -p.order_id "
				+ "                LEFT JOIN credit AS cd "
				+ "                       ON dh.customer_id = cd.customer_id "
				+ "          WHERE     dh.actual > 0 ),"
				+ "     overdue_combined "
				+ "     AS (SELECT * "
				+ "           FROM overdue_invoice "
				+ "          WHERE balance > 1 AND days_over > 0 "
				+ "         UNION "
				+ "         SELECT * "
				+ "           FROM overdue_delivery "
				+ "          WHERE balance > 1 AND days_over > 0) ";
		// @sql:off		
	}

	public Object[][] getData(int customerId) {
		// @sql:on
		Object[][] objectArray = sql.getTableData(new Object[] {DIS.NO_SO_WITH_OVERDUE_CUTOFF, DIS.TODAY, customerId}, ""
				+ cte 
				+ ", overdue "
				+ "    AS (SELECT * "
				+ "          FROM overdue_combined "
				+ "         WHERE customer_id = ?) "
				+ "SELECT row_number () OVER (ORDER BY days_over DESC), "
				+ "		  order_id, "
				+ "       series, "
				+ "       order_date, "
				+ "       due_date, "
				+ "       days_over, "
				+ "       balance "
				+ "  FROM overdue " 
				+ " ORDER BY days_over DESC ");
		// @sql:off
		return objectArray;
	}

	public Object[][] getDataDump() {
		// @sql:on
		Object[][] objectArray = sql.getTableData(""
				+ cte
				+ "SELECT cm.id,"
				+ "		  cm.name, "
				+ "       order_id, "
				+ "       series, "
				+ "       order_date, "
				+ "       due_date, "
				+ "       days_over, "
				+ "       balance "
				+ "  FROM overdue_combined AS oc "
				+ "		  INNER JOIN customer_header AS cm "
				+ "			 ON oc.customer_id = cm.id " 
				+ " ORDER BY cm.name ");
		// @sql:off
		return objectArray;
	}

	public BigDecimal getBalance(int customerId) {
		// @sql:on
		Object datum = sql.getDatum(new Object[] {DIS.NO_SO_WITH_OVERDUE_CUTOFF, DIS.TODAY, customerId}, ""
				+ cte + ", overdue "
				+ "    AS (SELECT balance FROM overdue_combined WHERE customer_id = ?)\n"
				+ "SELECT sum(balance) FROM overdue;");
		// @sql:off
		return datum == null ? BigDecimal.ZERO : (BigDecimal) datum;
	}
}

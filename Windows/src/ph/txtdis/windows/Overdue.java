package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

public class Overdue {
	private int customerId;
	private Data sql;
	protected Date[] dates;
	private String string = "", customer;

	public Overdue() {
		sql = new Data();
		dates = new Date[] {DIS.NO_SO_WITH_OVERDUE_CUTOFF};
		// @sql:on
		string = " WITH "
				+ SQL.addPaymentStmt() + ", "
				+ "     overdue_invoice " 
				+ "     AS (SELECT invoice_id AS order_id, "
				+ "                ih.series, "
				+ "                ih.customer_id, "
				+ "                invoice_date AS order_date, "
				+ "                invoice_date + CASE WHEN term IS NULL THEN 0 ELSE term END AS due_date, "
				+ "                  current_date "
				+ "                - invoice_date "
				+ "                - CASE WHEN term IS NULL THEN 0 ELSE term END AS days_over, "
				+ "                  CASE WHEN actual IS NULL THEN 0 ELSE actual END "
				+ "                - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END AS balance "
				+ "           FROM invoice_header AS ih "
				+ "                LEFT JOIN payment AS p "
				+ "                   ON ih.invoice_id = p.order_id AND ih.series = p.series "
				+ "                LEFT OUTER JOIN credit_detail AS cd "
				+ "                   ON ih.customer_id = cd.customer_id "
				+ "          WHERE ih.actual > 0), "
				+ "     overdue_delivery "
				+ "     AS (SELECT delivery_id AS order_id, "
				+ "                cast (' ' AS text) AS series, "
				+ "                dh.customer_id, "
				+ "                delivery_date AS order_date, "
				+ "                delivery_date + CASE WHEN term IS NULL THEN 0 ELSE term END "
				+ "                   AS due_date, "
				+ "                  current_date "
				+ "                - delivery_date "
				+ "                - CASE WHEN term IS NULL THEN 0 ELSE term END "
				+ "                   AS days_over, "
				+ "                  CASE WHEN actual IS NULL THEN 0 ELSE actual END "
				+ "                - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END "
				+ "                   AS balance "
				+ "           FROM delivery_header AS dh "
				+ "                LEFT JOIN payment AS p ON dh.delivery_id = -p.order_id "
				+ "                LEFT OUTER JOIN credit_detail AS cd "
				+ "                   ON dh.customer_id = cd.customer_id "
				+ "          WHERE dh.actual > 0), "
				+ "     overdue_combined "
				+ "     AS (SELECT * "
				+ "           FROM overdue_invoice "
				+ "          WHERE balance > 1 AND days_over > 0 "
				+ "         UNION "
				+ "         SELECT * "
				+ "           FROM overdue_delivery "
				+ "          WHERE balance > 1 AND days_over > 1) ";
		// @sql:off		
	}

	public Overdue(int customerId) {
		this();
		this.customerId = customerId;
		// @sql:on
		string += ", overdue "
				+ "    AS (SELECT * "
				+ "          FROM overdue_combined "
				+ "         WHERE     customer_id = ? "
				+ "	      	      AND order_date >= ?) ";
		// @sql:off			
	}

	public Overdue(String customer) {
		this();
		this.customer = customer;
		// @sql:on
		string += ", route_outlet " 
				+ "    AS (SELECT customer_id AS outlet_id "
		        + "          FROM account INNER JOIN route ON route_id = route.id "
		        + "         WHERE route.name = ?) "
				+ "SELECT customer.name, "
		        + "	      sum (balance) "
		        + "  FROM overdue_combined AS due "
		        + "       INNER JOIN customer_master AS customer ON customer_id = customer.id "
		        + "       INNER JOIN route_outlet ON customer_id = outlet_id " 
		        + " WHERE due_date >= ? "
		        + " GROUP BY customer.name " 
		        + " ORDER BY customer.name ";
		// @sql:off
	}

	public Object[][] getData() {
		// @sql:on
		Object[][] objectArray = sql.getDataArray(new Object[] {customerId, DIS.NO_SO_WITH_OVERDUE_CUTOFF}, ""
				+ string
				+ "SELECT 0, "
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
		Object[][] objectArray = sql.getDataArray(""
				+ string
				+ "SELECT cm.id,"
				+ "		  cm.name, "
				+ "       order_id, "
				+ "       series, "
				+ "       order_date, "
				+ "       due_date, "
				+ "       days_over, "
				+ "       balance "
				+ "  FROM overdue_combined AS oc "
				+ "		  INNER JOIN customer_master AS cm "
				+ "			 ON oc.customer_id = cm.id " 
				+ " ORDER BY cm.name ");
		// @sql:off
		return objectArray;
	}

	public BigDecimal getBalance() {
		// @sql:on
		Object datum = sql.getDatum(new Object[] {customerId, DIS.NO_SO_WITH_OVERDUE_CUTOFF}, ""
				+ string
				+ "SELECT sum(balance) " 
				+ "  FROM overdue " );
		// @sql:off
		return datum == null ? BigDecimal.ZERO : (BigDecimal) datum;
	}

	public Object[][] getRouteOutlets() {
		// @sql:on
		return sql.getDataArray(new Object[] { customer, DIS.NO_SO_WITH_OVERDUE_CUTOFF }, string);
		// @sql:off
	}

	public Date[] getDates() {
		return dates;
	}

	public static void main(String[] args) {
		Object[][] data = new Overdue(24).getData();
		if (data != null) {
			for (Object[] objects : data) {
				for (Object object : objects) {
					System.out.print(object + ", ");
				}
				System.out.println();
			}
		}
	}
}

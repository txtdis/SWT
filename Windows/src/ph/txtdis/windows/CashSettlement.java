package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class CashSettlement extends Report {
	private Data sql;
	private Date startDate, endDate;
	private String stmt;
	
	public CashSettlement() {
		sql = new Data();
	}
	
	public CashSettlement(Date[] loadingDates, int loadedRouteId) {
		this();
		module = "Cash Settlement";
		dates = loadingDates;
		if (dates == null) {
			startDate = DIS.TODAY;
			endDate = DIS.TODAY;
			dates = new Date[] {
			        startDate, endDate };
		}
		startDate = dates[0];
		endDate = dates[1];
		dates = loadingDates;
		routeId = loadedRouteId;

		headers = new String[][] {
		        {
		                StringUtils.center("#", 2), "Line" }, {
		                StringUtils.center("S/I(D/R)", 8), "ID" }, {
		                StringUtils.center("SERIES", 6), "String" }, {
		                StringUtils.center("CUSTOMER", 40), "String" }, {
		    		    StringUtils.center(DIS.CURRENCY_SIGN + " VALUE", 14), "BigDecimal" }, {
		    			StringUtils.center("REMIT #", 8), "ID" }, {
				        StringUtils.center(DIS.CURRENCY_SIGN + " DEPOSIT", 14), "BigDecimal" }, {
					    StringUtils.center(DIS.CURRENCY_SIGN + " GAIN(LOSS)", 14), "BigDecimal" } };

		stmt = ""
				// @sql:on
				+ "WITH parameter\n" 
				+ "     AS (SELECT CAST (? AS date) AS start_date,\n" 
				+ "                CAST (? AS date) AS end_date,\n" 
				+ "                CAST (? AS int) AS route_id),\n" 
				+ "     latest_credit_term_date\n" 
				+ "     AS (  SELECT customer_id, max (start_date) AS start_date\n" 
				+ "             FROM credit_detail\n" 
				+ "         GROUP BY customer_id),\n" 
				+ "     latest_credit_term\n" 
				+ "     AS (SELECT cd.customer_id, cd.term\n" 
				+ "           FROM credit_detail AS cd\n" 
				+ "                INNER JOIN latest_credit_term_date AS lctd\n" 
				+ "                   ON     cd.customer_id = lctd.customer_id\n" 
				+ "                      AND cd.start_date = lctd.start_date),\n" 
				+ "     latest_route_date\n" 
				+ "     AS (  SELECT customer_id, max (start_date) AS start_date\n" 
				+ "             FROM account\n" 
				+ "         GROUP BY customer_id),\n" 
				+ "     latest_route\n" 
				+ "     AS (SELECT a.customer_id, a.route_id\n" 
				+ "           FROM account AS a\n" 
				+ "                INNER JOIN latest_route_date AS lrd\n" 
				+ "                   ON     a.customer_id = lrd.customer_id\n" 
				+ "                      AND a.start_date = lrd.start_date),\n" 
				+ "     sold\n" 
				+ "     AS (SELECT ih.invoice_id AS order_id,\n" 
				+ "                ih.series,\n" 
				+ "                cm.name,\n" 
				+ "                ih.actual,\n" 
				+ "                rd.remit_id,\n" 
				+ "                rd.payment,\n" 
				+ "                  CASE WHEN rd.payment IS NULL THEN 0 ELSE rd.payment END\n" 
				+ "                - ih.actual\n" 
				+ "                   AS variance\n" 
				+ "           FROM invoice_header AS ih\n" 
				+ "                INNER JOIN customer_master AS cm\n" 
				+ "                   ON     ih.customer_id = cm.id\n" 
				+ "                LEFT JOIN latest_credit_term AS lct\n" 
				+ "                   ON ih.customer_id = lct.customer_id\n" 
				+ "                LEFT JOIN latest_route AS lr\n" 
				+ "                   ON ih.customer_id = lr.customer_id\n" 
				+ "                LEFT JOIN remittance_detail AS rd\n" 
				+ "                   ON rd.order_id = ih.invoice_id AND rd.series = ih.series\n" 
				+ "                INNER JOIN parameter AS p\n" 
				+ "                   ON     ih.invoice_date BETWEEN p.start_date AND p.end_date\n" 
				+ "                      AND (lct.term IS NULL OR lct.term = 0)\n" 
				+ "                      AND lr.route_id = p.route_id),\n" 
				+ "     delivered\n" 
				+ "     AS (SELECT -ih.delivery_id AS order_id,\n" 
				+ "                CAST ('DR' AS text) AS series,\n" 
				+ "                cm.name,\n" 
				+ "                ih.actual,\n" 
				+ "                rd.remit_id,\n" 
				+ "                rd.payment,\n" 
				+ "                  CASE WHEN rd.payment IS NULL THEN 0 ELSE rd.payment END\n" 
				+ "                - ih.actual\n" 
				+ "                   AS variance\n" 
				+ "           FROM delivery_header AS ih\n" 
				+ "                INNER JOIN customer_master AS cm\n" 
				+ "                   ON ih.customer_id = cm.id\n" 
				+ "                LEFT JOIN latest_credit_term AS lct\n" 
				+ "                   ON ih.customer_id = lct.customer_id\n" 
				+ "                LEFT JOIN latest_route AS lr\n" 
				+ "                   ON ih.customer_id = lr.customer_id\n" 
				+ "                LEFT JOIN remittance_detail AS rd\n" 
				+ "                   ON rd.order_id = -ih.delivery_id\n" 
				+ "                INNER JOIN parameter AS p\n" 
				+ "                   ON     ih.delivery_date BETWEEN p.start_date\n" 
				+ "                                               AND p.end_date\n" 
				+ "                      AND (lct.term IS NULL OR lct.term = 0)\n" 
				+ "                      AND lr.route_id = p.route_id),\n" 
				+ "     combined\n" 
				+ "     AS (SELECT * FROM sold\n" 
				+ "         UNION\n" 
				+ "         SELECT * FROM delivered)\n" 
				// @sql:off
				;
		data = new Data().getDataArray(new Object[] { startDate, endDate, routeId }, ""
				// @sql:on
				+ stmt 
				+ "SELECT row_number() over(ORDER BY variance),\n"
				+ "       *\n" 
				+ "  FROM combined\n" 
				// @sql:off
				);
	}

	public BigDecimal getTotalVariance() {
		Object variance  = sql.getDatum(new Object[] { startDate, endDate, routeId }, ""
				// @sql:on
				+ stmt
				+ "SELECT sum (variance) AS value\n"
				+ "  FROM combined\n"
				+ " WHERE variance < -1\n"
				// @sql:off
				);
		return variance == null ? BigDecimal.ZERO : (BigDecimal) variance;
	}
}

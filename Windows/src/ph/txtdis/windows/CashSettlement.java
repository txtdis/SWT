package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class CashSettlement extends Report {
		
	public CashSettlement(Date[] dates, int routeId) {
		module = "Cash Settlement";
		dates = dates == null ? new Date[] { DIS.TODAY, DIS.TODAY } : dates;
		this.dates = dates;
		this.routeId = routeId;

		headers = new String[][] {
		        {
		                StringUtils.center("#", 3), "Line" }, {
		                StringUtils.center("S/I(D/R)", 8), "ID" }, {
		                StringUtils.center("SERIES", 6), "String" }, {
		                StringUtils.center("CUSTOMER", 40), "String" }, {
		    		    StringUtils.center(DIS.CURRENCY_SIGN + " VALUE", 14), "BigDecimal" }, {
		    			StringUtils.center("REMIT #", 8), "ID" }, {
				        StringUtils.center(DIS.CURRENCY_SIGN + " DEPOSIT", 14), "BigDecimal" }, {
					    StringUtils.center(DIS.CURRENCY_SIGN + " GAIN(LOSS)", 14), "BigDecimal" } };

		data = new Data().getDataArray(new Object[] { dates[0], dates[1], routeId },""
				// @sql:on
				+ "WITH parameter\n" 
				+ "     AS (SELECT CAST (? AS date) AS start_date,\n" 
				+ "                CAST (? AS date) AS end_date,\n" 
				+ "                CAST (? AS int) AS route_id),\n" 
				+ SQL.addPaymentStmt() + ",\n" 
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
				+ "                pm.remit_id,\n" 
				+ "                pm.payment,\n" 
				+ "                  CASE WHEN pm.payment IS NULL THEN 0 ELSE pm.payment END\n" 
				+ "                - ih.actual\n" 
				+ "                   AS variance\n" 
				+ "           FROM invoice_header AS ih\n" 
				+ "                INNER JOIN customer_master AS cm\n" 
				+ "                   ON     ih.customer_id = cm.id\n" 
				+ "                LEFT JOIN latest_credit_term AS lct\n" 
				+ "                   ON ih.customer_id = lct.customer_id\n" 
				+ "                LEFT JOIN latest_route AS lr\n" 
				+ "                   ON ih.customer_id = lr.customer_id\n" 
				+ "                LEFT JOIN payment AS pm\n" 
				+ "                   ON pm.order_id = ih.invoice_id AND pm.series = ih.series\n" 
				+ "                INNER JOIN parameter AS p\n" 
				+ "                   ON     ih.invoice_date BETWEEN p.start_date AND p.end_date\n" 
				+ "                      AND (lct.term IS NULL OR lct.term = 0)\n" 
				+ "                      AND lr.route_id = p.route_id),\n" 
				+ "     delivered\n" 
				+ "     AS (SELECT -ih.delivery_id AS order_id,\n" 
				+ "                CAST ('DR' AS text) AS series,\n" 
				+ "                cm.name,\n" 
				+ "                ih.actual,\n" 
				+ "                pm.remit_id,\n" 
				+ "                pm.payment,\n" 
				+ "                  CASE WHEN pm.payment IS NULL THEN 0 ELSE pm.payment END\n" 
				+ "                - ih.actual\n" 
				+ "                   AS variance\n" 
				+ "           FROM delivery_header AS ih\n" 
				+ "                INNER JOIN delivery_detail AS dd\n" 
				+ "                   ON     ih.delivery_id = dd.delivery_id\n" 
				+ "                      AND dd.line_id = 1\n" 
				+ "                INNER JOIN item_master AS im\n" 
				+ "                   ON     dd.item_id = im.id\n" 
				+ "                      AND im.name NOT LIKE '%SALARY%'\n" 
				+ "                INNER JOIN customer_master AS cm\n" 
				+ "                   ON ih.customer_id = cm.id\n" 
				+ "                LEFT JOIN latest_credit_term AS lct\n" 
				+ "                   ON ih.customer_id = lct.customer_id\n" 
				+ "                LEFT JOIN latest_route AS lr\n" 
				+ "                   ON ih.customer_id = lr.customer_id\n" 
				+ "                LEFT JOIN payment AS pm\n" 
				+ "                   ON -pm.order_id = ih.delivery_id\n" 
				+ "                INNER JOIN parameter AS p\n" 
				+ "                   ON     ih.delivery_date BETWEEN p.start_date AND p.end_date\n" 
				+ "                      AND (lct.term IS NULL OR lct.term = 0)\n" 
				+ "                      AND lr.route_id = p.route_id),\n" 
				+ "     combined\n" 
				+ "     AS (SELECT * FROM sold\n" 
				+ "         UNION\n" 
				+ "         SELECT * FROM delivered)\n" 
				+ "SELECT row_number() over(ORDER BY variance),\n"
				+ "       order_id,\n" 
				+ "       series,\n" 
				+ "       name,\n" 
				+ "       actual,\n" 
				+ "       remit_id,\n" 
				+ "       payment,\n" 
				+ "       CASE WHEN variance BETWEEN -1 AND 1 THEN 0 ELSE variance END,\n" 
				+ "       sum(CASE WHEN variance BETWEEN -1 AND 1 THEN 0 ELSE variance END) OVER()\n" 
				+ "  FROM combined\n" 
				+ " ORDER BY variance;" 
				// @sql:off
				);
	}

	public BigDecimal getTotalVariance() {
		return data[0][8] == null ? BigDecimal.ZERO : (BigDecimal) data[0][8];
	}
}

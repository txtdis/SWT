package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class RemitSettlement extends RoutedData {

	public RemitSettlement(Date[] dates, int routeId) {
		super(dates, routeId);
		type = Type.REMIT_SETTLEMENT;

		tableHeaders = new String[][] { 
				{ StringUtils.center("#", 3), "Line" }, 
				{ StringUtils.center("REMIT #", 8), "ID" },
				{ StringUtils.center("S/I(D/R)", 8), "ID" },
		        { StringUtils.center(DIS.$ + " PAYMENT", 14), "BigDecimal" },
		        { StringUtils.center("REMIT #", 8), "ID" },
		        { StringUtils.center(DIS.$ + " DEPOSIT", 14), "BigDecimal" },
		        { StringUtils.center(DIS.$ + " GAIN(LOSS)", 14), "BigDecimal" } };

		tableData = new Query().getTableData(new Object[] { dates[0], dates[1], routeId, DIS.MAIN_CASHIER }, ""
				// @sql:on
				+ "WITH parameter\n" 
				+ "     AS (SELECT CAST (? AS date) AS start_date,\n" 
				+ "                CAST (? AS date) AS end_date,\n" 
				+ "                CAST (? AS int) AS route_id,\n" 
				+ "                CAST (? AS int) AS bank_id),\n" 
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
				+ "     deposited_remit\n" 
				+ "     AS (SELECT DISTINCT\n" 
				+ "                rh.remit_id AS id,\n" 
				+ "                rh.bank_id,\n" 
				+ "                remit_date,\n" 
				+ "                remit_time,\n" 
				+ "                CASE WHEN series = 'R' THEN order_id ELSE rh.remit_id END\n" 
				+ "                   AS remit_id,\n" 
				+ "                total\n" 
				+ "           FROM remit_header AS rh\n" 
				+ "                INNER JOIN remit_detail AS rd\n" 
				+ "                   ON rh.remit_id = rd.remit_id\n" 
				+ "                INNER JOIN parameter AS p\n" 
				+ "                   ON     (rh.bank_id = p.bank_id OR remit_time <> '00:00:00')\n" 
				+ "                      AND remit_date BETWEEN p.start_date AND p.end_date + 1),\n" 
				+ "     deposited_sales\n" 
				+ "     AS (SELECT dr.remit_id AS id,\n" 
				+ "                rd.remit_id,\n" 
				+ "                rd.order_id,\n" 
				+ "                rd.payment\n" 
				+ "           FROM remit_detail AS rd\n" 
				+ "                INNER JOIN deposited_remit AS dr\n" 
				+ "                   ON rd.remit_id = dr.remit_id),\n" 
				+ "     combined\n" 
				+ "     AS (  SELECT DISTINCT\n" 
				+ "                  rh.remit_id,\n" 
				+ "                  rd.order_id,\n" 
				+ "                  rd.payment,\n" 
				+ "                  ds.id,\n" 
				+ "                  CASE WHEN ds.id IS NULL THEN 0 ELSE rd.payment END AS deposit,\n" 
				+ "                  CASE WHEN ds.id IS NULL THEN -rd.payment ELSE 0 END\n" 
				+ "                     AS variance\n" 
				+ "             FROM remit_header AS rh\n" 
				+ "                  INNER JOIN remit_detail AS rd\n" 
				+ "                     ON rh.remit_id = rd.remit_id\n" 
				+ "                  LEFT JOIN deposited_sales AS ds ON rh.remit_id = ds.remit_id\n" 
				+ "                  LEFT JOIN invoice_header AS ih ON rd.order_id = ih.invoice_id\n" 
				+ "                  LEFT JOIN delivery_header AS dh\n" 
				+ "                     ON -rd.order_id = dh.delivery_id\n" 
				+ "                  LEFT JOIN latest_route AS lr\n" 
				+ "                     ON lr.customer_id =\n" 
				+ "                           CASE\n" 
				+ "                              WHEN rd.order_id > 0 THEN ih.customer_id\n" 
				+ "                              ELSE dh.customer_id\n" 
				+ "                           END\n" 
				+ "                  INNER JOIN parameter AS p\n" 
				+ "                     ON     p.route_id = lr.route_id\n" 
				+ "                        AND rh.remit_date BETWEEN p.start_date AND p.end_date\n" 
				+ "            WHERE rd.series <> 'R' AND rd.payment > 0\n" 
				+ "         ORDER BY 6, 5)\n" 
				+ "SELECT CAST (row_number() over(ORDER BY variance) AS int),\n"
				+ "       remit_id,\n" 
				+ "       order_id,\n" 
				+ "       payment,\n" 
				+ "       id,\n" 
				+ "       deposit,\n" 
				+ "       variance,\n" 
				+ "       sum(variance) OVER()\n" 
				+ "  FROM combined\n"
				+ " ORDER BY variance;" 
				// @sql:off
		        );
	}

	public BigDecimal getTotalVariance() {
		return tableData == null || tableData[0][7] == 	null ? BigDecimal.ZERO : (BigDecimal) tableData[0][7];
	}
}

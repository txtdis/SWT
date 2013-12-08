package ph.txtdis.windows;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class Receivables extends Report {

	public Receivables() {
		module = "Receivables";
		headers = new String[][]{
				{StringUtils.center("ROUTE", 12), "String"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("CUSTOMER NAME", 28), "String"},
				{StringUtils.center("TOTAL", 13), "BigDecimal"},
				{StringUtils.center("CURRENT", 13), "BigDecimal"},
				{StringUtils.center("1-7", 13), "BigDecimal"},
				{StringUtils.center("8-15", 13), "BigDecimal"},
				{StringUtils.center("16-30", 13), "BigDecimal"},
				{StringUtils.center(">30", 13), "BigDecimal"}
		};

		// Data
		data = new Data().getDataArray("" +
				"WITH latest_route_date\n" +
				"        AS (  SELECT customer_id, max (start_date) AS start_date\n" +
				"                FROM account\n" +
				"            GROUP BY customer_id),\n" +
				"        latest_route\n" +
				"        AS (SELECT a.customer_id, a.route_id\n" +
				"              FROM account AS a\n" +
				"                   INNER JOIN latest_route_date AS lrd\n" +
				"                      ON     a.customer_id = lrd.customer_id\n" +
				"                         AND a.start_date = lrd.start_date),\n" +
				"        latest_credit_term_date\n" +
				"        AS (  SELECT customer_id, max (start_date) AS start_date\n" +
				"                FROM credit_detail\n" +
				"            GROUP BY customer_id),\n" +
				"        latest_credit_term\n" +
				"        AS (SELECT cd.customer_id, cd.term\n" +
				"              FROM credit_detail AS cd\n" +
				"                   INNER JOIN latest_credit_term_date AS lctd\n" +
				"                      ON     cd.customer_id = lctd.customer_id\n" +
				"                         AND cd.start_date = lctd.start_date),\n" +
				"        total_invoice\n" +
				"        AS (  SELECT ih.customer_id,\n" +
				"                     sum (\n" +
				"                        CASE\n" +
				"                           WHEN (  CASE\n" +
				"                                      WHEN ih.actual IS NULL THEN 0\n" +
				"                                      ELSE ih.actual\n" +
				"                                   END\n" +
				"                                 - CASE\n" +
				"                                      WHEN p.payment IS NULL THEN 0\n" +
				"                                      ELSE p.payment\n" +
				"                                   END) < 1\n" +
				"                           THEN\n" +
				"                              0\n" +
				"                           ELSE\n" +
				"                              (  CASE\n" +
				"                                    WHEN ih.actual IS NULL THEN 0\n" +
				"                                    ELSE ih.actual\n" +
				"                                 END\n" +
				"                               - CASE\n" +
				"                                    WHEN p.payment IS NULL THEN 0\n" +
				"                                    ELSE p.payment\n" +
				"                                 END)\n" +
				"                        END)\n" +
				"                        AS bal\n" +
				"                FROM invoice_header AS ih\n" +
				"                     LEFT JOIN payment AS p\n" +
				"                        ON ih.invoice_id = p.order_id AND ih.series = p.series\n" +
				"               WHERE ih.actual > 0 AND ih.invoice_date > '2013-03-31'\n" +
				"            GROUP BY ih.customer_id),\n" +
				"        current_invoice\n" +
				"        AS (  SELECT ih.customer_id,\n" +
				"                     sum (\n" +
				"                        CASE\n" +
				"                           WHEN (  CASE\n" +
				"                                      WHEN ih.actual IS NULL THEN 0\n" +
				"                                      ELSE ih.actual\n" +
				"                                   END\n" +
				"                                 - CASE\n" +
				"                                      WHEN p.payment IS NULL THEN 0\n" +
				"                                      ELSE p.payment\n" +
				"                                   END) < 1\n" +
				"                           THEN\n" +
				"                              0\n" +
				"                           ELSE\n" +
				"                              (  CASE\n" +
				"                                    WHEN ih.actual IS NULL THEN 0\n" +
				"                                    ELSE ih.actual\n" +
				"                                 END\n" +
				"                               - CASE\n" +
				"                                    WHEN p.payment IS NULL THEN 0\n" +
				"                                    ELSE p.payment\n" +
				"                                 END)\n" +
				"                        END)\n" +
				"                        AS bal\n" +
				"                FROM invoice_header AS ih\n" +
				"                     LEFT JOIN latest_credit_term AS cd\n" +
				"                        ON ih.customer_id = cd.customer_id\n" +
				"                     LEFT JOIN payment AS p\n" +
				"                        ON ih.invoice_id = p.order_id AND ih.series = p.series\n" +
				"               WHERE     ih.actual > 0\n" +
				"                     AND (  current_date\n" +
				"                          - ih.invoice_date\n" +
				"                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) <=\n" +
				"                            0\n" +
				"            GROUP BY ih.customer_id),\n" +
				"        t01to07_invoice\n" +
				"        AS (  SELECT ih.customer_id,\n" +
				"                     sum (\n" +
				"                        CASE\n" +
				"                           WHEN (  CASE\n" +
				"                                      WHEN ih.actual IS NULL THEN 0\n" +
				"                                      ELSE ih.actual\n" +
				"                                   END\n" +
				"                                 - CASE\n" +
				"                                      WHEN p.payment IS NULL THEN 0\n" +
				"                                      ELSE p.payment\n" +
				"                                   END) < 1\n" +
				"                           THEN\n" +
				"                              0\n" +
				"                           ELSE\n" +
				"                              (  CASE\n" +
				"                                    WHEN ih.actual IS NULL THEN 0\n" +
				"                                    ELSE ih.actual\n" +
				"                                 END\n" +
				"                               - CASE\n" +
				"                                    WHEN p.payment IS NULL THEN 0\n" +
				"                                    ELSE p.payment\n" +
				"                                 END)\n" +
				"                        END)\n" +
				"                        AS bal\n" +
				"                FROM invoice_header AS ih\n" +
				"                     LEFT JOIN latest_credit_term AS cd\n" +
				"                        ON ih.customer_id = cd.customer_id\n" +
				"                     LEFT JOIN payment AS p\n" +
				"                        ON ih.invoice_id = p.order_id AND ih.series = p.series\n" +
				"               WHERE     ih.actual > 0\n" +
				"                     AND (  current_date\n" +
				"                          - ih.invoice_date\n" +
				"                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) BETWEEN 1\n" +
				"                                                                                     AND 7\n" +
				"            GROUP BY ih.customer_id),\n" +
				"        t08to15_invoice\n" +
				"        AS (  SELECT ih.customer_id,\n" +
				"                     sum (\n" +
				"                        CASE\n" +
				"                           WHEN (  CASE\n" +
				"                                      WHEN ih.actual IS NULL THEN 0\n" +
				"                                      ELSE ih.actual\n" +
				"                                   END\n" +
				"                                 - CASE\n" +
				"                                      WHEN p.payment IS NULL THEN 0\n" +
				"                                      ELSE p.payment\n" +
				"                                   END) < 1\n" +
				"                           THEN\n" +
				"                              0\n" +
				"                           ELSE\n" +
				"                              (  CASE\n" +
				"                                    WHEN ih.actual IS NULL THEN 0\n" +
				"                                    ELSE ih.actual\n" +
				"                                 END\n" +
				"                               - CASE\n" +
				"                                    WHEN p.payment IS NULL THEN 0\n" +
				"                                    ELSE p.payment\n" +
				"                                 END)\n" +
				"                        END)\n" +
				"                        AS bal\n" +
				"                FROM invoice_header AS ih\n" +
				"                     LEFT JOIN latest_credit_term AS cd\n" +
				"                        ON ih.customer_id = cd.customer_id\n" +
				"                     LEFT JOIN payment AS p\n" +
				"                        ON ih.invoice_id = p.order_id AND ih.series = p.series\n" +
				"               WHERE     ih.actual > 0\n" +
				"                     AND (  current_date\n" +
				"                          - ih.invoice_date\n" +
				"                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) BETWEEN 8\n" +
				"                                                                                     AND 15\n" +
				"            GROUP BY ih.customer_id),\n" +
				"        t16to30_invoice\n" +
				"        AS (  SELECT ih.customer_id,\n" +
				"                     sum (\n" +
				"                        CASE\n" +
				"                           WHEN (  CASE\n" +
				"                                      WHEN ih.actual IS NULL THEN 0\n" +
				"                                      ELSE ih.actual\n" +
				"                                   END\n" +
				"                                 - CASE\n" +
				"                                      WHEN p.payment IS NULL THEN 0\n" +
				"                                      ELSE p.payment\n" +
				"                                   END) < 1\n" +
				"                           THEN\n" +
				"                              0\n" +
				"                           ELSE\n" +
				"                              (  CASE\n" +
				"                                    WHEN ih.actual IS NULL THEN 0\n" +
				"                                    ELSE ih.actual\n" +
				"                                 END\n" +
				"                               - CASE\n" +
				"                                    WHEN p.payment IS NULL THEN 0\n" +
				"                                    ELSE p.payment\n" +
				"                                 END)\n" +
				"                        END)\n" +
				"                        AS bal\n" +
				"                FROM invoice_header AS ih\n" +
				"                     LEFT JOIN latest_credit_term AS cd\n" +
				"                        ON ih.customer_id = cd.customer_id\n" +
				"                     LEFT JOIN payment AS p\n" +
				"                        ON ih.invoice_id = p.order_id AND ih.series = p.series\n" +
				"               WHERE     ih.actual > 0\n" +
				"                     AND (  current_date\n" +
				"                          - ih.invoice_date\n" +
				"                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) BETWEEN 16\n" +
				"                                                                                     AND 30\n" +
				"            GROUP BY ih.customer_id),\n" +
				"        t30up_invoice\n" +
				"        AS (  SELECT ih.customer_id,\n" +
				"                     sum (\n" +
				"                        CASE\n" +
				"                           WHEN (  CASE\n" +
				"                                      WHEN ih.actual IS NULL THEN 0\n" +
				"                                      ELSE ih.actual\n" +
				"                                   END\n" +
				"                                 - CASE\n" +
				"                                      WHEN p.payment IS NULL THEN 0\n" +
				"                                      ELSE p.payment\n" +
				"                                   END) < 1\n" +
				"                           THEN\n" +
				"                              0\n" +
				"                           ELSE\n" +
				"                              (  CASE\n" +
				"                                    WHEN ih.actual IS NULL THEN 0\n" +
				"                                    ELSE ih.actual\n" +
				"                                 END\n" +
				"                               - CASE\n" +
				"                                    WHEN p.payment IS NULL THEN 0\n" +
				"                                    ELSE p.payment\n" +
				"                                 END)\n" +
				"                        END)\n" +
				"                        AS bal\n" +
				"                FROM invoice_header AS ih\n" +
				"                     LEFT JOIN latest_credit_term AS cd\n" +
				"                        ON ih.customer_id = cd.customer_id\n" +
				"                     LEFT JOIN payment AS p\n" +
				"                        ON ih.invoice_id = p.order_id AND ih.series = p.series\n" +
				"               WHERE     ih.actual > 0\n" +
				"                     AND (  current_date\n" +
				"                          - ih.invoice_date\n" +
				"                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) >\n" +
				"                            30\n" +
				"                     AND ih.invoice_date > '2013-03-31'\n" +
				"            GROUP BY ih.customer_id),\n" +
				"        aging_invoice\n" +
				"        AS (SELECT r.name AS route,\n" +
				"                   total.customer_id,\n" +
				"                   cm.name AS cust,\n" +
				"                   CASE WHEN total.bal IS NULL THEN 0 ELSE total.bal END\n" +
				"                      AS total_bal,\n" +
				"                   CASE WHEN current.bal IS NULL THEN 0 ELSE current.bal END\n" +
				"                      AS current_bal,\n" +
				"                   CASE WHEN t01to07.bal IS NULL THEN 0 ELSE t01to07.bal END\n" +
				"                      AS t01to07_bal,\n" +
				"                   CASE WHEN t08to15.bal IS NULL THEN 0 ELSE t08to15.bal END\n" +
				"                      AS t08to15_bal,\n" +
				"                   CASE WHEN t16to30.bal IS NULL THEN 0 ELSE t16to30.bal END\n" +
				"                      AS t16to30_bal,\n" +
				"                   CASE WHEN t30up.bal IS NULL THEN 0 ELSE t30up.bal END\n" +
				"                      AS t30up_bal\n" +
				"              FROM customer_master AS cm\n" +
				"                   INNER JOIN total_invoice AS total\n" +
				"                      ON total.customer_id = cm.id\n" +
				"                   LEFT JOIN current_invoice AS current\n" +
				"                      ON current.customer_id = cm.id\n" +
				"                   LEFT JOIN t01to07_invoice AS t01to07\n" +
				"                      ON t01to07.customer_id = cm.id\n" +
				"                   LEFT JOIN t08to15_invoice AS t08to15\n" +
				"                      ON t08to15.customer_id = cm.id\n" +
				"                   LEFT JOIN t16to30_invoice AS t16to30\n" +
				"                      ON t16to30.customer_id = cm.id\n" +
				"                   LEFT JOIN t30up_invoice AS t30up ON t30up.customer_id = cm.id\n" +
				"                   LEFT JOIN latest_route AS a ON a.customer_id = cm.id\n" +
				"                   LEFT JOIN route AS r ON a.route_id = r.id\n" +
				"             WHERE total.bal > 0),\n" +
				"        total_delivery\n" +
				"        AS (  SELECT ih.customer_id,\n" +
				"                     sum (\n" +
				"                        CASE\n" +
				"                           WHEN (  CASE\n" +
				"                                      WHEN ih.actual IS NULL THEN 0\n" +
				"                                      ELSE ih.actual\n" +
				"                                   END\n" +
				"                                 - CASE\n" +
				"                                      WHEN p.payment IS NULL THEN 0\n" +
				"                                      ELSE p.payment\n" +
				"                                   END) < 1\n" +
				"                           THEN\n" +
				"                              0\n" +
				"                           ELSE\n" +
				"                              (  CASE\n" +
				"                                    WHEN ih.actual IS NULL THEN 0\n" +
				"                                    ELSE ih.actual\n" +
				"                                 END\n" +
				"                               - CASE\n" +
				"                                    WHEN p.payment IS NULL THEN 0\n" +
				"                                    ELSE p.payment\n" +
				"                                 END)\n" +
				"                        END)\n" +
				"                        AS bal\n" +
				"                FROM delivery_header AS ih\n" +
				"                     LEFT JOIN latest_credit_term AS cd\n" +
				"                        ON ih.customer_id = cd.customer_id\n" +
				"                     LEFT JOIN payment AS p ON ih.delivery_id = -p.order_id\n" +
				"               WHERE ih.actual > 0 AND ih.delivery_date > '2013-03-31'\n" +
				"            GROUP BY ih.customer_id),\n" +
				"        current_delivery\n" +
				"        AS (  SELECT ih.customer_id,\n" +
				"                     sum (\n" +
				"                        CASE\n" +
				"                           WHEN (  CASE\n" +
				"                                      WHEN ih.actual IS NULL THEN 0\n" +
				"                                      ELSE ih.actual\n" +
				"                                   END\n" +
				"                                 - CASE\n" +
				"                                      WHEN p.payment IS NULL THEN 0\n" +
				"                                      ELSE p.payment\n" +
				"                                   END) < 1\n" +
				"                           THEN\n" +
				"                              0\n" +
				"                           ELSE\n" +
				"                              (  CASE\n" +
				"                                    WHEN ih.actual IS NULL THEN 0\n" +
				"                                    ELSE ih.actual\n" +
				"                                 END\n" +
				"                               - CASE\n" +
				"                                    WHEN p.payment IS NULL THEN 0\n" +
				"                                    ELSE p.payment\n" +
				"                                 END)\n" +
				"                        END)\n" +
				"                        AS bal\n" +
				"                FROM delivery_header AS ih\n" +
				"                     LEFT JOIN latest_credit_term AS cd\n" +
				"                        ON ih.customer_id = cd.customer_id\n" +
				"                     LEFT JOIN payment AS p ON ih.delivery_id = -p.order_id\n" +
				"               WHERE     ih.actual > 0\n" +
				"                     AND (  current_date\n" +
				"                          - ih.delivery_date\n" +
				"                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) <=\n" +
				"                            0\n" +
				"            GROUP BY ih.customer_id),\n" +
				"        t01to07_delivery\n" +
				"        AS (  SELECT ih.customer_id,\n" +
				"                     sum (\n" +
				"                        CASE\n" +
				"                           WHEN (  CASE\n" +
				"                                      WHEN ih.actual IS NULL THEN 0\n" +
				"                                      ELSE ih.actual\n" +
				"                                   END\n" +
				"                                 - CASE\n" +
				"                                      WHEN p.payment IS NULL THEN 0\n" +
				"                                      ELSE p.payment\n" +
				"                                   END) < 1\n" +
				"                           THEN\n" +
				"                              0\n" +
				"                           ELSE\n" +
				"                              (  CASE\n" +
				"                                    WHEN ih.actual IS NULL THEN 0\n" +
				"                                    ELSE ih.actual\n" +
				"                                 END\n" +
				"                               - CASE\n" +
				"                                    WHEN p.payment IS NULL THEN 0\n" +
				"                                    ELSE p.payment\n" +
				"                                 END)\n" +
				"                        END)\n" +
				"                        AS bal\n" +
				"                FROM delivery_header AS ih\n" +
				"                     LEFT JOIN latest_credit_term AS cd\n" +
				"                        ON ih.customer_id = cd.customer_id\n" +
				"                     LEFT JOIN payment AS p ON ih.delivery_id = -p.order_id\n" +
				"               WHERE     ih.actual > 0\n" +
				"                     AND (  current_date\n" +
				"                          - ih.delivery_date\n" +
				"                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) BETWEEN 1\n" +
				"                                                                                     AND 7\n" +
				"            GROUP BY ih.customer_id),\n" +
				"        t08to15_delivery\n" +
				"        AS (  SELECT ih.customer_id,\n" +
				"                     sum (\n" +
				"                        CASE\n" +
				"                           WHEN (  CASE\n" +
				"                                      WHEN ih.actual IS NULL THEN 0\n" +
				"                                      ELSE ih.actual\n" +
				"                                   END\n" +
				"                                 - CASE\n" +
				"                                      WHEN p.payment IS NULL THEN 0\n" +
				"                                      ELSE p.payment\n" +
				"                                   END) < 1\n" +
				"                           THEN\n" +
				"                              0\n" +
				"                           ELSE\n" +
				"                              (  CASE\n" +
				"                                    WHEN ih.actual IS NULL THEN 0\n" +
				"                                    ELSE ih.actual\n" +
				"                                 END\n" +
				"                               - CASE\n" +
				"                                    WHEN p.payment IS NULL THEN 0\n" +
				"                                    ELSE p.payment\n" +
				"                                 END)\n" +
				"                        END)\n" +
				"                        AS bal\n" +
				"                FROM delivery_header AS ih\n" +
				"                     LEFT JOIN latest_credit_term AS cd\n" +
				"                        ON ih.customer_id = cd.customer_id\n" +
				"                     LEFT JOIN payment AS p ON ih.delivery_id = -p.order_id\n" +
				"               WHERE     ih.actual > 0\n" +
				"                     AND (  current_date\n" +
				"                          - ih.delivery_date\n" +
				"                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) BETWEEN 8\n" +
				"                                                                                     AND 15\n" +
				"            GROUP BY ih.customer_id),\n" +
				"        t16to30_delivery\n" +
				"        AS (  SELECT ih.customer_id,\n" +
				"                     sum (\n" +
				"                        CASE\n" +
				"                           WHEN (  CASE\n" +
				"                                      WHEN ih.actual IS NULL THEN 0\n" +
				"                                      ELSE ih.actual\n" +
				"                                   END\n" +
				"                                 - CASE\n" +
				"                                      WHEN p.payment IS NULL THEN 0\n" +
				"                                      ELSE p.payment\n" +
				"                                   END) < 1\n" +
				"                           THEN\n" +
				"                              0\n" +
				"                           ELSE\n" +
				"                              (  CASE\n" +
				"                                    WHEN ih.actual IS NULL THEN 0\n" +
				"                                    ELSE ih.actual\n" +
				"                                 END\n" +
				"                               - CASE\n" +
				"                                    WHEN p.payment IS NULL THEN 0\n" +
				"                                    ELSE p.payment\n" +
				"                                 END)\n" +
				"                        END)\n" +
				"                        AS bal\n" +
				"                FROM delivery_header AS ih\n" +
				"                     LEFT JOIN latest_credit_term AS cd\n" +
				"                        ON ih.customer_id = cd.customer_id\n" +
				"                     LEFT JOIN payment AS p ON ih.delivery_id = -p.order_id\n" +
				"               WHERE     ih.actual > 0\n" +
				"                     AND (  current_date\n" +
				"                          - ih.delivery_date\n" +
				"                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) BETWEEN 16\n" +
				"                                                                                     AND 30\n" +
				"            GROUP BY ih.customer_id),\n" +
				"        t30up_delivery\n" +
				"        AS (  SELECT ih.customer_id,\n" +
				"                     sum (\n" +
				"                        CASE\n" +
				"                           WHEN (  CASE\n" +
				"                                      WHEN ih.actual IS NULL THEN 0\n" +
				"                                      ELSE ih.actual\n" +
				"                                   END\n" +
				"                                 - CASE\n" +
				"                                      WHEN p.payment IS NULL THEN 0\n" +
				"                                      ELSE p.payment\n" +
				"                                   END) < 1\n" +
				"                           THEN\n" +
				"                              0\n" +
				"                           ELSE\n" +
				"                              (  CASE\n" +
				"                                    WHEN ih.actual IS NULL THEN 0\n" +
				"                                    ELSE ih.actual\n" +
				"                                 END\n" +
				"                               - CASE\n" +
				"                                    WHEN p.payment IS NULL THEN 0\n" +
				"                                    ELSE p.payment\n" +
				"                                 END)\n" +
				"                        END)\n" +
				"                        AS bal\n" +
				"                FROM delivery_header AS ih\n" +
				"                     LEFT JOIN latest_credit_term AS cd\n" +
				"                        ON ih.customer_id = cd.customer_id\n" +
				"                     LEFT JOIN payment AS p ON ih.delivery_id = -p.order_id\n" +
				"               WHERE     ih.actual > 0\n" +
				"                     AND (  current_date\n" +
				"                          - ih.delivery_date\n" +
				"                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) >\n" +
				"                            30\n" +
				"                     AND ih.delivery_date > '2013-03-31'\n" +
				"            GROUP BY ih.customer_id),\n" +
				"        aging_delivery\n" +
				"        AS (SELECT r.name AS route,\n" +
				"                   total.customer_id,\n" +
				"                   cm.name AS cust,\n" +
				"                   CASE WHEN total.bal IS NULL THEN 0 ELSE total.bal END\n" +
				"                      AS total_bal,\n" +
				"                   CASE WHEN current.bal IS NULL THEN 0 ELSE current.bal END\n" +
				"                      AS current_bal,\n" +
				"                   CASE WHEN t01to07.bal IS NULL THEN 0 ELSE t01to07.bal END\n" +
				"                      AS t01to07_bal,\n" +
				"                   CASE WHEN t08to15.bal IS NULL THEN 0 ELSE t08to15.bal END\n" +
				"                      AS t08to15_bal,\n" +
				"                   CASE WHEN t16to30.bal IS NULL THEN 0 ELSE t16to30.bal END\n" +
				"                      AS t16to30_bal,\n" +
				"                   CASE WHEN t30up.bal IS NULL THEN 0 ELSE t30up.bal END\n" +
				"                      AS t30up_bal\n" +
				"              FROM customer_master AS cm\n" +
				"                   INNER JOIN total_delivery AS total\n" +
				"                      ON total.customer_id = cm.id\n" +
				"                   LEFT JOIN current_delivery AS current\n" +
				"                      ON current.customer_id = cm.id\n" +
				"                   LEFT JOIN t01to07_delivery AS t01to07\n" +
				"                      ON t01to07.customer_id = cm.id\n" +
				"                   LEFT JOIN t08to15_delivery AS t08to15\n" +
				"                      ON t08to15.customer_id = cm.id\n" +
				"                   LEFT JOIN t16to30_delivery AS t16to30\n" +
				"                      ON t16to30.customer_id = cm.id\n" +
				"                   LEFT JOIN t30up_delivery AS t30up\n" +
				"                      ON t30up.customer_id = cm.id\n" +
				"                   LEFT JOIN latest_route AS a ON a.customer_id = cm.id\n" +
				"                   LEFT JOIN route AS r ON a.route_id = r.id\n" +
				"             WHERE total.bal > 0),\n" +
				"        aging\n" +
				"        AS (SELECT * FROM aging_invoice\n" +
				"            UNION\n" +
				"            SELECT * FROM aging_delivery)\n" +
				"     SELECT route,\n" +
				"            customer_id AS id,\n" +
				"            cust AS name,\n" +
				"            SUM (total_bal) AS total,\n" +
				"            SUM (current_bal) AS current,\n" +
				"            SUM (t01to07_bal) AS t01to07,\n" +
				"            SUM (t08to15_bal) AS t08to15,\n" +
				"            SUM (t16to30_bal) AS t16to30,\n" +
				"            SUM (t30up_bal) AS t30up\n" +
				"       FROM aging\n" +
				"   GROUP BY route, id, name\n" +
				"   ORDER BY total DESC;\n" );
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin","localhost");
		Calendar cal = Calendar.getInstance();
		cal.set(2013, 1, 28);
		Receivables r = new Receivables();
		for (Object[] os : r.getData()) {
			for (Object o : os) {
				System.out.print(o + ", ");
			}
			System.out.println();
		}
		Database.getInstance().closeConnection();
	}

}

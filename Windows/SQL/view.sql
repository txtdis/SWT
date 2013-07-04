CREATE OR REPLACE VIEW payment
AS
     SELECT order_id,
            series,
            sum (CASE WHEN payment IS NULL THEN 0 ELSE payment END) AS payment
       FROM remittance_detail
   GROUP BY order_id, series
   ORDER BY order_id, series;

CREATE OR REPLACE VIEW aging
AS
   WITH total_invoice
        AS (  SELECT ih.customer_id,
                     sum (
                          CASE WHEN actual IS NULL THEN 0 ELSE actual END
                        - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END)
                        AS bal
                FROM invoice_header AS ih
                     LEFT JOIN payment AS p
                        ON ih.invoice_id = p.order_id AND ih.series = p.series
               WHERE ih.actual > 0
            GROUP BY ih.customer_id),
        current_invoice
        AS (  SELECT ih.customer_id,
                     sum (
                          CASE WHEN ih.actual IS NULL THEN 0 ELSE ih.actual END
                        - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END)
                        AS bal
                FROM invoice_header AS ih
                     LEFT OUTER JOIN credit_detail AS cd
                        ON ih.customer_id = cd.customer_id
                     LEFT JOIN payment AS p
                        ON ih.invoice_id = p.order_id AND ih.series = p.series
               WHERE     ih.actual > 0
                     AND (  current_date
                          - ih.invoice_date
                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) <=
                            0
            GROUP BY ih.customer_id),
        t01to07_invoice
        AS (  SELECT ih.customer_id,
                     sum (
                          CASE WHEN ih.actual IS NULL THEN 0 ELSE ih.actual END
                        - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END)
                        AS bal
                FROM invoice_header AS ih
                     LEFT OUTER JOIN credit_detail AS cd
                        ON ih.customer_id = cd.customer_id
                     LEFT JOIN payment AS p
                        ON ih.invoice_id = p.order_id AND ih.series = p.series
               WHERE     ih.actual > 0
                     AND (  current_date
                          - ih.invoice_date
                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) BETWEEN 1
                                                                                     AND 7
            GROUP BY ih.customer_id),
        t08to15_invoice
        AS (  SELECT ih.customer_id,
                     sum (
                          CASE WHEN ih.actual IS NULL THEN 0 ELSE ih.actual END
                        - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END)
                        AS bal
                FROM invoice_header AS ih
                     LEFT OUTER JOIN credit_detail AS cd
                        ON ih.customer_id = cd.customer_id
                     LEFT JOIN payment AS p
                        ON ih.invoice_id = p.order_id AND ih.series = p.series
               WHERE     ih.actual > 0
                     AND (  current_date
                          - ih.invoice_date
                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) BETWEEN 8
                                                                                     AND 15
            GROUP BY ih.customer_id),
        t16to30_invoice
        AS (  SELECT ih.customer_id,
                     sum (
                          CASE WHEN ih.actual IS NULL THEN 0 ELSE ih.actual END
                        - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END)
                        AS bal
                FROM invoice_header AS ih
                     LEFT OUTER JOIN credit_detail AS cd
                        ON ih.customer_id = cd.customer_id
                     LEFT JOIN payment AS p
                        ON ih.invoice_id = p.order_id AND ih.series = p.series
               WHERE     ih.actual > 0
                     AND (  current_date
                          - ih.invoice_date
                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) BETWEEN 16
                                                                                     AND 30
            GROUP BY ih.customer_id),
        t30up_invoice
        AS (  SELECT ih.customer_id,
                     sum (
                          CASE WHEN ih.actual IS NULL THEN 0 ELSE ih.actual END
                        - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END)
                        AS bal
                FROM invoice_header AS ih
                     LEFT OUTER JOIN credit_detail AS cd
                        ON ih.customer_id = cd.customer_id
                     LEFT JOIN payment AS p
                        ON ih.invoice_id = p.order_id AND ih.series = p.series
               WHERE     ih.actual > 0
                     AND (  current_date
                          - ih.invoice_date
                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) >
                            30
            GROUP BY ih.customer_id),
        aging_invoice
        AS (SELECT r.name AS route,
                   total.customer_id,
                   cm.name AS cust,
                   CASE WHEN total.bal IS NULL THEN 0 ELSE total.bal END
                      AS total_bal,
                   CASE WHEN current.bal IS NULL THEN 0 ELSE current.bal END
                      AS current_bal,
                   CASE WHEN t01to07.bal IS NULL THEN 0 ELSE t01to07.bal END
                      AS t01to07_bal,
                   CASE WHEN t08to15.bal IS NULL THEN 0 ELSE t08to15.bal END
                      AS t08to15_bal,
                   CASE WHEN t16to30.bal IS NULL THEN 0 ELSE t16to30.bal END
                      AS t16to30_bal,
                   CASE WHEN t30up.bal IS NULL THEN 0 ELSE t30up.bal END
                      AS t30up_bal
              FROM customer_master AS cm
                   INNER JOIN total_invoice AS total
                      ON total.customer_id = cm.id
                   LEFT OUTER JOIN current_invoice AS current
                      ON current.customer_id = cm.id
                   LEFT OUTER JOIN t01to07_invoice AS t01to07
                      ON t01to07.customer_id = cm.id
                   LEFT OUTER JOIN t08to15_invoice AS t08to15
                      ON t08to15.customer_id = cm.id
                   LEFT OUTER JOIN t16to30_invoice AS t16to30
                      ON t16to30.customer_id = cm.id
                   LEFT OUTER JOIN t30up_invoice AS t30up
                      ON t30up.customer_id = cm.id
                   LEFT OUTER JOIN account AS a ON a.customer_id = cm.id
                   LEFT OUTER JOIN route AS r ON a.route_id = r.id
             WHERE total.bal > 0),
        total_delivery
        AS (  SELECT ih.customer_id,
                     sum (
                          CASE WHEN actual IS NULL THEN 0 ELSE actual END
                        - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END)
                        AS bal
                FROM delivery_header AS ih
                     LEFT OUTER JOIN credit_detail AS cd
                        ON ih.customer_id = cd.customer_id
                     LEFT JOIN payment AS p ON ih.delivery_id = -p.order_id
               WHERE ih.actual > 0
            GROUP BY ih.customer_id),
        current_delivery
        AS (  SELECT ih.customer_id,
                     sum (
                          CASE WHEN ih.actual IS NULL THEN 0 ELSE ih.actual END
                        - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END)
                        AS bal
                FROM delivery_header AS ih
                     LEFT OUTER JOIN credit_detail AS cd
                        ON ih.customer_id = cd.customer_id
                     LEFT JOIN payment AS p ON ih.delivery_id = -p.order_id
               WHERE     ih.actual > 0
                     AND (  current_date
                          - ih.delivery_date
                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) <=
                            0
            GROUP BY ih.customer_id),
        t01to07_delivery
        AS (  SELECT ih.customer_id,
                     sum (
                          CASE WHEN ih.actual IS NULL THEN 0 ELSE ih.actual END
                        - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END)
                        AS bal
                FROM delivery_header AS ih
                     LEFT OUTER JOIN credit_detail AS cd
                        ON ih.customer_id = cd.customer_id
                     LEFT JOIN payment AS p ON ih.delivery_id = -p.order_id
               WHERE     ih.actual > 0
                     AND (  current_date
                          - ih.delivery_date
                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) BETWEEN 1
                                                                                     AND 7
            GROUP BY ih.customer_id),
        t08to15_delivery
        AS (  SELECT ih.customer_id,
                     sum (
                          CASE WHEN ih.actual IS NULL THEN 0 ELSE ih.actual END
                        - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END)
                        AS bal
                FROM delivery_header AS ih
                     LEFT OUTER JOIN credit_detail AS cd
                        ON ih.customer_id = cd.customer_id
                     LEFT JOIN payment AS p ON ih.delivery_id = -p.order_id
               WHERE     ih.actual > 0
                     AND (  current_date
                          - ih.delivery_date
                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) BETWEEN 8
                                                                                     AND 15
            GROUP BY ih.customer_id),
        t16to30_delivery
        AS (  SELECT ih.customer_id,
                     sum (
                          CASE WHEN ih.actual IS NULL THEN 0 ELSE ih.actual END
                        - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END)
                        AS bal
                FROM delivery_header AS ih
                     LEFT OUTER JOIN credit_detail AS cd
                        ON ih.customer_id = cd.customer_id
                     LEFT JOIN payment AS p ON ih.delivery_id = -p.order_id
               WHERE     ih.actual > 0
                     AND (  current_date
                          - ih.delivery_date
                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) BETWEEN 16
                                                                                     AND 30
            GROUP BY ih.customer_id),
        t30up_delivery
        AS (  SELECT ih.customer_id,
                     sum (
                          CASE WHEN ih.actual IS NULL THEN 0 ELSE ih.actual END
                        - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END)
                        AS bal
                FROM delivery_header AS ih
                     LEFT OUTER JOIN credit_detail AS cd
                        ON ih.customer_id = cd.customer_id
                     LEFT JOIN payment AS p ON ih.delivery_id = -p.order_id
               WHERE     ih.actual > 0
                     AND (  current_date
                          - ih.delivery_date
                          - (CASE WHEN cd.term IS NULL THEN 0 ELSE cd.term END)) >
                            30
            GROUP BY ih.customer_id),
        aging_delivery
        AS (SELECT r.name AS route,
                   total.customer_id,
                   cm.name AS cust,
                   CASE WHEN total.bal IS NULL THEN 0 ELSE total.bal END
                      AS total_bal,
                   CASE WHEN current.bal IS NULL THEN 0 ELSE current.bal END
                      AS current_bal,
                   CASE WHEN t01to07.bal IS NULL THEN 0 ELSE t01to07.bal END
                      AS t01to07_bal,
                   CASE WHEN t08to15.bal IS NULL THEN 0 ELSE t08to15.bal END
                      AS t08to15_bal,
                   CASE WHEN t16to30.bal IS NULL THEN 0 ELSE t16to30.bal END
                      AS t16to30_bal,
                   CASE WHEN t30up.bal IS NULL THEN 0 ELSE t30up.bal END
                      AS t30up_bal
              FROM customer_master AS cm
                   INNER JOIN total_delivery AS total
                      ON total.customer_id = cm.id
                   LEFT OUTER JOIN current_delivery AS current
                      ON current.customer_id = cm.id
                   LEFT OUTER JOIN t01to07_delivery AS t01to07
                      ON t01to07.customer_id = cm.id
                   LEFT OUTER JOIN t08to15_delivery AS t08to15
                      ON t08to15.customer_id = cm.id
                   LEFT OUTER JOIN t16to30_delivery AS t16to30
                      ON t16to30.customer_id = cm.id
                   LEFT OUTER JOIN t30up_delivery AS t30up
                      ON t30up.customer_id = cm.id
                   LEFT OUTER JOIN account AS a ON a.customer_id = cm.id
                   LEFT OUTER JOIN route AS r ON a.route_id = r.id
             WHERE total.bal > 0),
        aging
        AS (SELECT * FROM aging_invoice
            UNION
            SELECT * FROM aging_delivery)
     SELECT route,
            customer_id AS id,
            cust AS name,
            SUM (total_bal) AS total,
            SUM (current_bal) AS current,
            SUM (t01to07_bal) AS t01to07,
            SUM (t08to15_bal) AS t08to15,
            SUM (t16to30_bal) AS t16to30,
            SUM (t30up_bal) AS t30up
       FROM aging
   GROUP BY route, id, name
   ORDER BY total DESC;

CREATE OR REPLACE VIEW overdue
AS
   WITH overdue_invoice
        AS (SELECT invoice_id AS order_id,
                   ih.series,
                   ih.customer_id,
                   invoice_date AS order_date,
                   invoice_date + CASE WHEN term IS NULL THEN 0 ELSE term END
                      AS due_date,
                     current_date
                   - invoice_date
                   - CASE WHEN term IS NULL THEN 0 ELSE term END
                      AS days_over,
                     CASE WHEN actual IS NULL THEN 0 ELSE actual END
                   - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END
                      AS balance
              FROM invoice_header AS ih
                   LEFT JOIN payment AS p
                      ON ih.invoice_id = p.order_id AND ih.series = p.series
                   LEFT OUTER JOIN credit_detail AS cd
                      ON ih.customer_id = cd.customer_id
             WHERE ih.actual > 0),
        overdue_delivery
        AS (SELECT delivery_id AS order_id,
                   cast (' ' AS text) AS series,
                   dh.customer_id,
                   delivery_date AS order_date,
                     delivery_date
                   + CASE WHEN term IS NULL THEN 0 ELSE term END
                      AS due_date,
                     current_date
                   - delivery_date
                   - CASE WHEN term IS NULL THEN 0 ELSE term END
                      AS days_over,
                     CASE WHEN actual IS NULL THEN 0 ELSE actual END
                   - CASE WHEN p.payment IS NULL THEN 0 ELSE p.payment END
                      AS balance
              FROM delivery_header AS dh
                   LEFT JOIN payment AS p ON dh.delivery_id = -p.order_id
                   LEFT OUTER JOIN credit_detail AS cd
                      ON dh.customer_id = cd.customer_id
             WHERE dh.actual > 0),
        overdue
        AS (SELECT *
              FROM overdue_invoice
             WHERE balance > 1 AND days_over > 0
            UNION
            SELECT *
              FROM overdue_delivery
             WHERE balance > 1 AND days_over > 1)
   SELECT order_id,
          series,
          customer_id,
          order_date,
          due_date,
          days_over,
          balance
     FROM overdue;

CREATE OR REPLACE VIEW item_parent
AS
 WITH RECURSIVE parent_child (child_id, parent_id)
 AS (
  SELECT it.child_id,
      it.parent_id
    FROM item_tree AS it
  UNION ALL
  SELECT parent_child.child_id,
         it.parent_id
    FROM item_tree it
    JOIN parent_child
      ON it.child_id = parent_child.parent_id
  )
  SELECT *
    FROM parent_child;

CREATE OR REPLACE VIEW receiving
AS
   SELECT rh.rr_id,
          rh.rr_date,
          rh.partner_id,
          cm.name,
          rh.ref_id,
          rh.user_id,
          rh.time_stamp,
          rd.item_id,
          rd.qty,
          rd.uom,
          rd.qc_id,
          rd.qty * qp.qty AS pcs,
          qp.qty AS qty_per,
          a.route_id
     FROM receiving_header AS rh
          INNER JOIN receiving_detail AS rd ON rh.rr_id = rd.rr_id
          INNER JOIN customer_master AS cm ON rh.partner_id = cm.id
          INNER JOIN qty_per AS qp
             ON rd.uom = qp.uom AND rd.item_id = qp.item_id
          LEFT OUTER JOIN account AS a ON rh.partner_id = a.customer_id;

CREATE OR REPLACE VIEW inventory
AS
   WITH last_count
        AS (SELECT max (count_date) AS count_date FROM count_closure),
        stock_take
        AS (  SELECT id.item_id, qc_id, sum (id.qty * qp.qty) AS qty
                FROM count_header AS ih
                     INNER JOIN count_detail AS id ON ih.count_id = id.count_id
                     INNER JOIN qty_per AS qp
                        ON id.uom = qp.uom AND id.item_id = qp.item_id
                     INNER JOIN last_count
                        ON ih.count_date = last_count.count_date
                     INNER JOIN item_master AS im
                        ON id.item_id = im.id AND im.type_id <> 2
            GROUP BY id.item_id, qc_id),
        adjustment
        AS (SELECT item_id, qc_id, qty
              FROM count_adjustment
                   INNER JOIN last_count
                      ON count_adjustment.count_date = last_count.count_date),
        adjusted
        AS (SELECT * FROM stock_take
            UNION
            SELECT * FROM adjustment),
        beginning
        AS (  SELECT item_id, qc_id, sum (qty) AS qty
                FROM adjusted
            GROUP BY item_id, qc_id),
        brought_in
        AS (  SELECT id.item_id, qc_id, sum (id.qty * qp.qty) AS qty
                FROM receiving_header AS ih
                     INNER JOIN receiving_detail AS id ON ih.rr_id = id.rr_id
                     INNER JOIN qty_per AS qp
                        ON id.uom = qp.uom AND id.item_id = qp.item_id
                     INNER JOIN last_count
                        ON ih.rr_date BETWEEN last_count.count_date
                                          AND current_date
               WHERE partner_id = 488 OR ref_id < 0 OR qc_id <> 0
            GROUP BY id.item_id, qc_id),
        sold_bundled
        AS (  SELECT bom.part_id AS item_id,
                     sum (id.qty * bom.qty * qp.qty) AS qty
                FROM invoice_header AS ih
                     INNER JOIN invoice_detail AS id
                        ON     ih.invoice_id = id.invoice_id
                           AND ih.series = id.series
                     INNER JOIN last_count
                        ON ih.invoice_date BETWEEN last_count.count_date
                                               AND current_date
                     INNER JOIN bom ON id.item_id = bom.item_id
                     INNER JOIN qty_per AS qp
                        ON bom.uom = qp.uom AND bom.part_id = qp.item_id
            GROUP BY bom.part_id),
        sold_as_is
        AS (  SELECT id.item_id, sum (id.qty * qp.qty) AS qty
                FROM invoice_header AS ih
                     INNER JOIN invoice_detail AS id
                        ON     ih.invoice_id = id.invoice_id
                           AND ih.series = id.series
                     INNER JOIN qty_per AS qp
                        ON id.uom = qp.uom AND id.item_id = qp.item_id
                     INNER JOIN last_count
                        ON ih.invoice_date BETWEEN last_count.count_date
                                               AND current_date
                     INNER JOIN item_master AS im
                        ON id.item_id = im.id AND im.type_id <> 2
            GROUP BY id.item_id),
        sold_combined
        AS (SELECT * FROM sold_bundled
            UNION
            SELECT * FROM sold_as_is),
        sold
        AS (  SELECT item_id, sum (qty) AS qty
                FROM sold_combined
            GROUP BY item_id),
        delivered
        AS (  SELECT id.item_id, sum (id.qty * qp.qty) AS qty
                FROM delivery_header AS ih
                     INNER JOIN delivery_detail AS id
                        ON ih.delivery_id = id.delivery_id
                     INNER JOIN qty_per AS qp
                        ON id.uom = qp.uom AND id.item_id = qp.item_id
                     INNER JOIN last_count
                        ON ih.delivery_date BETWEEN last_count.count_date
                                                AND current_date
            GROUP BY id.item_id),
        sent_out_combined
        AS (SELECT * FROM sold
            UNION
            SELECT * FROM delivered),
        sent_out
        AS (  SELECT item_id, 0 AS qc_id, sum (qty) AS qty
                FROM sent_out_combined
            GROUP BY item_id, qc_id),
        good
        AS (SELECT im.id,
                   0 AS qc_id,
                     CASE
                        WHEN beginning.qty IS NULL THEN 0
                        ELSE beginning.qty
                     END
                   + CASE
                        WHEN brought_in.qty IS NULL THEN 0
                        ELSE brought_in.qty
                     END
                   - CASE
                        WHEN sent_out.qty IS NULL THEN 0
                        ELSE sent_out.qty
                     END
                      AS ending
              FROM item_master AS im
                   LEFT OUTER JOIN beginning
                      ON im.id = beginning.item_id AND beginning.qc_id = 0
                   LEFT OUTER JOIN brought_in
                      ON im.id = brought_in.item_id AND brought_in.qc_id = 0
                   LEFT OUTER JOIN sent_out
                      ON im.id = sent_out.item_id AND sent_out.qc_id = 0
             WHERE    beginning.qty IS NOT NULL
                   OR brought_in.qty IS NOT NULL
                   OR sent_out.qty IS NOT NULL),
        on_hold
        AS (SELECT im.id,
                   1 AS qc_id,
                     CASE
                        WHEN beginning.qty IS NULL THEN 0
                        ELSE beginning.qty
                     END
                   + CASE
                        WHEN brought_in.qty IS NULL THEN 0
                        ELSE brought_in.qty
                     END
                   - CASE
                        WHEN sent_out.qty IS NULL THEN 0
                        ELSE sent_out.qty
                     END
                      AS ending
              FROM item_master AS im
                   LEFT OUTER JOIN beginning
                      ON im.id = beginning.item_id AND beginning.qc_id = 1
                   LEFT OUTER JOIN brought_in
                      ON im.id = brought_in.item_id AND brought_in.qc_id = 1
                   LEFT OUTER JOIN sent_out
                      ON im.id = sent_out.item_id AND sent_out.qc_id = 1
             WHERE    beginning.qty IS NOT NULL
                   OR brought_in.qty IS NOT NULL
                   OR sent_out.qty IS NOT NULL),
        bad
        AS (SELECT im.id,
                   2 AS qc_id,
                     CASE
                        WHEN beginning.qty IS NULL THEN 0
                        ELSE beginning.qty
                     END
                   + CASE
                        WHEN brought_in.qty IS NULL THEN 0
                        ELSE brought_in.qty
                     END
                   - CASE
                        WHEN sent_out.qty IS NULL THEN 0
                        ELSE sent_out.qty
                     END
                      AS ending
              FROM item_master AS im
                   LEFT OUTER JOIN beginning
                      ON im.id = beginning.item_id AND beginning.qc_id = 2
                   LEFT OUTER JOIN brought_in
                      ON im.id = brought_in.item_id AND brought_in.qc_id = 2
                   LEFT OUTER JOIN sent_out
                      ON im.id = sent_out.item_id AND sent_out.qc_id = 2
             WHERE    beginning.qty IS NOT NULL
                   OR brought_in.qty IS NOT NULL
                   OR sent_out.qty IS NOT NULL)
     SELECT row_number ()
            OVER (
               ORDER BY
                  CASE WHEN bad.ending IS NULL THEN 0 ELSE bad.ending END DESC,
                  CASE
                     WHEN on_hold.ending IS NULL THEN 0
                     ELSE on_hold.ending
                  END DESC,
                  CASE WHEN good.ending IS NULL THEN 0 ELSE good.ending END DESC)
               AS line,
            im.id,
            im.name,
            good.ending AS good,
            on_hold.ending AS on_hold,
            bad.ending AS bad
       FROM item_master AS im
            LEFT OUTER JOIN good ON im.id = good.id
            LEFT OUTER JOIN on_hold ON im.id = on_hold.id
            LEFT OUTER JOIN bad ON im.id = bad.id
      WHERE good.ending > 0 OR on_hold.ending > 0 OR bad.ending > 0
   ORDER BY CASE WHEN bad.ending IS NULL THEN 0 ELSE bad.ending END DESC,
            CASE WHEN on_hold.ending IS NULL THEN 0 ELSE on_hold.ending END DESC,
            CASE WHEN good.ending IS NULL THEN 0 ELSE good.ending END DESC;

CREATE OR REPLACE VIEW stt_per_day AS
WITH dates
     AS (SELECT current_date - 30 AS past_start,
                current_date AS past_end,
                current_date - 30 - INTERVAL '1 year' AS forecast_start,
                current_date - INTERVAL '1 year' AS forecast_end),
     sold_bundled
     AS (  SELECT ih.invoice_date AS order_date, bom.part_id AS item_id,
                  sum (id.qty * bom.qty * qp.qty) AS qty
             FROM invoice_header AS ih
                  INNER JOIN invoice_detail AS id
                     ON ih.invoice_id = id.invoice_id AND ih.series = id.series AND ih.actual > 0
                  INNER JOIN dates
                     ON ih.invoice_date BETWEEN dates.past_start
                                            AND past_end
                     OR ih.invoice_date BETWEEN dates.forecast_start
                                            AND forecast_end
                  INNER JOIN bom ON id.item_id = bom.item_id AND bom.is_free IS NOT TRUE
                  INNER JOIN qty_per AS qp
                     ON bom.uom = qp.uom AND bom.part_id = qp.item_id
         GROUP BY ih.invoice_date, bom.part_id),
     sold_as_is
     AS (  SELECT ih.invoice_date AS order_date, id.item_id, sum (id.qty * qp.qty) AS qty
             FROM invoice_header AS ih
                  INNER JOIN invoice_detail AS id
                     ON ih.invoice_id = id.invoice_id AND ih.series = id.series AND ih.actual > 0
                  INNER JOIN qty_per AS qp
                     ON id.uom = qp.uom AND id.item_id = qp.item_id
                  INNER JOIN dates
                     ON ih.invoice_date BETWEEN dates.past_start
                                            AND past_end
                     OR ih.invoice_date BETWEEN dates.forecast_start
                                            AND forecast_end
                  INNER JOIN item_master AS im
                     ON id.item_id = im.id AND im.type_id <> 2
         GROUP BY ih.invoice_date, id.item_id),
     delivered_bundled
     AS (  SELECT ih.delivery_date AS order_date, bom.part_id AS item_id,
                  sum (id.qty * bom.qty * qp.qty) AS qty
             FROM delivery_header AS ih
                  INNER JOIN delivery_detail AS id
                     ON ih.delivery_id = id.delivery_id AND ih.actual > 0
                  INNER JOIN dates
                     ON ih.delivery_date BETWEEN dates.past_start
                                            AND past_end
                     OR ih.delivery_date BETWEEN dates.forecast_start
                                            AND forecast_end
                  INNER JOIN bom ON id.item_id = bom.item_id AND bom.is_free IS NOT TRUE
                  INNER JOIN qty_per AS qp
                     ON bom.uom = qp.uom AND bom.part_id = qp.item_id
         GROUP BY ih.delivery_date, bom.part_id),
     delivered_as_is
     AS (  SELECT ih.delivery_date AS order_date, id.item_id, sum (id.qty * qp.qty) AS qty
             FROM delivery_header AS ih
                  INNER JOIN delivery_detail AS id
                     ON ih.delivery_id = id.delivery_id AND ih.actual > 0
                  INNER JOIN qty_per AS qp
                     ON id.uom = qp.uom AND id.item_id = qp.item_id
                  INNER JOIN dates
                     ON ih.delivery_date BETWEEN dates.past_start
                                            AND past_end
                     OR ih.delivery_date BETWEEN dates.forecast_start
                                            AND forecast_end
                  INNER JOIN item_master AS im
                     ON id.item_id = im.id AND im.type_id <> 2
         GROUP BY ih.delivery_date, id.item_id),
     combined
     AS (SELECT * FROM sold_bundled
         UNION
         SELECT * FROM sold_as_is
         UNION
         SELECT * FROM delivered_bundled
         UNION
         SELECT * FROM delivered_as_is),
     selling_day
     AS (  SELECT count(DISTINCT order_date) AS selling_days
             FROM combined)
SELECT item_id AS id, sum(qty) / selling_days AS qty
             FROM combined, selling_day
         GROUP BY item_id, selling_days
   ORDER BY item_id;

WITH latest_incentive
     AS (  SELECT outlet_id, max (end_date) AS end_date
             FROM target_header AS thr
                  INNER JOIN target_outlet AS tot
                     ON thr.target_id = tot.target_id
            WHERE end_date <= '2013-05-30' AND outlet_id = 1
         GROUP BY outlet_id),
     item_product_line
     AS (SELECT child_id AS item_id, parent_id AS product_line_id
           FROM item_parent AS ipt),
     main_branch
     AS (  SELECT id AS branch,
                  CASE WHEN branch_of IS NULL THEN id ELSE branch_of END
                     AS main
             FROM customer_header
         ORDER BY id)
		 SELECT -row_number () OVER () AS line_id,
         tot.product_line_id,
            rpad (itf.name, 8)
         || ' - '
         || lpad (cast (tot.qty AS text), 7)
         || ' @ P'
         || lpad (cast (tre.value AS text), 5)
         || '/'
         || uom.unit
            AS very_long_description,
         uom.unit,
         sum (idl.qty * unit.qty * report.qty) AS qty,
         CASE
            WHEN sum (idl.qty * unit.qty * report.qty) < tot.qty THEN 0
            ELSE -tre.value
         END
            AS value,
         CASE
            WHEN sum (idl.qty * unit.qty * report.qty) < tot.qty THEN 0
            ELSE -tre.value * sum (idl.qty * unit.qty * report.qty)
         END
            AS rebate
    FROM target_header AS thr
         INNER JOIN target_outlet AS tot ON thr.target_id = tot.target_id
         INNER JOIN item_family AS itf ON tot.product_line_id = itf.id
         INNER JOIN latest_incentive AS lie
            ON tot.outlet_id = lie.outlet_id AND thr.end_date = lie.end_date
         INNER JOIN target_rebate AS tre
            ON     thr.target_id = tre.target_id
               AND tot.product_line_id = tre.product_line_id
         INNER JOIN main_branch AS mbh ON tot.outlet_id = mbh.main
         INNER JOIN invoice_header AS ihr
            ON     ihr.invoice_date BETWEEN thr.start_date AND thr.end_date
               AND ihr.customer_id = mbh.branch
         INNER JOIN invoice_detail AS idl
            ON ihr.invoice_id = idl.invoice_id AND ihr.series = idl.series
         INNER JOIN item_product_line AS ipl
            ON     tot.product_line_id = ipl.product_line_id
               AND idl.item_id = ipl.item_id
         INNER JOIN qty_per AS unit
            ON idl.item_id = unit.item_id AND idl.uom = unit.uom
         INNER JOIN qty_per AS report
            ON idl.item_id = report.item_id AND report.report IS TRUE
         INNER JOIN uom ON uom.id = report.uom
GROUP BY tot.product_line_id,
         itf.name,
         uom.unit,
         tot.qty,
         tre.value

WITH discounts
     AS (SELECT *
           FROM discount
          WHERE family_id = 0),
     updates
     AS (SELECT customer_id,
                -1 AS family_id,
                level_1,
                start_date,
                user_id,
                time_stamp,
                level_2
           FROM discounts
         UNION
         SELECT customer_id,
                -2 AS family_id,
                level_1,
                start_date,
                user_id,
                time_stamp,
                level_2
           FROM discounts)
INSERT INTO discount
   SELECT * FROM updates;

DELETE FROM discount
      WHERE family_id = 0;

WITH parameter
     AS (SELECT cast ('2013-07-06' AS date) AS post_date,
                11 AS cust_id,
                112 AS item_id),
     order_discount
     AS (  SELECT level_1,
                  CASE WHEN level_2 IS NULL THEN 0 ELSE level_2 END AS level_2
             FROM discount AS d
                  INNER JOIN item_parent AS ip ON d.family_id = ip.parent_id
                  INNER JOIN item_master AS im ON ip.child_id = im.id
                  INNER JOIN parameter AS p
                     ON     ip.child_id = p.item_id
                        AND d.customer_id = p.cust_id
                        AND d.start_date <= p.post_date
            WHERE im.not_discounted IS NOT TRUE
         ORDER BY d.family_id,
                  d.start_date DESC
            LIMIT 1)
  SELECT sd.sales_id
    FROM sales_detail AS sd
         INNER JOIN sales_header AS sh
            ON sd.sales_id = sh.sales_id AND sd.line_id = 1
         INNER JOIN item_parent AS ip ON sd.item_id = ip.child_id
         INNER JOIN discount AS d
            ON d.family_id = ip.parent_id AND d.customer_id = sh.customer_id
         INNER JOIN order_discount AS od
            ON     d.level_1 = od.level_1
               AND od.level_2 =
                      CASE WHEN d.level_2 IS NULL THEN 0 ELSE d.level_2 END
         INNER JOIN parameter AS p
            ON p.cust_id = sh.customer_id AND sh.sales_date = p.post_date
ORDER BY sh.sales_date DESC
   LIMIT 1;

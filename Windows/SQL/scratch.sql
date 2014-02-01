WITH 
parameter AS ( 
 SELECT CAST ('2013-08-07' AS DATE) AS start_date, 
        CAST ('2013-08-07' AS DATE) AS end_date, 
        1 AS route_id 
), 
  price_cutoff_date 
AS (SELECT price.item_id AS cutoff_item_id, 
           max(price.start_date) AS cutoff_date 
      FROM price 
INNER JOIN parameter AS pm ON price.start_date <= pm.start_date 
     WHERE tier_id = 1 
  GROUP BY item_id 
), 
latest_price 
AS (SELECT item_id, 
           price 
      FROM price 
	          INNER JOIN price_cutoff_date AS cutoff 
              ON     price.start_date = cutoff_date 
				    AND price.item_id = cutoff_item_id 
     WHERE tier_id = 1 
),


sales_bundled 
AS (SELECT bom.part_id AS item_id, 
           sum (id.qty * bom.qty * qp.qty) AS qty 
      FROM sales_header AS ih 
           INNER JOIN sales_detail AS id 
              ON ih.sales_id = id.sales_id 
INNER JOIN parameter AS pm ON ih.sales_date BETWEEN pm.start_date AND pm.end_date
            INNER JOIN bom ON id.item_id = bom.item_id 
           INNER JOIN qty_per AS qp 
              ON bom.uom = qp.uom AND bom.part_id = qp.item_id 
  GROUP BY bom.part_id), 
sales_as_is 
AS (SELECT IH.CUSTOMER_ID, IH.SALES_ID, IH.SALES_DATE, id.item_id, 
           (id.qty * qp.qty) AS qty 
      FROM sales_header AS ih 
           INNER JOIN sales_detail AS id 
              ON ih.sales_id = id.sales_id 
 INNER JOIN parameter AS pm ON ih.sales_date BETWEEN pm.start_date AND pm.end_date
             INNER JOIN qty_per AS qp 
              ON id.uom = qp.uom AND id.item_id = qp.item_id 
           INNER JOIN item_header AS im 
              ON id.item_id = im.id AND im.type_id <> 2 
-- GROUP BY id.item_id
)
SELECT * FROM SALES_AS_IS
-- , 
--sales_combined 
--AS (SELECT * FROM sales_bundled 
--	 UNION 
--	SELECT * FROM sales_as_is ), salesd
--				AS (SELECT item_id, 
--						   sum (qty) AS qty
--					  FROM sales_combined
--				  GROUP BY item_id)
--				  
--select * from salesd
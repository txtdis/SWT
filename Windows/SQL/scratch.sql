WITH parameter
     AS (SELECT cast ('2013-07-01' AS date) AS start_date,
	 current_date AS end_date,
                1 AS route_id),
invoices as (
select ih.invoice_id
from invoice_header as ih
inner join parameter as p
on ih.invoice_date between p.start_date and p.end_date
inner join account as a
on a.customer_id = ih.customer_id
and a.route_id = a.route_id
left join credit_detail as cd
on ih.customer_id = cd.customer_id
where cd.customer_id is null
) 
select * from invoices
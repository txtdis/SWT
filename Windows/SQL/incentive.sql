with latest_incentive as (
select outlet_id,
max(end_date) as end_date
from target_header as thr 
inner join target_outlet as tot 
on thr.target_id = tot.target_id
where end_date <= '2013-05-30'
and outlet_id = 1
group by outlet_id
),
item_product_line as (
select child_id as item_id,
parent_id as product_line_id
from item_parent as ipt
),
main_branch as (
select id as branch,
case when branch_of is null then id else branch_of end as main
from customer_master
order by id
)
select 
-row_number() over() as line_id,
tot.product_line_id,
rpad(itf.name, 8) || ' - ' || lpad(cast(tot.qty as text) ,7) 
|| ' @ P' || lpad(cast(tre.value as text), 5) || '/' || uom.unit as very_long_description,
uom.unit,
sum(idl.qty * unit.qty * report.qty) as qty,

case when sum(idl.qty * unit.qty * report.qty) < tot.qty then 0 else -tre.value end as value,
case when sum(idl.qty * unit.qty * report.qty) < tot.qty then 0 else -tre.value * sum(idl.qty * unit.qty * report.qty) end 
as rebate


from target_header as thr 
inner join target_outlet as tot 
on thr.target_id = tot.target_id
inner join item_family as itf
on tot.product_line_id = itf.id
inner join latest_incentive as lie
on tot.outlet_id = lie.outlet_id
and thr.end_date = lie.end_date
inner join target_rebate as tre
on thr.target_id = tre.target_id
and tot.product_line_id = tre.product_line_id
inner join main_branch as mbh
on tot.outlet_id = mbh.main 
inner join invoice_header as ihr
on ihr.invoice_date between thr.start_date and thr.end_date
and ihr.customer_id = mbh.branch
inner join invoice_detail as idl
on ihr.invoice_id = idl.invoice_id
and ihr.series = idl.series
inner join item_product_line as ipl
on tot.product_line_id = ipl.product_line_id
and idl.item_id = ipl.item_id
inner join qty_per as unit
on idl.item_id = unit.item_id
and idl.uom = unit.uom
inner join qty_per as report
on idl.item_id = report.item_id
and report.report is true
inner join uom 
on uom.id = report.uom
group by 
tot.product_line_id,
itf.name,
uom.unit,
tot.qty,
tre.value

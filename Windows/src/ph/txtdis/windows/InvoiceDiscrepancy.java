package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class InvoiceDiscrepancy extends Report {

	public InvoiceDiscrepancy(Date[] dates) {
		Calendar cal = Calendar.getInstance();
		if (dates == null) {
			dates = new Date[2];
			dates[0] = new Date(cal.getTimeInMillis());
			dates[1]= new Date(cal.getTimeInMillis());
		}
		Date start = dates[0];
		Date end = dates[1];
		this.dates = dates;

		module = "Invoicing Discrepancies";

		headers = new String[][] {
				{StringUtils.center("DATE", 10), "Date"},
				{StringUtils.center("INVOICE", 7), "ID"},
				{StringUtils.center("SERIES", 6), "String"},
				{StringUtils.center("ID ",7), "ID"},
				{StringUtils.center("CUSTOMER NAME", 32), "String"},
				{StringUtils.center("SYSTEM", 12), "BigDecimal"},
				{StringUtils.center("WRITTEN", 12), "BigDecimal"},
				{StringUtils.center("VARIANCE", 11), "BigDecimal"}
		};
		data = new Data().getDataArray(new Date[] {start, end}, "" +
				"WITH " + 
				"RECURSIVE parent_child (child_id, parent_id) AS ( " + 
				"	SELECT	it.child_id, " +
				"			it.parent_id " +
				"	FROM	item_tree AS it " +
				"	UNION ALL " +
				"	SELECT	parent_child.child_id, " +
				"			it.parent_id " +
				"	FROM 	item_tree it " +
				"	JOIN 	parent_child " +
				"	ON 		it.child_id = parent_child.parent_id " +
				"), " +
				"order_table AS ( " + 
				"	SELECT	h.invoice_id AS order_id, " +
				"			h.series, " +
				"			h.invoice_date AS order_date, " +
				"			h.customer_id, " +
				"			h.actual, " +
				"			CASE WHEN d.item_id < 0 THEN -1 ELSE 1 END " +
				"				* d.item_id AS " +
				"			item_id, " +
				"			d.qty, " +
				"			d.uom, " +
				"			d.qty * qp.qty AS pcs, " +
				"			qp.qty AS qty_per," +
				"				CASE WHEN d.item_id < 0 THEN true ELSE false END AS " +
				"			bo " +
				"	FROM invoice_header AS h " +
				"	INNER JOIN invoice_detail AS d " +
				"		ON 	h.invoice_id = d.invoice_id " +
				"		AND	h.series = d.series " +
				"	INNER JOIN qty_per AS qp " +
				"		ON d.uom = qp.uom " +
				"			AND	CASE WHEN d.item_id < 0 THEN -1 ELSE 1 END " +
				"				* d.item_id = qp.item_id " +
				"	WHERE h.invoice_date BETWEEN ? AND ? " +
				"), " +
				"latest_price_start_date_per_order AS ( " + 
				"	SELECT	ot.order_id, " +
				"			ot.item_id, " +
				"			p.tier_id," +
				"			ot.bo, " +
				"			max(p.start_date) AS max_date " +
				"	FROM order_table AS ot " +
				"	INNER JOIN  price AS p " +
				"	ON ot.item_id = p.item_id " +
				"	INNER JOIN parent_child AS pc " +
				"	ON ot.item_id = pc.child_id " +
				"	INNER JOIN channel_price_tier AS cpt " +
				"	ON p.tier_id = cpt.tier_id " +
				"		AND cpt.family_id = pc.parent_id " +
				"	INNER JOIN customer_master AS cm " +
				"	ON ot.customer_id = cm.id " +
				"		AND cm.type_id = cpt.channel_id " +
				"	WHERE p.start_date <= ot.order_date " +
				"	GROUP BY ot.order_id, " +
				"			ot.item_id, " +
				"			p.tier_id," +
				"			ot.bo " +
				"), prices AS ( " + 
				"	SELECT	pd.order_id, " +
				"			pd.item_id, " +
				"				CASE WHEN pd.bo THEN -1 ELSE 1 END * " +
				"				p.price AS " +
				"			price " +
				"	FROM latest_price_start_date_per_order AS pd " +
				"	INNER JOIN  price AS p " +
				"	ON pd.max_date = p.start_date " +
				"		AND pd.tier_id = p.tier_id " +
				"		And pd.item_id = p.item_id " +
				"), " +
				"latest_volume_discount_start_date_per_order AS ( " + 
				"	SELECT		ot.order_id, " +
				"				ot.item_id, " +
				"				max(d.start_date) AS max_date " +
				"	FROM 		order_table AS ot " +
				"	INNER JOIN 	volume_discount AS d " +
				"	ON 			ot.item_id = d.item_id " +
				"	WHERE 		d.start_date <= ot.order_date " +
				"	GROUP BY 	ot.order_id, " +
				"				ot.item_id " +
				"), " +
				"volume_discounts AS ( " + 
				"	SELECT	dd.order_id, " +
				"			d.item_id, " +
				"			d.uom, " +
				"			d.per_qty, " +
				"			d.less " +
				"	FROM latest_volume_discount_start_date_per_order AS dd " +
				"	INNER JOIN volume_discount AS d " +
				"	ON dd.max_date = d.start_date " +
				"		AND dd.item_id = d.item_id " +
				"), " +
				"latest_discount_start_date_per_order AS ( " + 
				"	SELECT	ot.order_id, " +
				"			ot.customer_id, " +
				"			d.family_id, " +
				"			max(d.start_date) AS max_date " +
				"	FROM 	order_table AS ot, " +
				"			discount AS d, " +
				"			item_master AS im " +
				"	WHERE	d.customer_id = ot.customer_id " +
				"		AND	d.start_date <= ot.order_date " +
				"	GROUP BY ot.order_id, " +
				"			ot.customer_id, " +
				"			d.family_id " +
				"), " +
				"latest_discount_per_family AS ( " + 
				"	SELECT	dd.order_id, " +
				"			dd.family_id, " +
				"			d.level_1 AS rate1, " +
				"			d.level_2 AS rate2 " +
				"	FROM latest_discount_start_date_per_order AS dd " +
				"	INNER JOIN discount AS d " +
				"	ON dd.customer_id = d.customer_id " +
				"		AND dd.max_date = d.start_date " +
				"		AND dd.family_id = d.family_id " +
				"), " +
				"leaf_family_per_customer_discount AS ( " + 
				"	SELECT	ot.order_id, " +
				"			ot.item_id, " +
				"			min(pc.parent_id) AS min_family " +
				"	FROM  order_table AS ot " +
				"	INNER JOIN latest_discount_start_date_per_order AS d " +
				"	ON ot.customer_id = d.customer_id " +
				"	INNER JOIN parent_child AS pc " +
				"	ON ot.item_id = pc.child_id " +
				"		AND d.family_id = pc.parent_id " +
				"	INNER JOIN item_master AS im" +
				"	ON ot.item_id = im.id " +
				"		AND im.not_discounted = FALSE " +
				"	GROUP BY  ot.order_id, " +
				"			ot.item_id " +
				"), " +
				"partner_discounts AS ( " + 
				"	SELECT	ot.order_id, " +
				"			ot.item_id, " +
				"			CASE WHEN rate1 IS null THEN 0 ELSE rate1 END AS rate1, " +
				"			CASE WHEN rate2 IS null THEN 0 ELSE rate2 END AS rate2 " +
				"	FROM latest_discount_per_family AS d " +
				"	INNER JOIN leaf_family_per_customer_discount AS f " +
				"	ON d.order_id = f.order_id " +
				"		AND d.family_id = f.min_family " +
				"	RIGHT OUTER JOIN order_table AS ot " +
				"	ON f.order_id = ot.order_id " +
				"		AND f.item_id = ot.item_id " +
				"), " +
				"t AS ( " +
				"	SELECT	ot.order_date, " +						
				"			ot.order_id, " + 
				"			ot.series, " +
				"			ot.customer_id, " +
				"			ot.actual as act, " +
				"			cm.name, " +
				"			sum	( " +			
				"					p.price * " +
				"					ot.qty_per * " +
				"					ot.qty - " +
				"					CASE " +
				"						WHEN less IS null " +
				"						THEN 0 " +
				"						ELSE less " +
				"					END * " +
				"					ROUND " +
				"					( " +
				"						ot.qty_per * " +
				"						ot.qty / " +
				"						CASE " +
				"							WHEN vd.per_qty IS null " +
				"							THEN 1 " +
				"							ELSE vd.per_qty " +
				"						END, 0 " +
				"					) " +
				"				) - " +							
				"			sum (" +
				"					(" +
				"						p.price * " +
				"						ot.qty_per * " +
				"						ot.qty - " +
				"						CASE WHEN less IS null THEN 0 ELSE less END * " +
				"						ROUND " +
				"						( " +
				"							ot.qty_per * " +
				"							ot.qty / " +
				"							CASE " +
				"								WHEN vd.per_qty IS null " +
				"								THEN 1 " +
				"								ELSE vd.per_qty " +
				"							END, 0 " +
				"						) " +
				"					) * d.rate1/100 " +
				"				) - " +				
				"			sum ( " +
				"					( " +
				"						( " +
				"							p.price * " +
				"							ot.qty_per * " +
				"							ot.qty - " +
				"							CASE " +
				"								WHEN less IS null " +
				"								THEN 0 " +
				"								ELSE less " +
				"							END * " +
				"							ROUND " +
				"							(" +
				"								ot.qty_per * " +
				"								ot.qty / " +
				"								CASE " +
				"									WHEN vd.per_qty IS null " +
				"									THEN 1 " +
				"									ELSE vd.per_qty " +
				"								END, 0 " +
				"							) " +
				"						) " +
				"						- " +
				"						( " +
				"							p.price * " +
				"							ot.qty_per * " +
				"							ot.qty - " +
				"							CASE " +
				"								WHEN less IS null " +
				"								THEN 0 " +
				"								ELSE less " +
				"							END * " +
				"							ROUND " +
				"							( " +
				"								ot.qty_per * " +
				"								ot.qty / " +
				"								CASE " +
				"									WHEN vd.per_qty IS null " +
				"									THEN 1 ELSE vd.per_qty " +
				"								END, 0 " +
				"							) " +
				"						) * d.rate1/100 " +
				"					) * d.rate2/100 " +
				"				) AS total, " +	
				"			ot.actual " +
				"	FROM 	order_table AS ot " +
				"	INNER JOIN customer_master AS cm " +
				"		ON	ot.customer_id = cm.id " +
				"	INNER JOIN prices AS p " +
				"		ON 	ot.item_id = p.item_id " +
				"		AND ot.order_id = p.order_id " +
				"	LEFT OUTER JOIN volume_discounts AS vd " +
				"		ON ot.order_id = vd.order_id " +
				"		AND ot.item_id = vd.item_id " +
				"	LEFT OUTER JOIN partner_discounts AS d " +
				"		ON ot.order_id = d.order_id " +
				"		AND ot.item_id = d.item_id " +
				"	GROUP BY " +
				"		ot.order_id, " +
				"		ot.series, " +
				"		ot.order_date, " +
				"		ot.customer_id, " +
				"		cm.name, " +
				"		ot.actual " +
				") " +
				"SELECT	order_date, " +
				"		order_id, " +
				"		series, " +
				"		customer_id, " +
				"		name, " +
				"		ROUND(total, 2), " +
				"		t.act, " +
				"		t.actual - ROUND(total, 2) " +
				"FROM 	t " +
				"WHERE 	(actual - total) NOT BETWEEN -1 AND 1 " +
				"ORDER BY (actual - total) " +
				""
				);
	}
}
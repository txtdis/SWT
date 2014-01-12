package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class OutletList extends Report {
	private Date[] dates;
	private int productLineId;

	public OutletList(Date[] dates, int routeId, int productLineId, int categoryId){
		Calendar cal = Calendar.getInstance();
		if (dates == null) {
			dates = new Date[2];
			cal.add(Calendar.MONTH, -1);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			dates[0] = new Date(cal.getTimeInMillis());
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			dates[1]= new Date(cal.getTimeInMillis());
		}
		module = "Outlet List";
		this.dates = dates;
		this.routeId = routeId;
		this.productLineId = productLineId;
		this.categoryId = categoryId;
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("ID", 7), "ID"},
				{StringUtils.center("OUTLET", 28), "String"},
				{StringUtils.center("QUANTITY", 9), "BigDecimal"}
		};
		data = new Data().getDataArray(dates, "" +
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
				"order_dates AS (\n" +
				"	SELECT	CAST (? AS date) AS start_date,\n" +
				"			CAST (? AS date) AS end_date\n" +
				"),\n" +
				"invoices AS ( " + 
				"	SELECT	DISTINCT ON(ih.invoice_id, ih.series, id.item_id)\n" +
				"           ih.invoice_id AS order_id, " +
				"			ih.invoice_date AS order_date, " +
				"			ih.series, " +
				"			ih.customer_id, " +
				"			ih.actual, " +
				"			id.item_id, " +
				"			id.qty, " +
				"			id.uom, " +
				"			id.qty * qp.qty AS pcs, " +
				"			qp.qty AS qty_per, " +
				"       	CASE\n" +
				"           	WHEN a.route_id IS NULL THEN 0\n" +
				"           	ELSE last_value( a.route_id)\n" +
				"               	OVER (PARTITION BY ih.invoice_id, ih.series, id.item_id ORDER BY a.start_date DESC)\n" +
				"       	END\n" +
				"           	AS route_id\n" +
			    "FROM invoice_header AS ih " +
			    "   INNER JOIN customer_master AS cm ON ih.customer_id = cm.id\n" +
				"   INNER JOIN channel AS ch ON cm.type_id = ch.id " +
				"	INNER JOIN invoice_detail as id " +
				"		ON ih.invoice_id = id.invoice_id " +
				"		AND ih.series = id.series  " +
				"	INNER JOIN qty_per as qp " +
				"		ON id.uom = qp.uom " +
				"			AND	id.item_id = qp.item_id " +
				"	INNER JOIN order_dates " +
				"       ON ih.invoice_date BETWEEN start_date AND end_date " +
				"	LEFT JOIN account AS a " +
				"		ON     ih.customer_id = a.customer_id " +
				"          AND ih.invoice_date >= a.start_date\n" +
				" WHERE ih.actual >= 0\n" +
				"       AND ch.name <> 'OTHERS' " +
				"), " +
				"deliveries AS ( " + 
				"	SELECT	DISTINCT ON(ih.delivery_id, series, id.item_id)\n" +
				"           ih.delivery_id AS order_id, " +
				"			ih.delivery_date AS order_date, " +
				"			CAST (' ' AS text) AS series, " +
				"			ih.customer_id, " +
				"			ih.actual, " +
				"			id.item_id, " +
				"			id.qty, " +
				"			id.uom, " +
				"			id.qty * qp.qty AS pcs, " +
				"			qp.qty AS qty_per, " +
				"       	CASE\n" +
				"           	WHEN a.route_id IS NULL THEN 0\n" +
				"           	ELSE last_value( a.route_id)\n" +
				"               	OVER (PARTITION BY ih.delivery_id, id.item_id ORDER BY a.start_date DESC)\n" +
				"       	END\n" +
				"           	AS route_id\n" +
				"FROM delivery_header AS ih " +
				"   INNER JOIN customer_master AS cm ON ih.customer_id = cm.id\n" +
				"   INNER JOIN channel AS ch ON cm.type_id = ch.id " +
				"	INNER JOIN delivery_detail as id " +
				"		ON ih.delivery_id = id.delivery_id " +
				"	INNER JOIN order_dates " +
				"       ON ih.delivery_date BETWEEN start_date AND end_date " +
				"	INNER JOIN qty_per as qp " +
				"		ON id.uom = qp.uom " +
				"			AND	id.item_id = qp.item_id " +
				"	LEFT JOIN account AS a " +
				"		ON      ih.customer_id = a.customer_id " +
				"           AND ih.delivery_date >= a.start_date\n" +
				" WHERE ih.actual >= 0\n" +
				"       AND ch.name <> 'OTHERS' " +
				"), " +
				"report_qty AS (" +
				"	SELECT	item_id, " +
				"			qty " +
				"	FROM	qty_per " +
				"	WHERE	report = true " +
				"), " + 
				"sold AS (\n" +
				"		SELECT * FROM invoices\n" +
				"		UNION\n" +
				"		SELECT * FROM deliveries\n" +
				"), " +
				"prod_line AS ( " +
				"	SELECT	" +
				" 			cm.id, " +
				"			cm.name, " +
				"			SUM(i.pcs / rq.qty) AS qty " +
				"	FROM	sold AS i " +
				"	INNER JOIN customer_master AS cm " +
				"		ON	i.customer_id = cm.id " +
				"   INNER JOIN channel as ch " +
				"       ON  cm.type_id = ch.id " +
				"	INNER JOIN	parent_child AS pc " +
				"		ON	i.item_id = pc.child_id " +
				"		AND	pc.parent_id  = " + productLineId + " " +
				"	INNER JOIN report_qty AS rq " +
				"		ON	i.item_id = rq.item_id " +
				"	WHERE i.route_id = " + routeId + " " +
				"       AND ch.name <> 'OTHERS' " +
				"	GROUP BY cm.id, cm.name	" +
				 ") " +
				"SELECT	DISTINCT " +
				"		ROW_NUMBER() OVER (ORDER BY qty DESC), " +
				" 		id, " + 
				" 		name, " + 
				"		qty " + 
				"FROM 	prod_line " +
				"WHERE 	qty <> 0 " +
				"ORDER BY qty DESC");
	}

	public Date[] getDates() {
		return dates;
	}
	
	public int getProductLineId() {
		return productLineId;
	}
}

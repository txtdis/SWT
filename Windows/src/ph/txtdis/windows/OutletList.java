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
				"invoices AS ( " + 
				"	SELECT	ih.invoice_id, " +
				"			ih.invoice_date, " +
				"			ih.series, " +
				"			ih.customer_id, " +
				"			ih.actual, " +
				"			id.item_id, " +
				"			id.qty, " +
				"			id.uom, " +
				"			id.qty * qp.qty AS pcs, " +
				"			qp.qty AS qty_per " +
				"	FROM invoice_header AS ih " +
				"	LEFT OUTER JOIN invoice_detail as id " +
				"		ON ih.invoice_id = id.invoice_id " +
				"		AND ih.series = id.series  " +
				"	INNER JOIN qty_per as qp " +
				"		ON id.uom = qp.uom " +
				"			AND	id.item_id = qp.item_id " +
				"	INNER JOIN account AS a " +
				"		ON ih.customer_id = a.customer_id " +
				"	INNER JOIN route AS r " +
				"		ON a.route_id = r.id " +
				"	WHERE	ih.invoice_date BETWEEN ? AND ? " +
				"		AND r.id = " + routeId + " " +
				"), " +
				"report_qty AS (" +
				"	SELECT	item_id, " +
				"			qty " +
				"	FROM	qty_per " +
				"	WHERE	report = true " +
				"), " + 
				"prod_line AS ( " +
				"	SELECT	" +
				" 			cm.id, " +
				"			cm.name, " +
				"			SUM(i.pcs / rq.qty) AS qty " +
				"	FROM	invoices AS i " +
				"	INNER JOIN customer_master AS cm " +
				"		ON	i.customer_id = cm.id " +
				"	INNER JOIN	parent_child AS pc " +
				"		ON	i.item_id = pc.child_id " +
				"		AND	pc.parent_id  = " + productLineId + " " +
				"	INNER JOIN report_qty AS rq " +
				"		ON	i.item_id = rq.item_id " +
				"	GROUP BY cm.id, cm.name	 " +
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

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin","localhost");
		Object[][] aao = new OutletList(null, 3, 4, -10).getData();
		for (Object[] objects : aao) {
			for (Object object : objects) {
				System.out.print(object + ", ");
			}
			System.out.println();
		}
		Database.getInstance().closeConnection();
	}

}

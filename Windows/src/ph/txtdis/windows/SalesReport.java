package ph.txtdis.windows; 

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class SalesReport extends Report {

	private Date[] dates;
	private int category, routeOrOutlet;
	private String metric;
	private ItemHelper ih;

	public SalesReport(Date[] dates, String metric, int category, int routeOrOutlet){
		this.metric = metric;
		this.category = category;
		this.routeOrOutlet = routeOrOutlet;
		Calendar cal = Calendar.getInstance();
		if (dates == null) {
			dates = new Date[2];
			cal.add(Calendar.MONTH, -1);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			dates[0] = new Date(cal.getTimeInMillis());
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			dates[1]= new Date(cal.getTimeInMillis());
		}
		this.dates = dates;
		module = "Sales Report";
		String[] prodLines = new ItemHelper().getProductLines(category);
		int size = prodLines.length;
		prodLines = ArrayUtils.add(prodLines, 0, "TOTAL");
		
		String strColumn = "";
		String strQty = "";
		String strTbl = "";

		ih = new ItemHelper();
		totals = new Object[size + 4];
		headers = new String[size + 4][];
		headers[0] = new String[]{StringUtils.center("#", 4), "Line"};
		headers[1] = new String[]{StringUtils.center("ID", 4), "ID"};
		headers[2] = new String[]{StringUtils.center("NAME", 29), "String"};
		for (int i = 0; i < size + 1; i++) {
			headers[i+3] = new String[]{
					StringUtils.center(prodLines[i], 7), 
					(metric.equals("SALES TO TRADE") ? "Quantity" : "Long")
			};
			if(metric.equals("SALES TO TRADE")) {
				// Sales to Trade
				if(routeOrOutlet == DIS.ROUTE) {
					// per Route
					strColumn = strColumn + "" +
							"p" + i + " AS ( " +
							"SELECT	r.id, " +
							"		SUM(i.pcs / qp.qty) AS qty " +
							"FROM	invoices AS i  " +
							"INNER JOIN	parent_child AS pc " +
							"	ON	i.item_id = pc.child_id" +
							"	AND	pc.parent_id  = " + ih.getFamilyId(prodLines[i]) + " " +
							"INNER JOIN qty_per AS qp " +
							"	ON	i.item_id = qp.item_id " +
							"	AND qp.report IS true " +
							"inner join account AS a " + 
							"	ON	a.customer_id = i.customer_id "+ 
							"inner JOIN route AS r " +
							"	ON	r.id = a.route_id " +
							"group by r.id " +
							(i == size ? ") " : "), ") +
							"";
				} else {
					// per Outlet
					strColumn = strColumn + "" +
							"p" + i + " AS ( " +
							"SELECT	i.customer_id AS id, " +
							"		SUM(i.pcs / qp.qty) AS qty " +
							"FROM	invoices AS i  " +
							"INNER JOIN	parent_child AS pc " +
							"	ON	i.item_id = pc.child_id" +
							"	AND	pc.parent_id  = " + ih.getFamilyId(prodLines[i]) + " " +
							"INNER JOIN qty_per AS qp " +
							"	ON	i.item_id = qp.item_id " +
							"	AND qp.report IS true " +
							"GROUP BY i.customer_id " +
							(i == size ? ") " : "), ") +
							"";
				}
			} else {
				// Productivity
				strColumn = strColumn + "" +
						"p" + i + " AS ( " +
						"SELECT	" +
						" 		r.id, " +
						"		COUNT(DISTINCT i.customer_id) AS qty " +
						"FROM	invoices AS i  " +
						"INNER JOIN	parent_child AS pc " +
						"	ON	i.item_id = pc.child_id" +
						"	AND	pc.parent_id  = " + ih.getFamilyId(prodLines[i]) + " " +
						"inner join account AS a " + 
						"	ON	a.customer_id = i.customer_id "+ 
						"inner JOIN route AS r " +
						"	ON	r.id = a.route_id " +
						"group by r.id " +
						(i == size ? ") " : "), ") +
						"";
			}
			strQty = strQty + 
					"CASE WHEN " + "p" + i + ".qty IS null " +
					"	THEN 0 " +
					"	ELSE p" + i + ".qty " +
					"END AS p" + i + "_qty " + 
					(i == size ? " " : ", ")
					;
			strTbl = strTbl +
					"LEFT OUTER JOIN p" + i + " " +
					"	ON row.id = p" + i + ".id "
					;
		}
		String row = "";
		if (routeOrOutlet == DIS.ROUTE ) { 
			row = 	"row AS ( " +
					"	SELECT	r.id, " +
					"			r.name " +
					"	FROM 	account AS ac " +
					"	inner JOIN	route AS r " +
					"		ON	r.id = ac.route_id " +
					"), ";			
		} else {
			row = 	"row AS ( " +
					"	SELECT	cm.id, " +
					"			cm.name " +
					"	FROM 	customer_master AS cm " +
					"), ";
		}
		data = new SQL().getDataArray(dates, "" + 
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
				"			ih.customer_id, " +
				"			ih.actual, " +
				"			ih.ref_id, " +
				"			id.item_id, " +
				"			id.qty, " +
				"			id.uom, " +
				"			id.qty * qp.qty AS pcs, " +
				"			qp.qty AS qty_per " +
				"	FROM invoice_header AS ih " +
				"	INNER JOIN invoice_detail as id " +
				"		ON ih.invoice_id = id.invoice_id " +
				"		AND ih.series = id.series " +
				"	INNER JOIN qty_per as qp " +
				"		ON id.uom = qp.uom " +
				"			AND	id.item_id = qp.item_id " +
				"	WHERE	ih.invoice_date BETWEEN ? AND ? " +
				"), " + 
				row +
				strColumn +
				"SELECT	DISTINCT " +
				"		CAST (0 AS smallint), " +
				" 		row.id, " + 
				" 		row.name, " + 
				"" + 	strQty + 
				"FROM 	row " +
				"" +	strTbl + " " +
				"WHERE p0.qty <> 0 " +
				"ORDER BY p0_qty DESC " +
				"");
	}

	public Date[] getDates() {
		return dates;
	}

	public String getMetric() {
		return metric;
	}

	public int getCategoryId() {
		return category;
	}

	public int getRouteOrOutlet() {
		return routeOrOutlet;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		SalesReport r = new SalesReport(null, "SALES TO TRADE", -237, 0);
		for (Object[] os : r.getData()) {
			for (Object o : os) {
				System.out.print(o + ", ");
			}
			System.out.println();
		}
		Database.getInstance().closeConnection();
	}
}

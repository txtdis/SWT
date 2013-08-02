package ph.txtdis.windows; 

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class SalesReport extends Report {

	private Date[] dates;
	private int categoryId, routeOrOutletId;
	private String metric;
	private ItemHelper ih;

	public SalesReport(Date[] dates, String metric, int categoryId, int routeOrOutletId){
		this.metric = metric;
		this.categoryId = categoryId;
		this.routeOrOutletId = routeOrOutletId;
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
		ih = new ItemHelper();
		String[] prodLines = ih.getProductLines(categoryId);
		int arraySize = prodLines.length;
		prodLines = ArrayUtils.add(prodLines, 0, "TOTAL");
		
		String sqlColumn = "";
		String sqlQty = "";
		String sqlTable = "";

		totals = new Object[arraySize + 4];
		headers = new String[arraySize + 4][];
		headers[0] = new String[]{StringUtils.center("#", 4), "Line"};
		headers[1] = new String[]{StringUtils.center("ID", 4), "ID"};
		headers[2] = new String[]{StringUtils.center("NAME", 29), "String"};
		int familyId;
		for (int i = 0; i < arraySize + 1; i++) {
			if(i == 0) {
				familyId = categoryId;
			} else {
				familyId = ih.getFamilyId(prodLines[i]);
			}
			headers[i+3] = new String[]{
					StringUtils.center(prodLines[i], 7), 
					(metric.equals("SALES TO TRADE") ? "Quantity" : "Long")
			};
			if(metric.equals("SALES TO TRADE")) {
				// Sales to Trade
				if(routeOrOutletId == DIS.ROUTE) {
					// per Route
					sqlColumn = sqlColumn + "" +
							"p" + i + " AS ( " +
							"SELECT	r.id, " +
							"		SUM(i.pcs / qp.qty) AS qty " +
							"FROM	invoices AS i  " +
							"INNER JOIN	parent_child AS pc " +
							"	ON	i.item_id = pc.child_id" +
							"	AND	pc.parent_id  = " + familyId + " " +
							"INNER JOIN qty_per AS qp " +
							"	ON	i.item_id = qp.item_id " +
							"	AND qp.report IS true " +
							"INNER JOIN account AS a " + 
							"	ON	a.customer_id = i.customer_id "+ 
							"INNER JOIN route AS r " +
							"	ON	r.id = a.route_id " +
							"GROUP BY r.id " +
							(i == arraySize ? ") " : "), ") +
							"";
				} else {
					// per Outlet
					sqlColumn = sqlColumn + "" +
							"p" + i + " AS ( " +
							"SELECT	i.customer_id AS id, " +
							"		SUM(i.pcs / qp.qty) AS qty " +
							"FROM	invoices AS i  " +
							"INNER JOIN	parent_child AS pc " +
							"	ON	i.item_id = pc.child_id" +
							"	AND	pc.parent_id  = " + familyId + " " +
							"INNER JOIN qty_per AS qp " +
							"	ON	i.item_id = qp.item_id " +
							"	AND qp.report IS true " +
							"GROUP BY i.customer_id " +
							(i == arraySize ? ") " : "), ") +
							"";
				}
			} else {
				// Productivity
				sqlColumn = sqlColumn + "" +
						"p" + i + " AS ( " +
						"SELECT	" +
						" 		r.id, " +
						"		COUNT(DISTINCT i.customer_id) AS qty " +
						"FROM	invoices AS i  " +
						"INNER JOIN	parent_child AS pc " +
						"	ON	i.item_id = pc.child_id" +
						"	AND	pc.parent_id  = " + familyId + " " +
						"inner join account AS a " + 
						"	ON	a.customer_id = i.customer_id "+ 
						"inner JOIN route AS r " +
						"	ON	r.id = a.route_id " +
						"group by r.id " +
						(i == arraySize ? ") " : "), ") +
						"";
			}
			sqlQty = sqlQty + 
					"CASE WHEN " + "p" + i + ".qty IS null " +
					"	THEN 0 " +
					"	ELSE p" + i + ".qty " +
					"END AS p" + i + "_qty " + 
					(i == arraySize ? " " : ", ")
					;
			sqlTable = sqlTable +
					"LEFT OUTER JOIN p" + i + " " +
					"	ON row.id = p" + i + ".id "
					;
		}
		String row = "";
		if (routeOrOutletId == DIS.ROUTE ) { 
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
				sqlColumn +
				"SELECT	DISTINCT " +
				"		CAST (0 AS smallint), " +
				" 		row.id, " + 
				" 		row.name, " + 
				"" + 	sqlQty + 
				"FROM 	row " +
				"" +	sqlTable + " " +
				"WHERE p0.qty <> 0 " +
				"ORDER BY row.id " +
				"");
	}

	public Date[] getDates() {
		return dates;
	}

	public String getMetric() {
		return metric;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public int getRouteOrOutlet() {
		return routeOrOutletId;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		SalesReport r = new SalesReport(null, "SALES TO TRADE", -10, 0);
		for (Object[] os : r.getData()) {
			for (Object o : os) {
				System.out.print(o + ", ");
			}
			System.out.println();
		}
		Database.getInstance().closeConnection();
	}
}

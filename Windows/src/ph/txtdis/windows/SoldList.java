package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class SoldList extends Report {
	private int productLineId;
	private String startDate, endDate;

	public SoldList(Date[] dates) {
		Calendar cal = Calendar.getInstance();
		if (dates == null) {
			dates = new Date[2];
			cal.add(Calendar.MONTH, -1);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			dates[0] = new Date(cal.getTimeInMillis());
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			dates[1]= new Date(cal.getTimeInMillis());
		}
		module = "Invoice/Delivery List";
		this.dates = dates;
		startDate = DIS.POSTGRES_DATE.format(dates[0]);
		endDate = DIS.POSTGRES_DATE.format(dates[1]);
	}

	public SoldList(Date[] dates, int outletId, int productLineId, Integer categoryId){
		this(dates);
		this.categoryId = categoryId;
		this.productLineId = productLineId;
		partnerId = outletId;
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("S/I(D/R)", 8), "ID"},
				{StringUtils.center("SERIES", 6), "String"},
				{StringUtils.center("DATE", 10), "Date"},
				{StringUtils.center("QUANTITY", 9), "Quantity"}
		};

		data = new Data().getDataArray("" +
				"WITH " +
				"invoices AS ( " + 
				"	SELECT	ih.invoice_id, " +
				"			ih.series, " +
				"			ih.invoice_date, " +
				"			ih.customer_id, " +
				"			ih.actual, " +
				"			ih.ref_id, " +
				"			ih.user_id, " +
				"			ih.time_stamp, " +
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
				") " +
				"SELECT	0, " +
				"		i.invoice_id, " +
				"		i.series, " +
				"		i.invoice_date, " +
				"		SUM(i.pcs / qp.qty) AS qty " +
				"FROM	invoices AS i  " +
				"INNER JOIN	item_parent AS ip " +
				"	ON	i.item_id = ip.child_id " +
				"	AND	ip.parent_id  = " + productLineId + " " +
				"INNER JOIN qty_per AS qp " +
				"	ON	i.item_id = qp.item_id " +
				"	AND qp.report = true " +
				"WHERE	i.invoice_date " +
				"			BETWEEN '" + startDate + "' AND '"+ endDate + "' " +
				"		AND i.customer_id = " + outletId + " " +
				"GROUP BY i.invoice_id, " +
				"		i.series, " +
				"		i.invoice_date " +
				"HAVING SUM(i.pcs / qp.qty) <> 0 " +
				"ORDER BY i.invoice_date " +
				"");
	}

	public SoldList(Date[] dates, int itemId){
		this(dates, itemId, null);
	}

	public SoldList(Date[] dates, int itemId, Integer routeId){
		this(dates);
		this.itemId = itemId;
		String routeStmt = "";
		String bundleStmt = "" +
				"	UNION " +
				"	SELECT	* " + 
				"	FROM 	bundled ";
		if(routeId != null) {
			this.routeId = routeId;
			routeStmt = " AND a.route_id = " + routeId + " ";
			bundleStmt = "";
		}
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("S/I(D/R)", 8), "ID"},
				{StringUtils.center("SERIES", 6), "String"},
				{StringUtils.center("OUTLET", 40), "String"},
				{StringUtils.center("DATE", 10), "Date"},
				{StringUtils.center("QUANTITY", 9), "Quantity"}
		};
		data = new Data().getDataArray("" +
				"WITH " +
				"invoices AS ( " + 
				"	SELECT	ih.invoice_id," +
				"			ih.series, " +
				"			ih.invoice_date, " +
				"			ih.customer_id, " +
				"			ih.actual, " +
				"			ih.ref_id, " +
				"			ih.user_id, " +
				"			ih.time_stamp, " +
				"			id.item_id, " +
				"			id.qty, " +
				"			id.uom, " +
				"			id.qty * qp.qty AS pcs, " +
				"			qp.qty AS qty_per " +
				"	FROM invoice_header AS ih " +
				"	INNER JOIN invoice_detail as id " +
				"		ON ih.invoice_id = id.invoice_id " +
				"	   AND ih.series = id.series " +
				"	INNER JOIN qty_per as qp " +
				"		ON id.uom = qp.uom " +
				"			AND	id.item_id = qp.item_id " +
				"), " +
				"invoiced AS ( " +
				"	SELECT	i.invoice_id AS order_id, " +
				"			i.series, " +
				"			cm.name, " + 
				"			i.invoice_date AS order_date, " + 
				"			i.pcs AS qty " + 
				"	FROM 	invoices AS i " +
				"	INNER JOIN customer_master AS cm " +
				"		ON i.customer_id = cm.id " +
				"	LEFT OUTER JOIN account AS a " +
				"		ON i.customer_id = a.customer_id " +
				"	WHERE	i.invoice_date " +
				"				BETWEEN '" + startDate + "' AND '"+ endDate + "' " +
				"		AND i.item_id = " + itemId + " " +
				routeStmt +
				"		AND	qty <> 0 " +
				"), " +
				"bundled AS ( " +
				"	SELECT	i.invoice_id AS order_id, " +
				"			i.series, " +
				"			cm.name, " + 
				"			i.invoice_date AS order_date, " + 
				"			i.pcs * bom.qty AS qty " + 
				"	FROM 	invoices AS i " +
				"	INNER JOIN bom " +
				"		ON i.item_id = bom.item_id " +
				"	INNER JOIN customer_master AS cm " +
				"		ON i.customer_id = cm.id " +
				"	LEFT OUTER JOIN account AS a " +
				"		ON i.customer_id = a.customer_id " +
				"	WHERE	i.invoice_date " +
				"				BETWEEN '" + startDate + "' AND '"+ endDate + "' " +
				"		AND bom.part_id = " + itemId + " " +
				routeStmt +
				"		AND	i.pcs * bom.qty <> 0 " +
				"), " +
				"deliveries AS ( " + 
				"	SELECT	dh.delivery_id, " +
				"			CAST('DR' AS TEXT) AS series, " +
				"			dh.delivery_date, " +
				"			dh.customer_id, " +
				"			dh.actual, " +
				"			dh.ref_id, " +
				"			dh.user_id, " +
				"			dh.time_stamp, " +
				"			dd.item_id, " +
				"			dd.qty, " +
				"			dd.uom, " +
				"			dd.qty * qp.qty AS pcs, " +
				"			qp.qty AS qty_per " +
				"	FROM delivery_header AS dh " +
				"	INNER JOIN delivery_detail AS dd " +
				"		ON dh.delivery_id = dd.delivery_id " +
				"	INNER JOIN qty_per as qp " +
				"		ON dd.uom = qp.uom " +
				"			AND	dd.item_id = qp.item_id " +
				"), " +
				"delivered AS ( " +
				"	SELECT	DISTINCT " +
				" 			-d.delivery_id AS order_id, " +
				"			d.series, " +
				"			cm.name, " + 
				"			d.delivery_date AS order_date, " + 
				"			d.pcs AS qty " + 
				"	FROM 	deliveries AS d " +
				"	INNER JOIN customer_master AS cm " +
				"		ON d.customer_id = cm.id " +
				"	LEFT OUTER JOIN account AS a " +
				"		ON d.customer_id = a.customer_id " +
				"	WHERE	d.delivery_date " +
				"				BETWEEN '" + startDate + "' AND '"+ endDate + "' " +
				"		AND d.item_id = " + itemId + " " +
				routeStmt +
				"		AND	qty <> 0 " +
				"), " +
				"sold AS ( " +
				"	SELECT	* " + 
				"	FROM 	delivered " +
				"	UNION " +
				"	SELECT	* " + 
				"	FROM 	invoiced " +
				bundleStmt +
				") " +
				"SELECT	0, " +
				"		order_id, " +
				"		series, " +
				"		name, " +
				"		order_date, " + 
				"		qty " + 
				"FROM 	sold " +
				"ORDER BY order_id " +
				"");
	}

	public int getProductLineId() {
		return productLineId;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin","localhost");
		Date[] dates = new Date[2];
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.MAY, 4);
		dates[0] = new Date(cal.getTimeInMillis());
		cal.set(2013, Calendar.MAY, 11);
		dates[1]= new Date(cal.getTimeInMillis());
		Object[][] aao = new SoldList(dates, 109).getData();
		for (Object[] objects : aao) {
			for (Object object : objects) {
				System.out.print(object + ", ");
			}
			System.out.println();
		}
		Database.getInstance().closeConnection();
	}

}

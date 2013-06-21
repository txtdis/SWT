package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class RouteReport extends Report {
	private Date[] dates;
	private int routeId;

	public RouteReport(Date[] dates, int routeId) {
		super();
		module = "Route Report";
		Calendar cal = Calendar.getInstance();
		if (dates == null) {
			dates = new Date[2];
			dates[0] = new Date(cal.getTimeInMillis());
			dates[1]= new Date(cal.getTimeInMillis());
		}
		this.dates = dates;
		this.routeId = routeId;
		headers = new String[][] {
				{StringUtils.center("#", 2), "Line"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("PRODUCT NAME", 40), "String"},
				{StringUtils.center("LOADED", 8), "Quantity"},
				{StringUtils.center("SOLD", 8), "Quantity"},
				{StringUtils.center("RETURNED", 8), "Quantity"},
				{StringUtils.center("VARIANCE", 8), "Quantity"}				
		};
		String startDate = DIS.DF.format(dates[0]);
		String endDate = DIS.DF.format(dates[1]);
		data = new SQL().getDataArray("" +
				"WITH " +
				"beginning AS ( " +
				"	SELECT	sd.item_id, " +
				"			sum(sd.qty * qp.qty) AS qty " +
				"	FROM 	sales_header AS sh " +
				"	INNER JOIN sales_detail AS sd " +
				"		ON 	sd.sales_id = sh.sales_id " +
				"		AND	sh.sales_date BETWEEN '" + startDate + "' AND '" + endDate + "' " +
				"	INNER JOIN account AS a " +
				"		ON sh.customer_id = a.customer_id " +
				"		AND a.route_id = " + routeId +
				"	INNER JOIN qty_per AS qp " + 
				"		ON 	sd.uom = qp.uom " +
				"		AND	qp.item_id = sd.item_id " +
				"	INNER JOIN sales_print_out AS pso " + 
				"		ON 	sd.sales_id = pso.sales_id " +
				"	GROUP BY sd.item_id " +
				"), " +
				"invoiced AS ( " +
				"	SELECT	id.item_id, " +
				"			sum(id.qty * qp.qty) AS qty " +
				"	FROM 	invoice_header AS ih " +
				"	INNER JOIN invoice_detail AS id " +
				"		ON 	id.invoice_id = ih.invoice_id " +
				"		AND ih.invoice_id > 0 " +
				"		AND id.item_id > 0 " +
				"		AND	ih.invoice_date BETWEEN '" + startDate + "' AND '" + endDate + "' " +
				"	INNER JOIN account AS a " +
				"		ON ih.customer_id = a.customer_id " +
				"		AND a.route_id = " + routeId +
				"	INNER JOIN qty_per AS qp " + 
				"		ON 	id.uom = qp.uom " +
				"		AND	qp.item_id = id.item_id " +
				"	GROUP BY id.item_id " +
				"), " +
				"delivered AS ( " +
				"	SELECT	dd.item_id, " +
				"			sum(dd.qty * qp.qty) AS qty " +
				"	FROM 	delivery_header AS dh " +
				"	INNER JOIN delivery_detail AS dd " +
				"		ON 	dd.delivery_id = dh.delivery_id " +
				"		AND dh.delivery_id > 0 " +
				"		AND dd.item_id > 0 " +
				"		AND	dh.delivery_date BETWEEN '" + startDate + 
								"' AND '" + endDate + "' " +
				"	INNER JOIN account AS a " +
				"		ON dh.customer_id = a.customer_id " +
				"		AND a.route_id = " + routeId +
				"	INNER JOIN qty_per AS qp " + 
				"		ON 	dd.uom = qp.uom " +
				"		AND	qp.item_id = dd.item_id " +
				"	GROUP BY dd.item_id " +
				"), " +
				"joined_sold AS ( " +
				"	SELECT	item_id, " +
				"			qty " +
				"	FROM 	delivered " +
				"	UNION " +	
				"	SELECT	item_id, " +
				"			qty " +
				"	FROM 	invoiced " +
				"), " +
				"sold AS ( " +
				"	SELECT	item_id, " +
				"			sum(qty) AS qty " +
				"	FROM 	joined_sold " +
				"	GROUP BY item_id " +
				"), " +
				"ending AS ( " +
				"	SELECT	rd.item_id, " +
				"			sum(rd.qty * qp.qty) AS qty " +
				"	FROM 	receiving_detail AS rd " +
				"	INNER JOIN receiving_header AS rh " +
				"		ON 	rd.rr_id = rh.rr_id " +
				"		AND	rh.rr_date BETWEEN '" + startDate + "' AND '" + endDate + "' " +
				"		AND rd.qc_id = 0 "+
				"	INNER JOIN qty_per AS qp " +
				"		ON	rd.uom = qp.uom " +
				"		AND	qp.item_id = rd.item_id " +
				"	INNER JOIN customer_master AS cm" +
				"		ON	cm.id = rh.partner_id " +
				"	INNER JOIN account AS a " +
				"		ON rh.partner_id = a.customer_id " +
				"		AND a.route_id = " + routeId +
				"	GROUP BY rd.item_id " +
				"), " +
				"combined AS ( " +
				"	SELECT	im.id, " +
				"			im.name, " +
				"			CASE WHEN beginning.qty IS NULL " +
				"				THEN 0 ELSE beginning.qty END AS beginning_qty, " +
				"			CASE WHEN sold.qty IS NULL " +
				"				THEN 0 ELSE sold.qty END AS sold_qty, " +
				"			CASE WHEN ending.qty IS NULL " +
				"				THEN 0 ELSE ending.qty END AS ending_qty " +
				"	FROM	item_master AS im " +
				"	LEFT OUTER JOIN beginning " +
				"		ON 	im.id = beginning.item_id " +
				"	LEFT OUTER JOIN sold " +
				"		ON 	im.id = sold.item_id " +
				"	LEFT OUTER JOIN ending " +
				"		ON	im.id = ending.item_id " +
				"), computed AS (" +
				"	SELECT	id, " +
				"			name, " +
				"			beginning_qty, " +
				"			sold_qty, " +
				"			ending_qty, " +
				"			sold_qty + ending_qty - beginning_qty AS variance " +
				"	FROM	combined " +
				"	WHERE	sold_qty + ending_qty + beginning_qty > 0 " +
				") " +
				"SELECT	row_number() OVER(ORDER BY variance), " +
				"		id, " +
				"		name, " +
				"		beginning_qty, " +
				"		sold_qty, " +
				"		ending_qty," +
				"		variance " +
				"FROM	computed " +
				"WHERE	variance <> 0 " +
				"ORDER BY variance " +
				"" );
	}

	public Date[] getDates() {
		return dates;
	}

	public int getRouteId() {
		return routeId;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Calendar cal = Calendar.getInstance();
		cal.set(2013, 1, 28);
		Date first = new Date(cal.getTimeInMillis());
		Date last = first;
		RouteReport rr = new RouteReport(new Date[] {first, last}, 1);
		for (Object[] os : rr.getData()) {
			for (Object o : os) {
				System.out.print(o + ", ");
			}
			System.out.println();
		}
		Database.getInstance().closeConnection();
	}

}

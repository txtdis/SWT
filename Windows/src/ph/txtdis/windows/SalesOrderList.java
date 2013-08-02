package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class SalesOrderList extends Report {
	private Date[] dates;
	private int itemId, routeId;

	public SalesOrderList(Date[] dates, int itemId, int routeId){
		Calendar cal = Calendar.getInstance();
		if (dates == null) {
			dates = new Date[2];
			cal.set(Calendar.DAY_OF_MONTH, 1);
			dates[0] = new Date(cal.getTimeInMillis());
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			dates[1]= new Date(cal.getTimeInMillis());
		}
		module = "Sales Order List";
		this.dates = dates;
		this.itemId = itemId;
		this.routeId = routeId;
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("S/O", 7), "ID"},
				{StringUtils.center("CUSTOMER", 28), "String"},
				{StringUtils.center("QUANTITY", 9), "BigDecimal"}
		};
		data = new Data().getDataArray(dates, "" +
				"WITH " +
				"sos AS ( " + 
				"	SELECT	DISTINCT " +
				"			sh.sales_id, " +
				"			sh.sales_date, " +
				"			sh.customer_id, " +
				"			sh.user_id, " +
				"			sh.time_stamp, " +
				"			sd.item_id, " +
				"			sd.qty, " +
				"			sd.uom, " +
				"			sd.qty * qp.qty AS pcs, " +
				"			qp.qty AS qty_per " +
				"	FROM sales_header AS sh " +
				"	INNER JOIN sales_detail as sd " +
				"		ON sh.sales_id = sd.sales_id " +
				"	INNER JOIN qty_per as qp " +
				"		ON sd.uom = qp.uom " +
				"			AND sd.item_id = qp.item_id " + 
				"	LEFT OUTER JOIN account AS a " +
				"		ON sh.customer_id = a.customer_id " +
				"		WHERE	sh.sales_date BETWEEN ? AND ? " +
				"		AND sd.item_id = " + itemId + " " +
				"		AND a.route_id = " + routeId + " " +
				") " +
				"SELECT	DISTINCT " +
				"		ROW_NUMBER() OVER (ORDER BY sales_id), " +
				" 		sos.sales_id, " +
				"		cm.name, " + 
				"		sos.pcs AS qty " + 
				"FROM 	sos " +
				"INNER JOIN customer_master AS cm " +
				"	ON sos.customer_id = cm.id " +
				"WHERE 	qty <> 0 " +
				"ORDER BY sos.sales_id " +
				"");
	}

	public Date[] getDates() {
		return dates;
	}
	
	public int getItemId() {
		return itemId;
	}

	public int getRouteId() {
		return routeId;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Object[][] aao = new SalesOrderList(null, 1, 7).getData();
		for (Object[] objects : aao) {
			for (Object object : objects) {
				System.out.print(object + ", ");
			}
			System.out.println();
		}
		Database.getInstance().closeConnection();
	}
}

package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class ReceivingList extends Report {
	private Date[] dates;
	private int itemId;
	private Integer routeId;

	public ReceivingList(Date[] dates, int itemId, Integer routeId) {
		module = "Receiving Report List";
		this.dates = dates;
		this.itemId = itemId;
		this.routeId = routeId;
		headers = new String[][] {
		        {
		                StringUtils.center("#", 3), "Line" }, {
		                StringUtils.center("R/R", 7), "ID" }, {
		                StringUtils.center("CUSTOMER", 28), "String" }, {
		                StringUtils.center("QUANTITY", 9), "Quantity" } };
		String routeStmt;
		if (routeId == null)
			routeStmt = "(partner_id = 488 OR ref_id < 0 or qc_id = 2)";
		else
			routeStmt = "route_id = " + routeId;
		data = new Data().getDataArray(dates, "" 
			+ "SELECT ROW_NUMBER() OVER (ORDER BY rh.receiving_id), "
			+ " 	  rh.receiving_id, " 
			+ "		  cm.name, " 
			+ "		  rd.qty * qp.qty AS qty " 
			+ "  FROM receiving_header as rh "
			+ "       INNER JOIN receiving_detail as rd "
			+ "		     ON rh.receiving_id = rd.receiving_id " 
			+ "       INNER JOIN account as a "
			+ "		     ON rh.partner_id = a.customer_id " 
			+ "       INNER JOIN customer_master as cm "
			+ "		     ON rh.partner_id = cm.id " 
			+ "       INNER JOIN qty_per as qp "
			+ "		     ON     qp.item_id = rd.item_id "
			+ "				AND qp.uom = rd.uom " 
		    + " WHERE     receiving_date BETWEEN ? AND ? " 
			+ "	      AND rd.item_id = " + itemId  
			+ "       AND " + routeStmt
		    + "ORDER BY rh.receiving_id " 
			);
	}

	public Date[] getDates() {
		return dates;
	}

	public int getItemId() {
		return itemId;
	}

	public Integer getRouteId() {
		return routeId;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin","localhost");
		Date[] dates = new Date[2];
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.MAY, 4);
		dates[0] = new Date(cal.getTimeInMillis());
		cal.set(2013, Calendar.MAY, 11);
		dates[1] = new Date(cal.getTimeInMillis());
		Object[][] aao = new ReceivingList(dates, 248, null).getData();
		if (aao != null)
			for (Object[] objects : aao) {
				for (Object object : objects) {
					System.out.print(object + ", ");
				}
				System.out.println();
			}
		else
			System.err.println("No Data");
		Database.getInstance().closeConnection();
	}

}

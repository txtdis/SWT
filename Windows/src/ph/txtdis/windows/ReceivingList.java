package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class ReceivingList extends Report {
	private Date[] dates;
	private int itemId;
	private Integer routeId;

	public ReceivingList(Date[] dates, int itemId, Integer routeId){
		module = "Receiving Report List";
		this.dates = dates;
		this.itemId = itemId;
		this.routeId = routeId;
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("R/R", 7), "ID"},
				{StringUtils.center("CUSTOMER", 28), "String"},
				{StringUtils.center("QUANTITY", 9), "Quantity"}
		};
		String routeStmt;
		if (routeId == null) 
			routeStmt = " AND (partner_id = 488 OR ref_id < 0 or qc_id = 2)";
		else
			routeStmt = " AND route_id = " + routeId;
		data = new SQL().getDataArray(dates, "" +
				"SELECT	ROW_NUMBER() OVER (ORDER BY rr_id), " +
				" 		rr_id, " +
				"		name, " + 
				"		pcs " + 
				"FROM 	receiving " +
				"WHERE rr_date BETWEEN ? AND ? " +
				"	AND item_id = " + itemId + " " +
				routeStmt +
				"ORDER BY rr_id " +
				""
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
		Database.getInstance().getConnection("irene","ayin");
		Date[] dates = new Date[2];
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.MAY, 4);
		dates[0] = new Date(cal.getTimeInMillis());
		cal.set(2013, Calendar.MAY, 11);
		dates[1]= new Date(cal.getTimeInMillis());
		Object[][] aao = new ReceivingList(dates, 248, null).getData();
		if(aao != null)
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

package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class StockTakeList extends Report {

	public StockTakeList(Date date, int itemId){
		this(date, itemId, null);
	}

	public StockTakeList(Date date, int itemId, Integer locationId){
		module = "Stock Take Tag List";
		this.date = date;
		this.itemId = itemId;
		String locationStmt = "";
		if (locationId != null) {
			locationStmt = "AND ch.location_id = " + locationId + " ";
		}
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("TAG", 5), "ID"},
				{StringUtils.center("LOCATION", 14), "String"},
				{StringUtils.center("UOM", 3), "String"},
				{StringUtils.center("QUANTITY", 8), "Quantity"}
		};
		data = new Data().getDataArray(new Object[] {date, itemId}, "" +
				"SELECT	ROW_NUMBER() OVER (ORDER BY cd.count_id) AS line, " +
				" 		cd.count_id, " +
				"		loc.name, " +
				"		cast ('PK' AS text) AS unit, " +
				"		sum (CASE WHEN cd.qty IS NULL THEN 0 ELSE cd.qty END "
				+ "		  * qp.qty) as pcs " + 
				"  FROM	count_header AS ch " +
				" INNER JOIN count_detail AS cd " +
				"    ON ch.count_id = cd.count_id " +
				" INNER JOIN location AS loc " +
				"    ON ch.location_id = loc.id " +
				" INNER JOIN qty_per AS qp " +
				"	 ON cd.uom = qp.uom " +
				"	AND cd.item_id = qp.item_id " +
				" WHERE ch.count_date = ? " +
				"	AND cd.item_id = ? " +
				locationStmt +
				"GROUP BY cd.count_id, name, unit " +
				"ORDER BY cd.count_id ");
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin","localhost");
		Date date;
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.AUGUST, 2);
		date = new Date(cal.getTimeInMillis());
		Object[][] aao = new StockTakeList(date, 102, null).getData();
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

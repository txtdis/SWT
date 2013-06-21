package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class StockTakeList extends Report {
	private Date date;
	private int itemId;

	public StockTakeList(Date date, int itemId){
		module = "Stock Take Tag List";
		this.date = date;
		this.itemId = itemId;
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("TAG", 5), "ID"},
				{StringUtils.center("LOCATION", 14), "String"},
				{StringUtils.center("UOM", 3), "String"},
				{StringUtils.center("QUANTITY", 8), "Quantity"}
		};
		data = new SQL().getDataArray(new Object[] {date, itemId}, "" +
				"SELECT	ROW_NUMBER() OVER (ORDER BY cd.count_id) AS line, " +
				" 		cd.count_id, " +
				"		loc.name, " +
				"		'PK' AS unit, " +
				"		SUM(cd.qty * qp.qty) as pcs " + 
				"  FROM	count_header AS ch " +
				"INNER JOIN count_detail AS cd " +
				"    ON ch.count_id = cd.count_id " +
				"INNER JOIN location AS loc " +
				"    ON ch.location_id = loc.id " +
				"INNER JOIN qty_per AS qp " +
				"	 ON cd.uom = qp.uom " +
				"	AND cd.item_id = qp.item_id " +
				"WHERE ch.count_date = ? " +
				"	AND cd.item_id = ? " +
				"GROUP BY cd.count_id, name, unit " +
				"ORDER BY cd.count_id " +
				""
				);
	}

	public Date getDate() {
		return date;
	}

	public int getItemId() {
		return itemId;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Date date;
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.MAY, 4);
		date = new Date(cal.getTimeInMillis());
		Object[][] aao = new StockTakeList(date, 248).getData();
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

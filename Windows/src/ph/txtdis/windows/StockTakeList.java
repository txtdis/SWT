package ph.txtdis.windows;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class StockTakeList extends Report {

	public StockTakeList(Date date, int itemId, Integer locationId, Integer qcId){
		module = "Stock Take Tag List";
		this.date = date;
		this.itemId = itemId;
		String locationStmt = locationId == null ? "" : "AND ch.location_id = " + locationId + "\n";
		String qcStmt = qcId == null ? "" : "AND cd.qc_id = " + qcId + "\n";
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
				"		location.name, " +
				"		cast ('PK' AS text) AS unit, " +
				"		sum (CASE WHEN cd.qty IS NULL THEN 0 ELSE cd.qty END * qp.qty) AS pcs " + 
				"  FROM	count_header AS ch " +
				" INNER JOIN count_detail AS cd " +
				"    ON ch.count_id = cd.count_id " +
				" INNER JOIN location " +
				"    ON ch.location_id = location.id " +
				" INNER JOIN qty_per AS qp " +
				"	 ON cd.uom = qp.uom " +
				"	AND cd.item_id = qp.item_id " +
				" WHERE ch.count_date = ? " +
				"	AND cd.item_id = ? " +
				locationStmt + qcStmt +
				"GROUP BY cd.count_id, name, unit " +
				"ORDER BY cd.count_id ");
	}
}

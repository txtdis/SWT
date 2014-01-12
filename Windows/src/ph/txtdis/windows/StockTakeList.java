package ph.txtdis.windows;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class StockTakeList extends Report {

	public StockTakeList(Date[] dates, int itemId, Integer locationId, Integer qcId) {
		module = "Stock Take Tag List";
		this.dates = dates;
		this.itemId = itemId;
		String locationStmt = locationId == null ? "" : "AND ch.location_id = " + locationId + "\n";
		String qcStmt = qcId == null ? "" : "AND cd.qc_id = " + qcId + "\n";
		
		headers = new String[][] { 
				//@sql:on
				{ StringUtils.center("#", 3), "Line" }, 
				{ StringUtils.center("TAG", 5), "ID" },
		        { StringUtils.center("DATE", 10), "Date" }, 
		        { StringUtils.center("LOCATION", 14), "String" }, 
		        { StringUtils.center("UOM", 3), "String" },
		        { StringUtils.center("QUANTITY", 8), "Quantity" } };
		
		data = new Data().getDataArray(new Object[] { dates[0], dates[1], itemId }, ""
		        + "SELECT row_number () OVER (ORDER BY ch.count_id) AS line, " 
				+ " 	  ch.count_id, "
		        + "		  ch.count_date, " 
		        + "		  location.name, " 
				+ "		  cast ('PK' AS text) AS unit, "
		        + "		  sum (CASE WHEN cd.qty IS NULL THEN 0 ELSE cd.qty END * qp.qty) AS pcs "
		        + "  FROM count_header AS ch " 
		        + "       INNER JOIN count_detail AS cd "
		        + "          ON ch.count_id = cd.count_id " 
		        + "       INNER JOIN location "
		        + "          ON ch.location_id = location.id " 
		        + "       INNER JOIN qty_per AS qp " 
		        + "	         ON     cd.uom = qp.uom "
		        + "	            AND cd.item_id = qp.item_id " 
		        + " WHERE     ch.count_date BETWEEN ? AND ?\n" 
		        + "	      AND cd.item_id = ? " 
		        + locationStmt
		        + qcStmt 
		        + "GROUP BY ch.count_id, ch.count_date, name, unit, location.name " 
		        + "ORDER BY ch.count_id "
		        //@sql:off
		        );
	}
}

package ph.txtdis.windows;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class CountList extends Data implements Subheaded {
	private int itemId;

	public CountList(Date date, int itemId, Integer locationId, Integer qcId) {
		type = Type.COUNT_LIST;
		this.date = date;
		this.itemId = itemId;
		String locationStmt = locationId == null ? "" : "AND ch.location_id = " + locationId + "\n";
		String qcStmt = qcId == null ? "" : "AND cd.qc_id = " + qcId + "\n";
		
		tableHeaders = new String[][] { 
				//@sql:on
				{ StringUtils.center("#", 3), "Line" }, 
				{ StringUtils.center("TAG", 5), "ID" },
		        { StringUtils.center("LOCATION", 14), "String" }, 
		        { StringUtils.center("QUALITY", 7), "String" }, 
		        { StringUtils.center("UOM", 3), "String" },
		        { StringUtils.center("QUANTITY", 8), "Quantity" } };
		
		tableData = new Query().getTableData(new Object[] { date, itemId }, ""
		        + "SELECT row_number () OVER (ORDER BY ch.count_id) AS line_id, " 
				+ " 	  ch.count_id, "
		        + "		  loc.name, " 
		        + "		  qc.name, " 
				+ "		  cast ('PK' AS text) AS unit, "
		        + "		  sum (CASE WHEN cd.qty IS NULL THEN 0 ELSE cd.qty END * qp.qty) AS pcs "
		        + "  FROM count_header AS ch " 
		        + "       INNER JOIN count_detail AS cd ON ch.count_id = cd.count_id " 
		        + "       INNER JOIN quality AS qc ON cd.qc_id = qc.id " 
		        + "       INNER JOIN location AS loc ON ch.location_id = loc.id " 
		        + "       INNER JOIN qty_per AS qp ON cd.uom = qp.uom AND cd.item_id = qp.item_id " 
		        + " WHERE ch.count_date = ? AND cd.item_id = ? " 
		        + locationStmt
		        + qcStmt 
		        + "GROUP BY ch.count_id, ch.count_date, loc.name, qc.name, unit " 
		        + "ORDER BY ch.count_id "
		        //@sql:off
		        );
	}

	@Override
    public String getSubheading() {
	    return Item.getName(itemId) + "\ncounted on " + date;
    }
}

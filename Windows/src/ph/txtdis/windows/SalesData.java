package ph.txtdis.windows;

import java.math.BigDecimal;

public class SalesData extends OrderData {
	private BigDecimal salesLimit;
	private Object[][] loadData;

	public SalesData(int orderId) {
		super(orderId);
		salesLimit = BigDecimal.ZERO;
		
		loadData = sql.getTableData(orderId, ""
				// @sql:on
				+ "WITH expanded AS\n" 
				+ "		 (SELECT sd.line_id,\n" 
				+ "				 CASE WHEN bom.item_id IS NULL THEN sd.item_id ELSE bom.part_id END AS item_id,\n" 
				+ "				 sd.uom,\n" 
				+ "				 CASE WHEN bom.qty IS NULL THEN sd.qty ELSE bom.qty * sd.qty END AS qty\n" 
				+ "			FROM sales_detail AS sd\n" 
				+ "				 INNER JOIN qty_per AS qp ON qp.item_id = sd.item_id AND qp.uom = sd.uom\n" 
				+ "				 LEFT JOIN bom ON sd.item_id = bom.item_id\n" 
				+ "		   WHERE sales_id = ?)\n" 
				+ "  SELECT e.line_id,\n" 
				+ "		 e.item_id,\n" 
				+ "		 unit,\n" 
				+ "		 e.qty\n" 
				+ "	FROM expanded AS e INNER JOIN uom ON uom.id = e.uom\n" 
				+ "ORDER BY 1, 2\n" 
				// @sql:off
				);
	}

	@Override
    protected void setProperties() {
		type = Type.SALES;
		referenceAndActualStmt ="" 
				// @sql:on
				+ " CAST(0 AS NUMERIC(10,2)) AS actual, " 
				+ " CAST(0 AS INT) AS ref_id, "
		        + " CAST(0 AS NUMERIC(10,2)) AS payment, "
				// @sql:off
		;
    }

	public Object[][] getLoadData() {
		return loadData;
	}

	public BigDecimal getSalesLimit() {
		return salesLimit;
	}

	public void setSalesLimit(BigDecimal salesLimit) {
		this.salesLimit = salesLimit;
	}
}

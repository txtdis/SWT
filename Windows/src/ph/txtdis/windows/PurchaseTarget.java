package ph.txtdis.windows;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class PurchaseTarget extends OrderData {
	
	public PurchaseTarget(Date date) {
		date = (date == null) ? DIS.getFirstOfMonth(DIS.TODAY) : date;
		type = Type.PURCHASE_TARGET;
		tableHeaders = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("PRODUCT LINE", 12), "String"},
				{StringUtils.center("UOM", 3), "String"},
				{StringUtils.center("TARGET QTY", 10), "Quantity"},
				{StringUtils.center("MTD QTY", 10), "Quantity"},
				{StringUtils.center("BALANCE", 10), "Quantity"},
		};
		tableData = new Query().getTableData(date, ""
				// @sql:on
				+ Item.addParentChildCTE() + ",\n" 
				+ "     siv_mtd\n" +
				"     AS (  SELECT if.id, sum (rd.qty * buy.qty / report.qty) AS qty\n" +
				"             FROM item_family AS if\n" +
				"                  INNER JOIN parent_child AS ip\n" +
				"                     ON if.id = ip.parent_id AND if.tier_id = 3\n" +
				"                  LEFT OUTER JOIN receiving_detail AS rd\n" +
				"                     ON rd.item_id = ip.child_id\n" +
				"                  LEFT OUTER JOIN receiving_header AS rh\n" +
				"                     ON     rh.receiving_id = rd.receiving_id\n" +
				"                        AND rh.receiving_date " +
				"								BETWEEN date_trunc ('month',\n" +
				"                                                    current_date)\n" +
				"                               	AND date_trunc (\n" +
				"                                   	'month',\n" +
				"                                       current_date\n" +
				"                                       + INTERVAL '1 month')\n" +
				"                  INNER JOIN qty_per AS buy\n" +
				"                     ON buy.item_id = rd.item_id " +
				"							AND buy.buy IS TRUE\n" +
				"                  INNER JOIN qty_per AS report\n" +
				"                     ON report.item_id = rd.item_id " +
				"							AND report.report IS TRUE\n" +
				"         WHERE rh.partner_id = 488\n" +
				"         GROUP BY if.id)\n" +
				"  SELECT row_number () OVER (ORDER BY if.id DESC),\n" +
				"         if.id * -1 AS id,\n" +
				"         if.name,\n" +
				"         uom.unit,\n" +
				"         CASE WHEN siv.qty IS NULL THEN 0 ELSE siv.qty END AS trgt,\n" +
				"         mtd.qty AS mtd,\n" +
				"         CASE WHEN siv.qty IS NULL THEN 0 ELSE siv.qty END" +
				" 			- mtd.qty AS qty,\n" +
				"         siv.target_date\n" +
				"    FROM item_family AS if\n" +
				"         LEFT OUTER JOIN target_siv AS siv\n" +
				"            ON siv.product_line_id = if.id AND siv.target_date = ?\n" +
				"         INNER JOIN uom ON uom.id = if.uom\n" +
				"         LEFT OUTER JOIN siv_mtd AS mtd ON if.id = mtd.id\n" +
				"   WHERE tier_id = 3\n" +
				"ORDER BY if.id DESC\n"
				// @sql:off
				);
	}
}

package ph.txtdis.windows;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

public class Inventory extends Data {

	public Inventory() {
	}

	public Inventory(String itemName) {
		type = Type.INVENTORY;
		// @sql:on
		tableHeaders = new String[][] { 
			{ StringUtils.center("#", 3), "Line" }, 
			{ StringUtils.center("ID", 4), "ID" },
			{ StringUtils.center("PRODUCT NAME", 18), "String" }, 
			{ StringUtils.center("ON-HAND", 8), "Quantity" },
			{ StringUtils.center("ON-HOLD", 8), "Quantity" }, 
		    { StringUtils.center("REJECTS", 8), "Quantity" } };
		// @sql:off
		tableData = new Query().getTableData(""
				+ "WITH " + addCTE() 
				+ "SELECT	* FROM inventory\n"
				+ "WHERE name LIKE '%" + itemName.toUpperCase() + "%'");
	}

	public static String addCTE() {
		// @sql:on
		return    "        last_count AS (SELECT max (count_date) AS count_date FROM count_closure),\n" 
				+ "        stock_take\n" 
				+ "        AS (  SELECT id.item_id, qc_id, sum (id.qty * qp.qty) AS qty\n" 
				+ "                FROM count_header AS ih\n" 
				+ "                     INNER JOIN count_detail AS id ON ih.count_id = id.count_id\n" 
				+ "                     INNER JOIN qty_per AS qp ON id.uom = qp.uom AND id.item_id = qp.item_id\n" 
				+ "                     INNER JOIN last_count ON ih.count_date = last_count.count_date\n" 
				+ "                     INNER JOIN item_header AS im ON id.item_id = im.id\n" 
				+ "            GROUP BY id.item_id, qc_id),\n" 
				+ "        adjustment\n" 
				+ "        AS (SELECT item_id, qc_id, qty\n" 
				+ "              FROM count_adjustment AS ca INNER JOIN last_count AS lc ON ca.count_date = lc.count_date),\n" 
				+ "        adjusted\n" 
				+ "        AS (SELECT * FROM stock_take\n" 
				+ "            UNION\n" 
				+ "            SELECT * FROM adjustment),\n" 
				+ "        beginning\n" 
				+ "        AS (  SELECT item_id, qc_id, sum (qty) AS qty\n" 
				+ "                FROM adjusted\n" 
				+ "            GROUP BY item_id, qc_id),\n" 
				+ "        brought_in\n" 
				+ "        AS (  SELECT id.item_id, qc_id, sum (id.qty * qp.qty) AS qty\n" 
				+ "                FROM receiving_header AS ih\n" 
				+ "                     INNER JOIN receiving_detail AS id ON ih.receiving_id = id.receiving_id\n" 
				+ "                     INNER JOIN qty_per AS qp ON id.uom = qp.uom AND id.item_id = qp.item_id\n" 
				+ "                     INNER JOIN last_count AS lc\n" 
				+ "                        ON ih.receiving_date BETWEEN lc.count_date + 1 AND current_date\n" 
				+ "            GROUP BY id.item_id, qc_id),\n" 
				+ "        sold_bundled\n" 
				+ "        AS (  SELECT bom.part_id AS item_id, 0 AS qc_id, sum (id.qty * bom.qty * qp.qty) AS qty\n" 
				+ "                FROM invoice_header AS ih\n" 
				+ "                     INNER JOIN invoice_detail AS id\n" 
				+ "                        ON ih.invoice_id = id.invoice_id AND ih.series = id.series\n" 
				+ "                     INNER JOIN last_count AS lc\n" 
				+ "                        ON ih.invoice_date BETWEEN lc.count_date + 1 AND current_date\n" 
				+ "                     INNER JOIN bom ON id.item_id = bom.item_id\n" 
				+ "                     INNER JOIN qty_per AS qp ON bom.uom = qp.uom AND bom.part_id = qp.item_id\n" 
				+ "                     INNER JOIN item_header AS im ON id.item_id = im.id AND im.type_id = 2\n" 
				+ "            GROUP BY bom.part_id, qc_id),\n" 
				+ "        sold_as_is\n" 
				+ "        AS (  SELECT id.item_id, 0 AS qc_id, sum (id.qty * qp.qty) AS qty\n" 
				+ "                FROM invoice_header AS ih\n" 
				+ "                     INNER JOIN invoice_detail AS id\n" 
				+ "                        ON ih.invoice_id = id.invoice_id AND ih.series = id.series\n" 
				+ "                     INNER JOIN qty_per AS qp ON id.uom = qp.uom AND id.item_id = qp.item_id\n" 
				+ "                     INNER JOIN last_count AS lc\n" 
				+ "                        ON ih.invoice_date BETWEEN lc.count_date + 1 AND current_date\n" 
				+ "                     INNER JOIN item_header AS im ON id.item_id = im.id AND im.type_id <> 2\n" 
				+ "            GROUP BY id.item_id, qc_id),\n" 
				+ "        sold_combined\n" 
				+ "        AS (SELECT * FROM sold_bundled\n" 
				+ "            UNION\n" 
				+ "            SELECT * FROM sold_as_is),\n" 
				+ "        sold\n" 
				+ "        AS (  SELECT item_id, qc_id, sum (qty) AS qty\n" 
				+ "                FROM sold_combined\n" 
				+ "            GROUP BY item_id, qc_id),\n" 
				+ "        delivered_bundled\n" 
				+ "        AS (  SELECT bom.part_id AS item_id,\n" 
				+ "                     CASE WHEN cm.name LIKE '%DISPOSAL%' THEN 2 ELSE 0 END AS qc_id,\n" 
				+ "                     sum (id.qty * bom.qty * qp.qty) AS qty\n" 
				+ "                FROM delivery_header AS ih\n" 
				+ "                     INNER JOIN delivery_detail AS id ON ih.delivery_id = id.delivery_id\n" 
				+ "                     INNER JOIN customer_header AS cm ON ih.customer_id = cm.id\n" 
				+ "                     INNER JOIN last_count AS lc\n" 
				+ "                        ON ih.delivery_date BETWEEN lc.count_date + 1 AND current_date\n" 
				+ "                     INNER JOIN bom ON id.item_id = bom.item_id\n" 
				+ "                     INNER JOIN qty_per AS qp ON bom.uom = qp.uom AND bom.part_id = qp.item_id\n" 
				+ "                     INNER JOIN item_header AS im ON id.item_id = im.id AND im.type_id = 2\n" 
				+ "            GROUP BY bom.part_id, qc_id),\n" 
				+ "        delivered_as_is\n" 
				+ "        AS (  SELECT id.item_id,\n" 
				+ "                     CASE WHEN cm.name LIKE '%DISPOSAL%' THEN 2 ELSE 0 END AS qc_id,\n" 
				+ "                     sum (id.qty * qp.qty) AS qty\n" 
				+ "                FROM delivery_header AS ih\n" 
				+ "                     INNER JOIN delivery_detail AS id ON ih.delivery_id = id.delivery_id\n" 
				+ "                     INNER JOIN customer_header AS cm ON ih.customer_id = cm.id\n" 
				+ "                     INNER JOIN qty_per AS qp ON id.uom = qp.uom AND id.item_id = qp.item_id\n" 
				+ "                     INNER JOIN last_count AS lc\n" 
				+ "                        ON ih.delivery_date BETWEEN lc.count_date + 1 AND current_date\n" 
				+ "                     INNER JOIN item_header AS im ON id.item_id = im.id AND im.type_id <> 2\n" 
				+ "            GROUP BY id.item_id, qc_id),\n" 
				+ "        delivered_combined\n" 
				+ "        AS (SELECT * FROM delivered_bundled\n" 
				+ "            UNION\n" 
				+ "            SELECT * FROM delivered_as_is),\n" 
				+ "        delivered\n" 
				+ "        AS (  SELECT item_id, qc_id, sum (qty) AS qty\n" 
				+ "                FROM delivered_combined\n" 
				+ "            GROUP BY item_id, qc_id),\n" 
				+ "        sent_out_combined\n" 
				+ "        AS (SELECT * FROM sold\n" 
				+ "            UNION\n" 
				+ "            SELECT * FROM delivered),\n" 
				+ "        sent_out\n" 
				+ "        AS (  SELECT item_id, qc_id, sum (qty) AS qty\n" 
				+ "                FROM sent_out_combined\n" 
				+ "            GROUP BY item_id, qc_id),\n" 
				+ "        good\n" 
				+ "        AS (SELECT im.id,\n" 
				+ "                   0 AS qc_id,\n" 
				+ "                     CASE WHEN beginning.qty IS NULL THEN 0 ELSE beginning.qty END\n" 
				+ "                   + CASE WHEN brought_in.qty IS NULL THEN 0 ELSE brought_in.qty END\n" 
				+ "                   - CASE WHEN sent_out.qty IS NULL THEN 0 ELSE sent_out.qty END\n" 
				+ "                      AS ending\n" 
				+ "              FROM item_header AS im\n" 
				+ "                   LEFT JOIN beginning ON im.id = beginning.item_id AND beginning.qc_id = 0\n" 
				+ "                   LEFT JOIN brought_in ON im.id = brought_in.item_id AND brought_in.qc_id = 0\n" 
				+ "                   LEFT JOIN sent_out ON im.id = sent_out.item_id AND sent_out.qc_id = 0\n" 
				+ "             WHERE beginning.qty IS NOT NULL OR brought_in.qty IS NOT NULL OR sent_out.qty IS NOT NULL),\n" 
				+ "        on_hold\n" 
				+ "        AS (SELECT im.id,\n" 
				+ "                   1 AS qc_id,\n" 
				+ "                     CASE WHEN beginning.qty IS NULL THEN 0 ELSE beginning.qty END\n" 
				+ "                   + CASE WHEN brought_in.qty IS NULL THEN 0 ELSE brought_in.qty END\n" 
				+ "                   - CASE WHEN sent_out.qty IS NULL THEN 0 ELSE sent_out.qty END\n" 
				+ "                      AS ending\n" 
				+ "              FROM item_header AS im\n" 
				+ "                   LEFT JOIN beginning ON im.id = beginning.item_id AND beginning.qc_id = 1\n" 
				+ "                   LEFT JOIN brought_in ON im.id = brought_in.item_id AND brought_in.qc_id = 1\n" 
				+ "                   LEFT JOIN sent_out ON im.id = sent_out.item_id AND sent_out.qc_id = 1\n" 
				+ "             WHERE beginning.qty IS NOT NULL OR brought_in.qty IS NOT NULL OR sent_out.qty IS NOT NULL),\n" 
				+ "        bad\n" 
				+ "        AS (SELECT im.id,\n" 
				+ "                   2 AS qc_id,\n" 
				+ "                     CASE WHEN beginning.qty IS NULL THEN 0 ELSE beginning.qty END\n" 
				+ "                   + CASE WHEN brought_in.qty IS NULL THEN 0 ELSE brought_in.qty END\n" 
				+ "                   - CASE WHEN sent_out.qty IS NULL THEN 0 ELSE sent_out.qty END\n" 
				+ "                      AS ending\n" 
				+ "              FROM item_header AS im\n" 
				+ "                   LEFT JOIN beginning ON im.id = beginning.item_id AND beginning.qc_id = 2\n" 
				+ "                   LEFT JOIN brought_in ON im.id = brought_in.item_id AND brought_in.qc_id = 2\n" 
				+ "                   LEFT JOIN sent_out ON im.id = sent_out.item_id AND sent_out.qc_id = 2\n" 
				+ "             WHERE beginning.qty IS NOT NULL OR brought_in.qty IS NOT NULL OR sent_out.qty IS NOT NULL),\n" 
				+ "        inventory\n" 
				+ "        AS (  SELECT row_number ()\n" 
				+ "                     OVER (\n" 
				+ "                        ORDER BY\n" 
				+ "                           CASE WHEN bad.ending IS NULL THEN 0 ELSE bad.ending END DESC,\n" 
				+ "                           CASE WHEN on_hold.ending IS NULL THEN 0 ELSE on_hold.ending END DESC,\n" 
				+ "                           CASE WHEN good.ending IS NULL THEN 0 ELSE good.ending END DESC)\n" 
				+ "                        AS line,\n" 
				+ "                     im.id,\n" 
				+ "                     im.short_id,\n" 
				+ "                     CASE WHEN good.ending IS NULL THEN 0 ELSE good.ending END AS good,\n" 
				+ "                     CASE WHEN on_hold.ending IS NULL THEN 0 ELSE on_hold.ending END AS on_hold,\n" 
				+ "                     CASE WHEN bad.ending IS NULL THEN 0 ELSE bad.ending END AS bad,\n" 
				+ "                     im.name\n" 
				+ "                FROM item_header AS im\n" 
				+ "                     LEFT JOIN good ON im.id = good.id\n" 
				+ "                     LEFT JOIN on_hold ON im.id = on_hold.id\n" 
				+ "                     LEFT JOIN bad ON im.id = bad.id\n" 
				+ "               WHERE good.ending > 0 OR on_hold.ending > 0 OR bad.ending > 0\n" 
				+ "            ORDER BY CASE WHEN bad.ending IS NULL THEN 0 ELSE bad.ending END DESC,\n" 
				+ "                     CASE WHEN on_hold.ending IS NULL THEN 0 ELSE on_hold.ending END DESC,\n" 
				+ "                     CASE WHEN good.ending IS NULL THEN 0 ELSE good.ending END DESC)\n" ;
		// @sql:off
	}

	public static BigDecimal getGoodStock(int itemId) {
		Object object = new Query().getDatum(itemId, "WITH " + Inventory.addCTE() + "SELECT good FROM inventory WHERE id = ?;");
		return object == null ? BigDecimal.ZERO : (BigDecimal) object;
	}

	public static Object[] getToBeLoadedQtyAndUom(int salesId) {
		return new Query().getList(salesId, "WITH " + Inventory.addCTE() + "SELECT good FROM inventory WHERE id = ?;");
	}

	public static BigDecimal getBadStock(int itemId) {
		Object object = new Query().getDatum(itemId, "WITH " + Inventory.addCTE() + "SELECT bad FROM inventory WHERE id = ?;");
		return object == null ? BigDecimal.ZERO : (BigDecimal) object;
	}
}

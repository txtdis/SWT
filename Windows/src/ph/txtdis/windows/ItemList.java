package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;

public class ItemList extends Report {

	public ItemList(String string) {
		module = "Item List";
		headers = new String[][] {
				{StringUtils.center("#", 4), "Line"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("NAME", 40), "String"},
				{StringUtils.center("PRICE", 9), "BigDecimal"},
				{StringUtils.center("AVAILABLE", 9), "BigDecimal"}
		};

		data = new Data().getDataArray("" +
				"WITH best_seller\n" +
				"     AS (  SELECT id.item_id, COUNT (id.item_id) AS freq\n" +
				"             FROM invoice_detail AS id\n" +
				"                  INNER JOIN invoice_header AS ih\n" +
				"                     ON id.invoice_id = ih.invoice_id\n" +
				"            WHERE ih.invoice_date " +
				"				BETWEEN CURRENT_DATE - 90 AND CURRENT_DATE\n" +
				"         GROUP BY id.item_id),\n" +
				"     latest_date\n" +
				"     AS (  SELECT item_id, tier_id, max (start_date) AS start_date\n" +
				"             FROM price\n" +
				"         GROUP BY item_id, tier_id),\n" +
				"     latest_price\n" +
				"     AS (SELECT p.item_id, p.price\n" +
				"           FROM price AS p\n" +
				"                INNER JOIN latest_date AS ld\n" +
				"                   ON     p.item_id = ld.item_id\n" +
				"                      AND p.tier_id = ld.tier_id\n" +
				"                      AND p.start_date = ld.start_date\n" +
				"          WHERE p.tier_id = 1)\n" +
				"  SELECT row_number () OVER (" +
				"		ORDER BY CASE WHEN FREQ IS NULL THEN 0 ELSE FREQ END DESC),\n" +
				"         im.id,\n" +
				"         im.name,\n" +
				"         p.price,\n" +
				"           CASE WHEN i.good IS NULL THEN 0 ELSE i.good END\n" +
				"            AS available\n" +
				"    FROM item_master AS im\n" +
				"         INNER JOIN latest_price AS p ON im.id = p.item_id\n" +
				"         LEFT OUTER JOIN best_seller AS bs ON bs.item_id = IM.id\n" +
				"         LEFT OUTER JOIN inventory AS i ON im.id = i.id\n" +
				"   WHERE im.name LIKE '%" + string.toUpperCase() + "%'\n" +
				"ORDER BY CASE WHEN FREQ IS NULL THEN 0 ELSE FREQ END DESC\n" 
				);
	}
	
	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		new ItemList("");
		Database.getInstance().closeConnection();
	}
}

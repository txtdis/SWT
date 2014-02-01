package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;

public class Pricelist extends Data {

	public Pricelist() {
		super();
		type = Type.PRICE_LIST;
		tableHeaders = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("ID", 6), "ID"},
				{StringUtils.center("PRODUCT NAME", 18), "String"},
				{StringUtils.center("PURCHASE", 11), "BigDecimal"},
				{StringUtils.center("SUPERMARKET", 11), "BigDecimal"},
				{StringUtils.center("WET MARKET", 10), "BigDecimal"}
		};
		tableData = new Query().getTableData("" +
				"SELECT CAST (row_number() over(ORDER BY im.name) AS int), " +
				"		im.id, " +
				"		im.short_id, " +
				"		buy.price, " +
				"		CASE WHEN super.price IS null " +
				"			THEN wet.price ELSE super.price END, " +
				"		CASE WHEN wet.price IS null " +
				"			THEN super.price ELSE wet.price END " +
				"FROM 	item_header AS im " +
				"LEFT OUTER JOIN (" +
				"	SELECT	p1.item_id, " +
				"			p1.price " +
				"	FROM price AS p1 " +
				"	INNER JOIN (" +
				"		SELECT 	item_id," +
				"				max(start_date) AS max_date " +
				"		FROM 	price AS p " +
				"		WHERE 	start_date < current_date " +
				"		GROUP BY item_id " +
				"	) AS p2 " +
				"	ON p1.item_id = p2.item_id " +
				"		AND p1.start_date = p2.max_date " +
				"		AND p1.tier_id = 0 " +
				") AS buy " +
				"ON im.id = buy.item_id " +
				"LEFT OUTER JOIN ( " +
				"	SELECT	p1.item_id, " +
				"			p1.price " +
				"	FROM price AS p1 " +
				"	INNER JOIN (" +
				"		SELECT 	item_id," +
				"				max(start_date) AS max_date " +
				"		FROM 	price AS p " +
				"		WHERE 	start_date < current_date " +
				"		GROUP BY item_id " +
				"	) AS p2 " +
				"	ON p1.item_id = p2.item_id " +
				"		AND p1.start_date = p2.max_date " +
				"		AND p1.tier_id = 3 " +
				") AS super " +
				"ON im.id = super.item_id " +
				"LEFT OUTER JOIN ( " +
				"	SELECT	p1.item_id, " +
				"			p1.price " +
				"	FROM price AS p1 " +
				"	INNER JOIN (" +
				"		SELECT 	item_id," +
				"				max(start_date) AS max_date " +
				"		FROM 	price AS p " +
				"		WHERE 	start_date < current_date " +
				"		GROUP BY item_id " +
				"	) AS p2 " +
				"	ON p1.item_id = p2.item_id " +
				"		AND p1.start_date = p2.max_date " +
				"		AND p1.tier_id = 1 " +
				") AS wet " +
				"ON im.id = wet.item_id " +
				"ORDER BY im.name "
				);
	}
}

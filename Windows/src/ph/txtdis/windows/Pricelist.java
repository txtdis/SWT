package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;

public class Pricelist extends Report {

	public Pricelist() {
		super();
		module = "Pricelist";
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("ID", 6), "ID"},
				{StringUtils.center("PRODUCT NAME", 64), "String"},
				{StringUtils.center("PURCHASE", 11), "BigDecimal"},
				{StringUtils.center("SUPERMARKET", 11), "BigDecimal"},
				{StringUtils.center("WET MARKET", 10), "BigDecimal"}
		};
		data = new SQL().getDataArray("" +
				"SELECT row_number() over(ORDER BY im.name), " +
				"		im.id, " +
				"		im.name, " +
				"		buy.price, " +
				"		CASE WHEN super.price IS null " +
				"			THEN wet.price ELSE super.price END, " +
				"		CASE WHEN wet.price IS null " +
				"			THEN super.price ELSE wet.price END " +
				"FROM 	item_master AS im " +
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

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Pricelist i = new Pricelist();
		for (Object[] os : i.getData()) {
			for (Object o : os) {
				System.out.print(o + ", ");
			}
			System.out.println();
		}
		Database.getInstance().closeConnection();
	}

}

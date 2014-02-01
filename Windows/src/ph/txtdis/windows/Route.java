package ph.txtdis.windows;

import java.sql.Date;
import java.util.Arrays;

public class Route {

	private int id;
	private String name;

	private static Query sql = new Query();

	public Route() {
	}
	
	public Route(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static int getId(String name) {
		return (int) sql.getDatum(name, "SELECT id FROM route WHERE name = ? ");
	}

	public static String getName(int id) {
		return (String) sql.getDatum(id, "SELECT name FROM route WHERE id = ? ");
	}

	public static int getId(int partnerId, Date date) {
		return (int) sql.getDatum(new Object[] { partnerId, date}, "" 
				// @sql:on
				+ "SELECT route_id "
				+ "  FROM account "
				+ " WHERE     customer_id = ? "
				+ "       AND start_date <= ? "
				+ "ORDER BY start_date DESC "
				+ "LIMIT 1"
				// @sql:off
				);
	}

	public static String[] getList() {
		Object[] names = sql.getList("SELECT name FROM route ORDER BY name ");
		return Arrays.copyOf(names, names.length, String[].class);
	}
	
	public static String addCTE(int partnerId, Date date) {
		// @sql:on
		return    "      latest_route AS\n" 
				+ "		 (SELECT DISTINCT ON (a.customer_id)\n" 
				+ "			     last_value( a.route_id) \n" 
				+ "				      OVER (PARTITION BY a.customer_id ORDER BY a.start_date DESC) AS id\n" 
				+ "			FROM account AS a\n" 
				+ "		   WHERE     a.customer_id = " + partnerId + "\n"
				+ "              AND a.start_date <= '" + date + "')\n";
		// @sql:off

	}
	
	public static boolean isPartnerFromAnExTruck(int partnerId, Date date) {
		return (boolean) sql.getDatum(""
				+ "WITH "
				+ addCTE(partnerId, date)
				+ "SELECT EXISTS (SELECT r.name LIKE '%EX-TRUCK%'\n" 
				+ "  FROM route AS r INNER JOIN latest_route AS lr ON lr.id = r.id)\n" 
				// @sql:off
				);
	}

	public static Object[][] getData(int partnerId) {
		return sql.getTableData(partnerId, ""
				// @sql:on
				+ "SELECT CAST (row_number() OVER(ORDER BY account.start_date) AS int), "
				+ "		  route.name, "
				+ "		  account.start_date, "
				+ "		  upper(account.user_id) "
				+ "  FROM account "
				+ "		  INNER JOIN route "
				+ "			 ON route.id = account.route_id "
				+ " WHERE 	  account.customer_id = ? "
				+ " ORDER BY account.start_date "
				// @sql:off
				);
	}
	
	public Date getStartDate(Date date, int partnerId) {
		return (Date) sql.getDatum(new Object[] { date, partnerId }, "" 
				// @sql:on
				+ "SELECT max(start_date) "
				+ "  FROM account "
				+ " WHERE start_date =< ? "
				+ "   AND customer_id = ?;"
				// @sql:off
				);
	}


}

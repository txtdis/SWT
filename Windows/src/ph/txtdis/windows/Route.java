package ph.txtdis.windows;

import java.sql.Date;
import java.util.Arrays;

public class Route {
	private int id;
	private Data sql;
	private Object object;
	private Object[] objects;
	private String name;

	public Route() {
		sql = new Data();
	}
	
	public Route(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public int getId(String name) {
		object = sql.getDatum(name, ""
				+ "SELECT id FROM route WHERE name = ? ");
		return object == null ? 0 : (int) object;
	}

	public int getId(int partnerId, Date date) {
		object = sql.getDatum(new Object[] { partnerId, date}, "" 
				+ "SELECT route_id "
				+ "  FROM account "
				+ " WHERE     customer_id = ? "
				+ "       AND start_date <= ? "
				+ "ORDER BY start_date DESC "
				+ "LIMIT 1"
				);
		return object == null ? -1 : (int) object;
	}

	public String getName() {
		return name;
	}

	public String getName(int id) {
		object = sql.getDatum(id, "SELECT name FROM route WHERE id = ? ");
		return object == null ? "" : (String) object;
	}

	public String[] getList() {
		objects = sql.getData("SELECT name FROM route ORDER BY name ");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}
	
	public boolean isPartnerFromAnExTruck(int partnerId, Date cutoff) {
		object = sql.getDatum(new Object[] {partnerId, cutoff }, ""
				// @sql:on
				+ "WITH latest_route AS\n" 
				+ "		 (SELECT DISTINCT ON (a.customer_id)\n" 
				+ "			     last_value( a.route_id) \n" 
				+ "				      OVER (PARTITION BY a.customer_id ORDER BY a.start_date DESC) AS id\n" 
				+ "			FROM account AS a\n" 
				+ "		   WHERE a.customer_id = ? AND a.start_date <= ?)\n" 
				+ "SELECT r.name LIKE '%EX-TRUCK%'\n" 
				+ "  FROM route AS r INNER JOIN latest_route AS lr ON lr.id = r.id\n" 
				// @sql:off
				);
		return object == null ? false : (boolean) object;
	}

	public Object[][] getData(int partnerId) {
		return sql.getDataArray(partnerId, ""
				+ "SELECT row_number() OVER(ORDER BY account.start_date), "
				+ "		  route.name, "
				+ "		  account.start_date, "
				+ "		  upper(account.user_id) "
				+ "  FROM account "
				+ "		  INNER JOIN route "
				+ "			 ON route.id = account.route_id "
				+ " WHERE 	  account.customer_id = ? "
				+ " ORDER BY account.start_date ");
	}
}

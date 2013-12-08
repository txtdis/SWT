package ph.txtdis.windows;

import java.sql.Date;
import java.util.Arrays;

public class Route {
	private int id;
	private Data sql;
	private Date date;
	private Object object;
	private Object[] objects;
	private String name;

	public Route() {
		this(DIS.TODAY);
	}
	
	public Route(Date date) {
		this.date = date;
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

	public int getId(int partnerId) {
		object = sql.getDatum(partnerId, "" 
				+ "SELECT route_id "
				+ "  FROM account "
				+ " WHERE     customer_id = ? "
				+ "       AND start_date <= '" + date + "'"
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
		object = sql.getDatum(partnerId, ""
				+ "WITH "
				+ SQL.addLatestRouteStmt(cutoff) + " "
				+ "SELECT route.id "
				+ "  FROM latest_route "
				+ "		  INNER JOIN route "
				+ "			 ON latest_route.id = route.id "
				+ " WHERE 	  latest_route.customer_id = ? "
				+ "		  AND route.name LIKE '%EX-TRUCK%'; ");
		return object == null ? false : true;
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

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene", "ayin", "localhost");
		Object[][] data = new Route().getData(22);
		for (Object[] objects : data) {
	        for (Object object : objects) {
	            System.out.print(object + ", ");
            }
	        System.out.println();
        }
		
		Database.getInstance().closeConnection();
	}
}

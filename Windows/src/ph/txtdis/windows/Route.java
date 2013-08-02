package ph.txtdis.windows;

import java.util.Arrays;

public class Route {
	private int id;
	private String name;
	private Data sql;

	public Route() {
		sql = new Data();
	}

	public Route(int id) {
		name = (String) new Data().getDatum(id, ""
				+ "SELECT name FROM route WHERE id = ? ");
	}

	public Route(String name) {
		Object object = new Data().getDatum(name, ""
				+ "SELECT id FROM route WHERE name = ? ");
		id = object != null ? (int) object : 0;
	}

	public Route(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public int getId(int partnerId) {
		Object object = sql.getDatum(partnerId, "" 
				+ "SELECT route_id FROM account WHERE customer_id = ? ");
		return object == null ? -1 : (int) object;
	}

	public String getName() {
		return name;
	}

	public String[] getRoutes() {
		Object[] objects = sql.getData(""
				+ "SELECT	name FROM route ORDER BY name ");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}
}

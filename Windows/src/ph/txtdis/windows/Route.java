package ph.txtdis.windows;

import java.util.Arrays;

public class Route {
	private int id;
	private String name;

	public Route() {
	}

	public Route(int id) {
		name = (String) new SQL().getDatum(name, "" +
				"SELECT	name " +
				"FROM	route " +
				"WHERE 	id = ? ");
	}

	public Route(String name) {
		Object o = new SQL().getDatum(name, "" +
				"SELECT	id " +
				"FROM	route " +
				"WHERE 	name = ? ");
		if(o != null) id = (int) o;
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

	public String[] getRoutes() {
		Object[] objects = new SQL().getData("" +
				"SELECT	name " +
				"FROM	route " + 
				"ORDER BY name " +
				""
				);
		return Arrays.copyOf(objects, objects.length, String[].class);
	}
}

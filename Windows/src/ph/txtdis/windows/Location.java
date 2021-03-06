package ph.txtdis.windows;

import java.util.Arrays;

public class Location {
	private int id;
	private String name;

	public Location() {
	}

	public Location(int id) {
		name = (String) new Query().getDatum(id, "" +
				"SELECT	name " +
				"FROM	location " +
				"WHERE 	id = ? ");
	}

	public Location(String name) {
		Object o = new Query().getDatum(name, "" +
				"SELECT	id " +
				"FROM	location " +
				"WHERE 	name = ? ");
		if(o != null) id = (int) o;
	}

	public Location(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String[] getNames() {
		Object[] objects = new Query().getList("" +
				"SELECT	name " +
				"FROM	location " + 
				"ORDER BY name " +
				"");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}	
}

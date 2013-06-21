package ph.txtdis.windows;

import java.util.Arrays;

public class Quality {
	private int id;
	private String name;

	public Quality() {
	}

	public Quality(String name) {
		Object o = new SQL().getDatum(name, "" +
				"SELECT	id " +
				"FROM	quality " +
				"WHERE 	name = ? ");
		if(o != null) id = (int) o;
	}

	public Quality(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String[] getQCStates() {
		Object[] objects = new SQL().getData("" +
				"SELECT	name " +
				"FROM	quality " + 
				"ORDER BY name " +
				"");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}	
}

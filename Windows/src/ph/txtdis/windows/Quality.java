package ph.txtdis.windows;

import java.util.Arrays;

public class Quality {
	private int id;
	private String name;

	public Quality() {
	}

	public Quality(String name) {
		Object o = new Data().getDatum(name, "" 
				+ "SELECT id " 
				+ "  FROM quality " 
				+ " WHERE name = ? ");
		id = o == null ? 0 : (int) o;
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

	public String[] getStates() {
		Object[] objects = new Data().getData("" 
				+ "SELECT name " 
				+ "  FROM quality " 
				+ " ORDER BY id ");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}
}

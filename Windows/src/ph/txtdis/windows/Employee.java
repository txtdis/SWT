package ph.txtdis.windows;

import java.util.Arrays;

public class Employee {
	private int id;
	private String name;

	public Employee() {
	}

	public Employee(String name) {
		id = (int) new SQL().getDatum(name, "" +
				"SELECT	id " +
				"FROM	contact_detail " +
				"WHERE 	name = ? " +
				"");
	}

	public Employee(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String[] getEmployees() {
		Object[] objects = new SQL().getData("" +
				"SELECT	name " +
				"FROM	contact_detail " +
				"WHERE	customer_id = 0 " + 
				"ORDER BY name " +
				"");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}	
}

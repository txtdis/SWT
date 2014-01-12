package ph.txtdis.windows;

import java.util.Arrays;

public class Employee {
	private int id;
	private String name;
	private Data sql;

	public Employee() {
		sql = new Data();
	}

	public Employee(String name) {
		this();
		id = (int) sql.getDatum(name, "" +
				"SELECT	id " +
				"  FROM	contact_detail " +
				" WHERE name = ? ");
	}

	public Employee(int id) {
		this();
		name = (String) sql.getDatum(id, "" +
				"SELECT	name " +
				"  FROM	contact_detail " +
				" WHERE	id = ? ");
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

	public String[] getNames() {
		Object[] objects = sql.getData("" +
				"SELECT	name " +
				"FROM	contact_detail " +
				"WHERE	customer_id = 0 " + 
				"ORDER BY name "
				);
		return Arrays.copyOf(objects, objects.length, String[].class);
	}
}

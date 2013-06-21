package ph.txtdis.windows;

import java.util.Arrays;

public class Area {

	private int id;

	public Area(int id) {
		this.id = id;
	}

	public Area(String name) {
		Object object = new SQL().getDatum(name, "" +
				"SELECT	a.id " +
				"FROM	area AS a " +
				"WHERE	a.name = ? ");
		if(object != null) id = (int) object; 
	}

	public int getId() {
		return id;
	}

	public String[] getAreas() {
		Object[] objects = new SQL().getData(id, "" +
				"SELECT	a.name " +
				"FROM	area AS a " +
				"INNER JOIN area_tree AS t " +
				"ON a.id = t.child_id " +
				"WHERE	t.parent_id = ? " +
				"ORDER BY a.name  " +
				"");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}

}

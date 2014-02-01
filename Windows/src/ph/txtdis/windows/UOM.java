package ph.txtdis.windows;

import java.util.ArrayList;
import java.util.Arrays;

public class UOM {

	public static int getId(String unit) {
		return (int) new Query().getDatum(unit, "SELECT id FROM uom WHERE unit = ?;");
	}

	public static int getId(Type unit) {
		return (int) new Query().getDatum(unit.toString(), "SELECT id FROM uom WHERE unit = ?;");
	}

	public static Type getType(int id) {
		return Type.valueOf((String) new Query().getDatum(id, "SELECT unit FROM uom WHERE id = ?;"));
	}

	public static String[] getUoms() {
		Object[] objects = new Query().getList("SELECT unit FROM uom ORDER BY id;");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}

	public static String[] getUoms(ArrayList<String> usedUoms) {
		String notIn = "$$";
		for (int i = 0; i < usedUoms.size(); i++) {
			if (i > 0)
				notIn += "$$, $$";
			notIn += usedUoms.get(i);
		}
		Object[] objects = new Query().getList("SELECT	unit FROM uom WHERE unit NOT IN (" + notIn + "$$) ORDER BY id;");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}

	public static String[] getUoms(int itemId) {
		Object[] objects = new Query().getList(itemId, ""
		        + "SELECT	unit FROM uom INNER JOIN qty_per ON id = uom WHERE item_id = ? ORDER BY id;");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}

	public static String[] getSellingUoms(int id) {
		Object[] objects = new Query().getList(id, "SELECT unit FROM qty_per INNER JOIN uom ON uom = id "
		        + "WHERE item_id = ? AND sell IS true ORDER BY qty, id;");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}
}

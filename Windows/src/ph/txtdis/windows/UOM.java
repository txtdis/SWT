package ph.txtdis.windows;

import java.util.ArrayList;
import java.util.Arrays;

public class UOM {
	private int id;
	private String unit;

	public UOM() {
	}

	public UOM(String unit) {
		id = (int) new SQL().getDatum(unit, "" +
				"SELECT id " +
				"FROM 	uom " +
				"WHERE	unit = ?;");
	}

	public UOM(int id) {
		unit = (String) new SQL().getDatum(id, "" +
				"SELECT unit " +
				"FROM 	uom " +
				"WHERE	id = ?;");
	}

	public int getId() {
		return 	id;
	}

	public String getUnit() {
		return unit;
	}

	public String[] getUoms() {
		Object[] objects = new SQL().getData("" +
				"SELECT	unit " +
				"FROM	uom " + 
				"ORDER BY unit; " +
				"");	
		return Arrays.copyOf(objects, objects.length, String[].class);
	}
	
	public String[] getUoms(ArrayList<String> usedUoms) {
		String notIn = "$$";
		for (int i = 0; i < usedUoms.size(); i++) {
			if(i > 0) notIn += "$$, $$"; 
			notIn += usedUoms.get(i);
		}
		Object[] objects = new SQL().getData("" +
				"SELECT	unit " +
				"FROM	uom " +
				"WHERE unit NOT IN ( " +
				notIn +
				"$$) " +
				"ORDER BY unit ; " +
				"");	
		return Arrays.copyOf(objects, objects.length, String[].class);
	}
	
	public String[] getUoms(int itemId) {
		Object[] objects = new SQL().getData(itemId, "" +
				"SELECT	unit " +
				"FROM uom " +
				"INNER JOIN qty_per " +
				"	ON id = uom " +
				"WHERE item_id = ? " + 
				"ORDER BY unit; " +
				"");	
		return Arrays.copyOf(objects, objects.length, String[].class);
	}	
	
	public String[] getSoldUoms(int id)  {
		Object[] objects = new SQL().getData(id, "" + 
				"SELECT unit " +
				"FROM 	qty_per " +
				"INNER JOIN uom " +
				"	ON 	uom = id " +
				"WHERE 	item_id = ? " +
				"	AND sell IS true " +
				"ORDER BY unit " +
				"");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}
}

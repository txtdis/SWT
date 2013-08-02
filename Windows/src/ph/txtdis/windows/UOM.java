package ph.txtdis.windows;

import java.util.ArrayList;
import java.util.Arrays;

public class UOM {
	private int id;
	private String unit;

	public UOM() {
	}

	public UOM(String unit) {
		id = (int) new Data().getDatum(unit, "" +
				"SELECT id " +
				"FROM 	uom " +
				"WHERE	unit = ?;");
	}

	public UOM(int id) {
		unit = (String) new Data().getDatum(id, "" +
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
		Object[] objects = new Data().getData("" +
				"SELECT	unit " +
				"FROM	uom " + 
				"ORDER BY id; " +
				"");	
		return Arrays.copyOf(objects, objects.length, String[].class);
	}
	
	public String[] getUoms(ArrayList<String> usedUoms) {
		String notIn = "$$";
		for (int i = 0; i < usedUoms.size(); i++) {
			if(i > 0) notIn += "$$, $$"; 
			notIn += usedUoms.get(i);
		}
		Object[] objects = new Data().getData("" +
				"SELECT	unit " +
				"FROM	uom " +
				"WHERE unit NOT IN ( " +
				notIn +
				"$$) " +
				"ORDER BY id ; " +
				"");	
		return Arrays.copyOf(objects, objects.length, String[].class);
	}
	
	public String[] getUoms(int itemId) {
		Object[] objects = new Data().getData(itemId, "" +
				"SELECT	unit " +
				"FROM uom " +
				"INNER JOIN qty_per " +
				"	ON id = uom " +
				"WHERE item_id = ? " + 
				"ORDER BY id; " +
				"");	
		return Arrays.copyOf(objects, objects.length, String[].class);
	}	
	
	public String[] getSellingUoms(int id)  {
		Object[] objects = new Data().getData(id, "" + 
				"SELECT unit " +
				"FROM 	qty_per " +
				"INNER JOIN uom " +
				"	ON 	uom = id " +
				"WHERE 	item_id = ? " +
				"	AND sell IS true " +
				"ORDER BY qty, id " +
				"");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}
}

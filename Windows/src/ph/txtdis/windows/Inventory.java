package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;

public class Inventory extends Report {
	public Inventory (String itemName) {		
		module = "Inventory"; 
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("PRODUCT NAME", 40), "String"},
				{StringUtils.center("ON-HAND", 8), "Quantity"},
				{StringUtils.center("ON-HOLD", 8), "Quantity"},
				{StringUtils.center("REJECTS", 8), "Quantity"}
		};
		data = new Data().getDataArray(
				"SELECT	* " +
				"FROM 	inventory " +
				"WHERE 	name LIKE '%" + itemName.toUpperCase() + "%'");
	}
}

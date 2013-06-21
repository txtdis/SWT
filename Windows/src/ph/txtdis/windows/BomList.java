package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;

public class BomList extends Report {
	private int itemId;

	public BomList (int itemId) {
		this.itemId = itemId;
		module = "Bill of Materials";
		headers = new String[][] {
				{StringUtils.center("#", 2), "Line"},
				{StringUtils.center("ID", 6), "ID"},
				{StringUtils.center("NAME", 40), "String"},
				{StringUtils.center("UOM", 5), "String"},
				{StringUtils.center("QUANTITY", 10), "BigDecimal"}
		};

		data = new SQL().getDataArray(itemId, "" +
				"SELECT " +
				"		ROW_NUMBER() OVER(), " +
				"		bom.part_id, " +
				"		im.name, " +
				"		uom.unit, " +
				"		bom.qty " +
				"FROM 	bom " +
				"INNER JOIN item_master AS im " +
				"	ON bom.part_id = im.id " +
				"INNER JOIN uom " +
				"	ON bom.uom = uom.id " +
				"WHERE bom.item_id = ? " +
				"");
	}

	public int getItemId() {
		return itemId;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		new BomList(0);
		Database.getInstance().closeConnection();
	}
}

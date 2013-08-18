package ph.txtdis.windows;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class BomList extends Report {

	public BomList (ItemMaster im) {
		itemId = im.getId();
		module = "Bill of Materials";
		headers = new String[][] {
				{StringUtils.center("#", 2), "Line"},
				{StringUtils.center("ID", 6), "ID"},
				{StringUtils.center("NAME", 40), "String"},
				{StringUtils.center("UOM", 5), "String"},
				{StringUtils.center("QUANTITY", 10), "BigDecimal"}
		};

		if(itemId != 0) {
		data = new Data().getDataArray(itemId, "" +
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
		} else {
			ArrayList<BOM> bomList = im.getBomList();
			int bomSize = bomList.size();
			if (bomSize > 0) {
				BOM bom;
				ItemHelper item = new ItemHelper();
				data = new Object[bomSize][5];
				for (int i = 0; i < bomSize; i++) {
					bom = bomList.get(i);
					int childId = bom.getItemId();
					data[i][0] = i + 1;
					data[i][1] = childId;
					data[i][2] = item.getName(childId);
					data[i][3] = new UOM(bom.getUom()).getUnit();
					data[i][4] = bom.getQty();
                }
			}
		}
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin","localhost");
		new BomList(new ItemMaster(0));
		Database.getInstance().closeConnection();
	}
}

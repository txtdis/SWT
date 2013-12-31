package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.Arrays;

public class ItemHelper {
	private Data sql;
	private Object object;
	private Object[] objects;

	public ItemHelper() {
		sql = new Data();
	}

	public int getId(String shortId) {
		object = sql.getDatum(shortId, "SELECT id FROM item_master WHERE short_id = ?");
		return object == null ? 0 : (int) object;
	}

	public int getId(long unspsc) {
		object = sql.getDatum(unspsc, "SELECT id FROM item_master WHERE unspsc_id = ?");
		return object == null ? 0 : (int) object;
	}

	public String getShortId(String name) {
		object = sql.getDatum(name, "SELECT short_id FROM item_master WHERE name = ?");
		return object == null ? "" : (String) object;
	}

	public String getShortId(int id) {
		object = sql.getDatum(id, "SELECT short_id FROM item_master WHERE id = ?;");
		return object == null ? "" : (String) object;
	}

	public String getName(int id) {
		object = sql.getDatum(id, "SELECT name FROM item_master WHERE id = ?;");
		return object == null ? "" : (String) object;
	}

	public String getFamily(int familyId) {
		object = sql.getDatum(familyId, "SELECT name FROM item_family WHERE id = ?;");
		return object == null ? "" : (String) object;
	}

	public String getBizUnit(String productLine) {
		int productLineId = getFamilyId(productLine);
		return getBizUnit(productLineId);
	}

	public String getBizUnit(int childId) {
		// @sql:on
		object = sql.getDatum(childId, ""
				+ SQL.addItemParentStmt() 
				+ "SELECT name " 
				+ "  FROM item_family AS if INNER JOIN parent_child AS ip ON if.id = ip.parent_id "
				+ " WHERE tier_id = 1 AND child_id = ?; ");
		// @sql:off
		return object == null ? "" : (String) object;
	}

	public int getFamilyId(String familyName) {
		object = sql.getDatum(familyName, "SELECT id FROM item_family WHERE name = ?;");
		return object != null ? (int) object : 0;
	}

	public String[] getFamilies(int tierId) {
		// @sql:on
		objects = sql.getData(tierId, ""
				+ "SELECT name "
				+ "  FROM item_family "
				+ " WHERE tier_id = ? "
				+ " ORDER BY id DESC;"
		        );
		// @sql:off
		if (objects != null)
			return Arrays.copyOf(objects, objects.length, String[].class);
		else {
			return new String[] {DIS.ITEM_FAMILY};			
		}
	}

	public int getMaxStockDays(int familyId) {
		// @sql:on
		object = sql.getDatum(familyId,"" 
				+ "SELECT CASE WHEN days IS NULL THEN 0 ELSE days END AS days "
				+ "  FROM target_stock_days "
				+ " WHERE item_family_id = ?");
		// @sql:off
		return object != null ? (int) object : 0;
	}

	public String[] getTypes() {
		// @sql:on
		objects = sql.getData(""
				+ "SELECT name "
				+ "  FROM item_type "
				+ "ORDER BY id;");
		// @sql:off
		return Arrays.copyOf(objects, objects.length, String[].class);
	}

	public int getTypeId(String name) {
		object = sql.getDatum(name, "SELECT id FROM item_type WHERE name = ?;");
		return object != null ? (int) object : 0;
	}

	public String[] getProductLines(int categoryId) {
		// @sql:on
		objects = sql.getData(""
				+ "SELECT name "
				+ "  FROM item_family WHERE id BETWEEN " + (categoryId * 10 - 9) 
				+ "                                AND " + (categoryId * 10)
				+ " ORDER BY id DESC ");
		// @sql:off
		return Arrays.copyOf(objects, objects.length, String[].class);
	}

	public Integer[] getProductLineIds(int categoryId) {
		// @sql:on
		objects = sql.getData(""
				+ "SELECT id "
				+ "  FROM item_family WHERE id BETWEEN " + (categoryId * 10 - 9) 
				+ "                                AND " + (categoryId * 10)
				+ " ORDER BY id DESC ");
		// @sql:off
		return Arrays.copyOf(objects, objects.length, Integer[].class);
	}

	public boolean isMonetaryType(int id, String type) {
		String order = "";
		if (type.equals("delivery")) {
			order = "INNER JOIN item_type AS it ON im.type_id = it.id WHERE it.name = 'MONETARY' AND im.id = ?;";
		} else {
			order = " WHERE im.name = $$DEALERS' INCENTIVE$$ AND im.id = ?;";
		}
		object = sql.getDatum(id, "SELECT im.id FROM item_master AS im " + order);
		return object == null ? false : true;
	}

	public boolean isSold(int id) {
		object = sql.getDatum(id, "SELECT qty FROM qty_per WHERE sell IS TRUE AND item_id = ? LIMIT 1;");
		return object == null ? false : true;
	}

	public boolean isRefMeat(String productLine) {
		return getBizUnit(productLine).equals("REF MEAT");
	}

	public boolean isPurchased(String type) {
		return type.equals("PURCHASED");
	}

	public boolean isTraded(String type) {
		return !(type.equals("VIRTUAL") || type.equals("MONETARY"));
	}

	public boolean isWithBOM(String type) {
		return type.equals("REPACKED") || type.equals("BUNDLED") || type.equals("DERIVED");
	}

	public boolean isDiscountExempt(int id) {
		object = sql.getDatum(id, "SELECT not_discounted FROM item_master WHERE id = ?;");
		return object == null ? false : (boolean) object;
	}

	public BigDecimal getAvailableStock(int itemId) {
		object = sql.getDatum(itemId,
				"WITH " + SQL.addInventoryStmt()
				+ "SELECT good FROM inventory WHERE id = ?;");
		
		return object == null ? BigDecimal.ZERO : (BigDecimal) object;
	}

	public Object[] getToBeLoadedQtyAndUom(int salesId) {
		return sql.getData(salesId, 
				"WITH "	+ SQL.addInventoryStmt()
				+ "SELECT good FROM inventory WHERE id = ?;");
	}

	public BigDecimal getBadStock(int itemId) {
		object = sql.getDatum(itemId,
				"WITH "	+ SQL.addInventoryStmt()
				+ "SELECT bad FROM inventory WHERE	id = ?;");
		return object == null ? BigDecimal.ZERO : (BigDecimal) object;
	}

	public BigDecimal getReferenceQty(int itemId, int refId) {
		String orderType = refId < 0 ? "purchase" : "sales";
		// @sql:on
		object = sql.getDatum(new Object[] {itemId, Math.abs(refId) },"" 
				+ "SELECT od.qty * qp.qty "
				+ "  FROM " + orderType + "_detail AS od"
				+ "  	  INNER JOIN qty_per as qp "
				+ "			 ON 	abs(od.item_id) = qp.item_id "
				+ "				AND od.uom = qp.uom " 
				+ "  	  INNER JOIN uom "
				+ "			 ON uom.id = od.uom "
				+ " WHERE 		abs(od.item_id) = abs(?) "
				+ "       AND " + orderType + "_id = ? ");
		// @sql:off
		return object == null ? BigDecimal.ZERO : (BigDecimal) object;
	}
}

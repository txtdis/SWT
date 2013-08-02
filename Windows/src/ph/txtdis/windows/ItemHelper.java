package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.Arrays;

public class ItemHelper {
	private Data sql;
	private Object object;

	public ItemHelper() {
		sql = new Data();
	}

	public int getId(String shortId) {
		object = sql.getDatum(shortId, "SELECT id FROM item_master WHERE short_id = ?");
		return object != null ? (int) object : 0;
	}

	public int getId(long unspsc) {
		object = sql.getDatum(unspsc, "SELECT id FROM item_master WHERE unspsc_id = ?");
		return object != null ? (int) object : 0;
	}

	public String getShortId(String name) {
		return (String) sql.getDatum(name, "SELECT short_id FROM item_master WHERE name = ?");
	}

	public String getShortId(int id) {
		return (String) sql.getDatum(id, "SELECT short_id FROM item_master WHERE id = ?;");
	}

	public String getName(int id) {
		Object name = sql.getDatum(id, "SELECT name FROM item_master WHERE id = ?;");
		return name == null ? "" : (String) name;
	}

	public String getFamily(int familyId) {
		return (String) sql.getDatum(familyId, "SELECT name FROM item_family WHERE id = ?;");
	}

	public String getBizUnit(String productLine) {
		int productLineId = getFamilyId(productLine);
		return getBizUnit(productLineId);
	}

	public String getBizUnit(int childId) {
		return (String) sql.getDatum(childId, ""
				// @sql:on
				+ "SELECT name " 
				+ "  FROM item_family AS if INNER JOIN item_parent AS ip ON if.id = ip.parent_id "
				+ " WHERE tier_id = 1 AND child_id = ?; "
				// @sql:off
				);
	}

	public int getFamilyId(String familyName) {
		object = sql.getDatum(familyName, "SELECT id FROM item_family WHERE name = ?;");
		return object != null ? (int) object : 0;
	}

	public String[] getFamilies(int tierId) {
		Object[] objects = sql.getData(tierId, "SELECT name FROM item_family WHERE tier_id = ? ORDER BY name;");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}

	public int getMaxStockDays(int familyId) {
		object = sql.getDatum(familyId,"" 
				// @sql:on
				+ "SELECT CASE WHEN days IS NULL THEN 0 ELSE days END AS days "
				+ "  FROM target_stock_days "
				+ " WHERE item_family_id = ?");
				// @sql:off
		return object != null ? (int) object : 0;
	}

	public String[] getTypes() {
		Object[] objects = sql.getData(""
				// @sql:on
				+ "SELECT name "
				+ "  FROM item_type "
				+ "ORDER BY id;");
				// @sql:off
		return Arrays.copyOf(objects, objects.length, String[].class);
	}

	public int getTypeId(String name) {
		return (int) sql.getDatum(name, "SELECT id FROM item_type WHERE name = ?;");
	}

	public String[] getProductLines(int categoryId) {
		Object[] objects = sql.getData(""
				// @sql:on
				+ "SELECT name "
				+ "  FROM item_family WHERE id BETWEEN " + (categoryId * 10 - 9) 
				+ "                                AND " + (categoryId * 10)
				+ " ORDER BY id DESC "
				// @sql:off
				);
		return Arrays.copyOf(objects, objects.length, String[].class);
	}

	public boolean isMonetaryType(int id, String type) {
		String order = "";
		if (type.equals("delivery")) {
			order = // @sql:on
					"         INNER JOIN item_type AS it "
					+ "          ON im.type_id = it.id "
					+ " WHERE     it.name = 'MONETARY' "
					+ "       AND im.id = ?;";
					// @sql:off
		} else {
			order = // @sql:on
					"   WHERE     im.name = $$DEALERS' INCENTIVE$$"
					+ "       AND im.id = ?;";
					// @sql:off
		}
		object = sql.getDatum(id, ""
				// @sql:on
				+ "SELECT im.id "
				+ "  FROM item_master AS im "
				+ order);
				// @sql:off
		return object == null ? false : true;
	}

	public boolean isSold(int id) {
		object = sql.getDatum(id, ""
				// @sql:on
				+ "SELECT im.id "
				+ "  FROM item_master AS im "
				+ "       INNER JOIN qty_per AS qp "
				+ "          ON im.id = qp.item_id "
				+ "WHERE     qp.sell IS TRUE "
				+ "      AND im.id = ? "
				+ "LIMIT 1;");
				// @sql:off
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
		object = sql.getDatum(id, ""
				// @sql:on
				+ "SELECT not_discounted "
				+ "  FROM item_master "
				+ " WHERE id = ?;");
				// @sql:off
		return object == null ? false : (boolean) object;
	}

	public BigDecimal getAvailableStock(int itemId) {
		object = sql.getDatum(itemId, "SELECT good FROM inventory WHERE id = ?;");
		return (object == null ? BigDecimal.ZERO : (BigDecimal) object);
	}

	public Object[] getToBeLoadedQtyAndUom(int salesId) {
		return sql.getData(salesId, "SELECT good FROM inventory WHERE id = ?;");
	}

	public BigDecimal getBadStock(int itemId) {
		object = sql.getDatum(itemId, "SELECT bad FROM	inventory WHERE	id = ?;");
		return (object == null ? BigDecimal.ZERO : (BigDecimal) object);
	}

	public Object[] getRefQtyAndUOM(int itemId, int refId) {
		String orderType = refId < 0 ? "purchase" : "sales";
		return sql.getData(new Object[] { itemId, Math.abs(refId) }, "" 
				// @sql:on
				+ "SELECT od.qty, "
				+ "		  uom.unit, "
				+ "		  qp.qty AS qty_per "
				+ "  FROM " + orderType + "_detail AS od"
				+ "  	  INNER JOIN qty_per as qp "
				+ "			 ON 	od.item_id = qp.item_id "
				+ "				AND od.uom = qp.uom " 
				+ "  	  INNER JOIN uom "
				+ "			 ON uom.id = od.uom "
				+ " WHERE 		od.item_id = ? "
				+ "       AND " + orderType + "_id = ? "
				// @sql:off
				);
	}
}

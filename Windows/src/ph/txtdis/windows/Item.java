package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.Arrays;

public class Item {
	private static Query sql = new Query();

	public static int getId(String shortId) {
		Object object = sql.getDatum(shortId, "SELECT id FROM item_header WHERE short_id = ?");
		return object == null ? 0 : (int) object;
	}

	public static int getId(long unspsc) {
		Object object = sql.getDatum(unspsc, "SELECT id FROM item_header WHERE unspsc_id = ?");
		return object == null ? 0 : (int) object;
	}

	public static String getShortId(String name) {
		Object object = sql.getDatum(name, "SELECT short_id FROM item_header WHERE name = ?");
		return object == null ? "" : (String) object;
	}

	public static String getShortId(int id) {
		Object object = sql.getDatum(id, "SELECT short_id FROM item_header WHERE id = ?;");
		return object == null ? "" : (String) object;
	}

	public static String getName(int id) {
		Object object = sql.getDatum(id, "SELECT name FROM item_header WHERE id = ?;");
		return object == null ? "" : (String) object;
	}

	public static String getFamily(int familyId) {
		Object object = sql.getDatum(familyId, "SELECT name FROM item_family WHERE id = ?;");
		return object == null ? "" : (String) object;
	}

	public static String getBizUnit(String productLine) {
		int productLineId = getFamilyId(productLine);
		return getBizUnit(productLineId);
	}

	public static String getBizUnit(int childId) {
		Object object = sql.getDatum(childId, addParentChildCTE()
		        + "SELECT name FROM item_family AS if INNER JOIN parent_child AS ip ON if.id = ip.parent_id "
		        + " WHERE tier_id = 1 AND child_id = ?");
		return object == null ? "" : (String) object;
	}

	public static int getFamilyId(String familyName) {
		Object object = sql.getDatum(familyName, "SELECT id FROM item_family WHERE name = ?;");
		return object != null ? (int) object : 0;
	}

	public static String[] getFamilies(int tierId) {
		Object[] objects = sql.getList(tierId, "SELECT name FROM item_family WHERE tier_id = ? ORDER BY id DESC");
		if (objects != null)
			return Arrays.copyOf(objects, objects.length, String[].class);
		else {
			return new String[] { DIS.ITEM_FAMILY };
		}
	}

	public static int getMaxStockDays(int familyId) {
		Object object = sql.getDatum(familyId, "SELECT days FROM target_stock_days WHERE item_family_id = ?");
		return object != null ? (int) object : 0;
	}

	public static String[] getTypes() {
		Object[] objects = sql.getList("SELECT name FROM item_type ORDER BY id;");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}

	public static int getTypeId(String name) {
		Object id = sql.getDatum(name, "SELECT id FROM item_type WHERE name = ?;");
		return id != null ? (int) id : 0;
	}

	public static String[] getProductLines(int categoryId) {
		Object[] productLines = sql.getList("SELECT name FROM item_family WHERE id BETWEEN " + (categoryId * 10 - 9)
		        + " AND " + (categoryId * 10) + " ORDER BY id DESC ");
		return Arrays.copyOf(productLines, productLines.length, String[].class);
	}

	public static Integer[] getProductLineIds(int categoryId) {
		Object[] productLineIds = sql.getList("SELECT id FROM item_family WHERE id BETWEEN " + (categoryId * 10 - 9)
		        + " AND " + (categoryId * 10) + " ORDER BY id DESC ");
		return Arrays.copyOf(productLineIds, productLineIds.length, Integer[].class);
	}

	public static String[] getMonetaryTypes() {
		Object[] types = sql.getList("SELECT name FROM monetary ORDER BY name ");
		return Arrays.copyOf(types, types.length, String[].class);
	}

	public static int getMonetaryId(String name) {
		return (int) sql.getDatum(name, "SELECT id FROM monetary WHERE name = ?");
	}

	public static boolean isMonetary(int id) {
		return (boolean) sql.getDatum(id, "SELECT EXISTS "
				+ "(SELECT 1 FROM item_header AS im INNER JOIN item_type AS it ON im.type_id = it.id "
				+ " WHERE it.name = 'MONETARY' AND im.id = ?)");
	}

	public static boolean isSold(int id) {
		Object object = sql.getDatum(id,  "SELECT EXISTS (SELECT 1 FROM qty_per WHERE sell IS TRUE AND item_id = ?);");
		return object == null ? false : true;
	}

	public static boolean isRefMeat(String productLine) {
		return getBizUnit(productLine).equals("REF MEAT");
	}

	public static boolean isPurchased(String type) {
		return type.equals("PURCHASED");
	}

	public static boolean isWithBOM(String type) {
		return type.equals("REPACKED") || type.equals("BUNDLED") || type.equals("FREEBIE") || type.equals("PROMO");
	}

	public static boolean isDiscountExempt(int id) {
		Object object = sql.getDatum(id, "SELECT not_discounted FROM item_header WHERE id = ?;");
		return object == null ? false : (boolean) object;
	}

	public static BigDecimal getReferenceQty(int itemId, int refId) {
		String orderType = refId < 0 ? "purchase" : "sales";
		Object referenceQty = sql.getDatum(new Object[] { itemId, Math.abs(refId) },"" 
				// @sql:on
				+ "SELECT od.qty * qp.qty "
				+ "  FROM " + orderType + "_detail AS od"
				+ "  	  INNER JOIN qty_per as qp "
				+ "			 ON 	abs(od.item_id) = qp.item_id "
				+ "				AND od.uom = qp.uom " 
				+ "  	  INNER JOIN uom "
				+ "			 ON uom.id = od.uom "
				+ " WHERE 		abs(od.item_id) = abs(?) "
				+ "       AND " + orderType + "_id = ? "
				// @sql:off
		        );
		return referenceQty == null ? BigDecimal.ZERO : (BigDecimal) referenceQty;
	}

	public static String addParentChildCTE() {
		return""
				// @sql:on
				+ "WITH RECURSIVE parent_child (child_id, parent_id) AS (\n" 
				+ " SELECT it.child_id,\n"
				+ "        it.parent_id\n" 
				+ "   FROM item_tree AS it\n" 
				+ "  UNION ALL\n"
				+ " SELECT parent_child.child_id,\n" 
				+ "        it.parent_id\n" 
				+ "   FROM item_tree it\n"
				+ "   JOIN parent_child\n" 
				+ "     ON it.child_id = parent_child.parent_id)\n"
				// @sql:off
		;
	}

	public static boolean isCredit(int id) {
		return (boolean) sql.getDatum(id, "SELECT EXISTS "
		        + "(SELECT id FROM item_header WHERE im.name LIKE '% CREDIT' AND im.id = ?);");
	}

	public static boolean isInvoiceOnly(int id) {
		return (boolean) sql.getDatum(id, "SELECT EXISTS "
		        + "(SELECT id FROM item_header WHERE im.name = $$DEALERS' INCENTIVE$$ AND im.id = ?);");
    }

	public static boolean isPhysical(int id) {
		return (boolean) sql.getDatum(id, "SELECT EXISTS "
				+ "(SELECT 1 FROM item_header AS im INNER JOIN item_type AS it ON im.type_id = it.id "
				+ " WHERE it.name IN ('PURCHASED', 'FREEBIE') AND im.id = ?)");
	}
}

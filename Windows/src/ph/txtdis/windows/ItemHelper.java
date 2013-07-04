package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.Arrays;

public class ItemHelper {

	public ItemHelper() {
	}

	public int getId(String shortId) {
		Object o = new SQL().getDatum(shortId, "SELECT id "
				+ "FROM item_master WHERE short_id = ?");
		return o != null ? (int) o : 0;
	}

	public int getId(long unspsc) {
		Object o = new SQL().getDatum(unspsc, "SELECT id "
				+ "FROM item_master WHERE unspsc_id = ?");
		return o != null ? (int) o : 0;
	}

	public String getShortId(String name) {
		return (String) new SQL().getDatum(name, "SELECT short_id "
				+ "FROM item_master WHERE name = ?");
	}

	public String getShortId(int id) {
		return (String) new SQL().getDatum(id, "SELECT short_id "
				+ "FROM item_master WHERE id = ?");
	}

	public String getName(int id) {
		return (String) new SQL().getDatum(id, "SELECT name "
				+ "FROM item_master WHERE id = ?");
	}

	public String getFamilyName(int familyId) {
		return (String) new SQL().getDatum(familyId, "SELECT name "
				+ "FROM item_family WHERE id = ?");
	}

	public int getFamilyId(String familyName) {
		Object object = new SQL().getDatum(familyName, "SELECT id "
				+ "FROM item_family WHERE name = ?");
		return object != null ? (int) object : 0;
	}

	public String[] getFamilies(int tierId) {
		Object[] objects = new SQL().getData(tierId, "SELECT name "
				+ "FROM item_family WHERE tier_id = ? " + "ORDER BY name ");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}

	public int getMaxStockDays(int familyId) {
		return (int) new SQL().getDatum(familyId, ""
				+ "SELECT CASE WHEN days IS NULL THEN 0 ELSE days END AS days "
				+ "FROM target_stock_days WHERE item_family_id = ?");
	}

	public String[] getTypes() {
		Object[] objects = new SQL().getData("SELECT name "
				+ "FROM item_type ORDER BY id");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}

	public int getTypeId(String name) {
		return (int) new SQL().getDatum(name, "SELECT id "
				+ "FROM item_type WHERE name = ? ");
	}

	public String[] getProductLines(int categoryId) {
		Object[] objects = new SQL().getData("SELECT name "
				+ "FROM	item_family WHERE id BETWEEN " + (categoryId * 10 - 9)
				+ " AND " + (categoryId * 10) + " " + "ORDER BY id DESC ");
		return Arrays.copyOf(objects, objects.length, String[].class);
	}

	public Integer getProductLineId(String productLine) {
		return (Integer) new SQL().getDatum(productLine, "SELECT id "
				+ "FROM	item_family WHERE name = ? ");
	}

	public boolean isMonetaryType(int id) {
		Object o = new SQL().getDatum(id, "SELECT	im.id "
				+ "FROM	item_master AS im INNER JOIN item_type AS it "
				+ "ON im.type_id = it.id WHERE it.name = 'MONETARY' "
				+ "AND im.id = ? " + "");
		return o == null ? false : true;
	}

	public boolean isNotDiscounted(int id) {
		return (boolean) new SQL().getDatum(id, "SELECT	not_discounted "
				+ "FROM	item_master WHERE id = ?");
	}

	public BigDecimal getAvailableStock(int itemId) {
		Object obj = new SQL().getDatum(itemId, "SELECT	good "
				+ "FROM	inventory WHERE id = ? ");
		return (obj == null ? BigDecimal.ZERO : (BigDecimal) obj);
	}

	public Object[] getToBeLoadedQtyAndUom(int salesId) {
		return new SQL().getData(salesId, "SELECT	good "
				+ "FROM	inventory WHERE id = ? ");
	}

	public BigDecimal getBadStock(int itemId) {
		Object obj = new SQL().getDatum(itemId, "SELECT	bad "
				+ "FROM	inventory WHERE	id = ?");
		return (obj == null ? BigDecimal.ZERO : (BigDecimal) obj);
	}

	public BigDecimal getItemQtyInSO(int itemId, int soId) {
		Object obj = new SQL().getDatum(new Object[] { itemId, soId }, ""
				+ "SELECT sum(sd.qty * qp.qty) FROM sales_detail AS sd "
				+ "INNER JOIN qty_per AS qp "
				+ "ON sd.item_id = qp.item_id AND sd.uom = qp.uom "
				+ "WHERE sd.item_id = ? AND sales_id = ? ");
		return (obj == null ? new BigDecimal(-1) : (BigDecimal) obj);
	}
}

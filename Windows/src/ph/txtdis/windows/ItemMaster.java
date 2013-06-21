package ph.txtdis.windows;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class ItemMaster extends Report {
	private long unspscId;
	private boolean notDiscounted;
	private String shortId, name, type, productLine;
	private String[] types, productLines;
	private String[][] uomHeaders, priceHeaders, discountHeaders;
	private Object[][] uomData, priceData, discountData;
	private ArrayList<BOM> bomList;
	private ArrayList<QtyPer> uomList;
	private ArrayList<Price> priceList;
	private ArrayList<VolumeDiscount> discountList;

	public ItemMaster(int id)  {
		super();
		this.id = id;
		SQL sql = new SQL();
		module = "Item Data";
		uomHeaders = new String[][]{
				{StringUtils.center("#", 1), "Line"},
				{StringUtils.center("QUANTITY", 18), "UOM"},
				{StringUtils.center("UOM", 5), "String"},
				{StringUtils.center("BUY", 6), "Boolean"},
				{StringUtils.center("SELL", 6), "Boolean"},
				{StringUtils.center("REPORT", 6), "Boolean"}};
		discountHeaders = new String[][]{
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("DISCOUNT", 8), "BigDecimal"},
				{StringUtils.center("PER QTY", 8), "Integer"},
				{StringUtils.center("UOM", 5), "String"},
				{StringUtils.center("CHANNEL", 18), "String"},
				{StringUtils.center("SINCE", 10), "Date"}};
		priceHeaders = new String[][]{
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("PURCHASE", 8), "BigDecimal"},
				{StringUtils.center("DEALER", 8), "BigDecimal"},
				{StringUtils.center("RETAIL", 8), "BigDecimal"},
				{StringUtils.center("MT LIST", 8), "BigDecimal"},
				{StringUtils.center("MT SRP", 8), "BigDecimal"},
				{StringUtils.center("SINCE", 10), "Date"},
				{StringUtils.center("ENCODER", 7), "String"}};
		if (id == 0) {
			shortId = "";
			name = "";
			uomData = new Object[0][0];
			discountData = new Object[0][0];
			priceData = new Object[0][0];
			bomList = new ArrayList<>();
		} else {
			Object[] ao = sql.getData(id, "" +
					"SELECT	im.short_id, " +
					"		im.name, " +
					"		if.name, " +
					"		im.unspsc_id, " +
					"		im.not_discounted, " +
					"		if.name " +
					"FROM	item_master AS im " +
					"INNER JOIN item_tree AS it " +
					"	ON 	im.id = it.child_id " +
					"INNER JOIN item_family as if " +
					"	ON 	it.parent_id = if.id " +
					"WHERE	im.id = ? " +
					""
					);
			if(ao != null) {
				shortId = (String) ao[0];
				name = (String) ao[1];
				type = (String) ao[2];
				unspscId = ao[3] == null ? 0L : (long) ao[3];
				notDiscounted = ao[4] == null ? false : (boolean) ao[4];
				productLine = (String) ao[5];
			}
			uomData = sql.getDataArray(id, "" +
					"SELECT	ROW_NUMBER() OVER(ORDER BY uom.id), " +
					"		CASE WHEN uom.unit='CS' OR uom.unit='TE' " +
					"			THEN qp.qty ELSE 1/qp.qty END, " +
					"		uom.unit, " +
					"		CASE WHEN qp.buy IS NULL THEN FALSE ELSE qp.buy END, " +
					"		CASE WHEN qp.sell IS NULL THEN FALSE ELSE qp.sell END, " +
					"		CASE WHEN qp.report IS NULL THEN FALSE ELSE qp.report END " +
					"FROM	uom " +
					"INNER JOIN qty_per AS qp " +
					"	ON 	uom.id = qp.uom " +
					"WHERE	qp.item_id = ? " +
					"ORDER BY uom.id " +
					""
					);
			priceData = sql.getDataArray(id, "" +
					"WITH item AS ( " +
					"	SELECT ? AS id " +
					"), " +
					"buy AS ( " +
					"	SELECT	item_id, " +
					"			price, " +
					"			start_date, " +
					"			user_id " +
					"	FROM	price " +
					"	INNER JOIN item " +
					"		ON 	item_id = id " +
					"	WHERE	tier_id = 0 " +
					"), " +
					"deal AS ( " +
					"	SELECT	item_id, " +
					"			price, " +
					"			start_date, " +
					"			user_id " +
					"	FROM	price " +
					"	INNER JOIN item " +
					"		ON 	item_id = id " +
					"	WHERE	tier_id = 1 " +
					"), " +
					"ret AS ( " +
					"	SELECT	item_id, " +
					"			price, " +
					"			start_date, " +
					"			user_id " +
					"	FROM	price " +
					"	INNER JOIN item " +
					"		ON 	item_id = id " +
					"	WHERE	tier_id = 2 " +
					"), " +
					"list AS ( " +
					"	SELECT	item_id, " +
					"			price, " +
					"			start_date, " +
					"			user_id " +
					"	FROM	price " +
					"	INNER JOIN item " +
					"		ON 	item_id = id " +
					"	WHERE	tier_id = 3 " +
					"), " +
					"srp AS ( " +
					"	SELECT	item_id, " +
					"			price, " +
					"			start_date, " +
					"			user_id " +
					"	FROM	price " +
					"	INNER JOIN item " +
					"		ON 	item_id = id " +
					"	WHERE	tier_id = 4 " +
					") " +
					"SELECT	ROW_NUMBER() OVER(ORDER BY deal.start_date DESC), " +
					"		CASE WHEN buy.price IS NULL THEN 0 ELSE buy.price END, " +
					"		CASE WHEN deal.price IS NULL THEN 0 ELSE deal.price END, " +
					"		CASE WHEN ret.price IS NULL THEN 0 ELSE ret.price END, " +
					"		CASE WHEN list.price IS NULL THEN 0 ELSE list.price END, " +
					"		CASE WHEN srp.price IS NULL THEN 0 ELSE srp.price END, " +
					"		deal.start_date, " +
					"		deal.user_id " +
					"FROM	item AS i " +
					"LEFT OUTER JOIN deal " +
					"	ON 	id = deal.item_id " +
					"LEFT OUTER JOIN buy " +
					"	ON	deal.item_id = buy.item_id " +
					"	AND deal.start_date = buy.start_date " +
					"	AND deal.user_id = buy.user_id " +
					"LEFT OUTER JOIN ret " +
					"	ON	deal.item_id = ret.item_id " +
					"	AND deal.start_date = ret.start_date " +
					"	AND deal.user_id = ret.user_id " +
					"LEFT OUTER JOIN list " +
					"	ON	deal.item_id = list.item_id " +
					"	AND deal.start_date = list.start_date " +
					"	AND deal.user_id = list.user_id " +
					"LEFT OUTER JOIN srp " +
					"	ON	deal.item_id = srp.item_id " +
					"	AND deal.start_date = srp.start_date " +
					"	AND deal.user_id = srp.user_id " +
					"ORDER BY deal.start_date DESC " +
					""
					);
			discountData = sql.getDataArray(id, "" +
					"SELECT	ROW_NUMBER() OVER(ORDER BY uom.id), " +
					"		vd.less, " +
					"		vd.per_qty, " +
					"		uom.unit, " +
					"		ch.name, " +
					"		vd.start_date " +
					"FROM	volume_discount AS vd " +
					"INNER JOIN uom " +
					"	on 	uom.id = vd.uom " +
					"INNER JOIN channel AS ch " +
					"	on 	vd.channel_id = ch.id " +
					"WHERE	vd.item_id = ? " +
					"ORDER BY uom.id " +
					""
					);

		}
		ItemHelper helper = new ItemHelper();
		types = helper.getTypes();
		productLines = helper.getFamilies(3);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getShortId() {
		return shortId;
	}

	public void setShortId(String shortId) {
		this.shortId = shortId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getUnspscId() {
		return unspscId;
	}

	public void setUnspscId(long unspscId) {
		this.unspscId = unspscId;
	}

	public boolean isNotDiscounted() {
		return notDiscounted;
	}

	public void setNotDiscounted(boolean notDiscounted) {
		this.notDiscounted = notDiscounted;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String[] getTypes() {
		return types;
	}

	public String[] getProductLines() {
		return productLines;
	}

	public String getProductLine() {
		return productLine;
	}

	public void setProductLine(String productLine) {
		this.productLine = productLine;
	}

	public String[][] getUomHeaders() {
		return uomHeaders;
	}

	public String[][] getPriceHeaders() {
		return priceHeaders;
	}

	public String[][] getDiscountHeaders() {
		return discountHeaders;
	}

	public Object[][] getUomData() {
		return uomData;
	}

	public Object[][] getPriceData() {
		return priceData;
	}

	public Object[][] getDiscountData() {
		return discountData;
	}

	public ArrayList<BOM> getBomList() {
		return bomList;
	}

	public void setBomList(ArrayList<BOM> bomList) {
		this.bomList = bomList;
	}

	public ArrayList<QtyPer> getUomList() {
		return uomList;
	}

	public void setUomList(ArrayList<QtyPer> uomList) {
		this.uomList = uomList;
	}

	public ArrayList<Price> getPriceList() {
		return priceList;
	}

	public void setPriceList(ArrayList<Price> priceList) {
		this.priceList = priceList;
	}

	public ArrayList<VolumeDiscount> getDiscountList() {
		return discountList;
	}

	public void setDiscountList(ArrayList<VolumeDiscount> discountList) {
		this.discountList = discountList;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		new ItemMaster(495);
		Database.getInstance().closeConnection();
	}
}

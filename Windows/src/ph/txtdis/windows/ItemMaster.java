package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class ItemMaster extends Order {

	private boolean isNotDiscounted, isBundled, isPromo, isFree;
	private long unspscId;
	private ArrayList<BOM> bomList;
	private ArrayList<Integer> childIdList;
	private ArrayList<Price> priceList;
	private ArrayList<QtyPerUOM> qtyPerUOMList;
	private ArrayList<VolumeDiscount> volumeDiscountList;
	private BigDecimal purchasePrice, dealerPrice, retailPrice, supermarketPrice, supermarketSRPrice;
	private Date priceStartDate;
	private Object[][] bomData, qtyPerUOMData, priceData, discountData;
	private String itemDescription, itemType, productLine;
	private String[] types, productLines;
	private String[][] bomHeaders, qtyPerUOMHeaders, priceHeaders, discountHeaders;

	public ItemMaster(int id) {
		super();
		this.id = id;
		Data sql = new Data();
		type = "item";
		module = "Item Data";
		ItemHelper helper = new ItemHelper();
		types = helper.getTypes();
		productLines = helper.getFamilies(3);
		// @sql:on
		bomHeaders = new String[][] {
				{StringUtils.center("#", 2), "Line"},
				{StringUtils.center("ID", 6), "ID"},
				{StringUtils.center("NAME", 18), "String"},
				{StringUtils.center("UOM", 5), "String"},
				{StringUtils.center("QUANTITY", 10), "BigDecimal"}
		};
		qtyPerUOMHeaders = new String[][] { 
				{ StringUtils.center("#", 1), "Line" },
		        { StringUtils.center("QUANTITY", 10), "UOM" }, 
		        { StringUtils.center("UOM", 5), "String" },
		        { StringUtils.center("BUY", 6), "Boolean" }, 
		        { StringUtils.center("SELL", 6), "Boolean" },
		        { StringUtils.center("REPORT", 6), "Boolean" } 
		        };
		discountHeaders = new String[][] { 
				{ StringUtils.center("#", 3), "Line" },
		        { StringUtils.center("DISCOUNT", 8), "BigDecimal" }, 
		        { StringUtils.center("PER QTY", 8), "Integer" },
		        { StringUtils.center("UOM", 5), "String" }, 
		        { StringUtils.center("CHANNEL", 18), "String" },
		        { StringUtils.center("SINCE", 10), "Date" } 
		        };
		priceHeaders = new String[][] { 
				{ StringUtils.center("#", 3), "Line" },
		        { StringUtils.center("PURCHASE", 8), "BigDecimal" }, 
		        { StringUtils.center("DEALER", 8), "BigDecimal" },
		        { StringUtils.center("RETAIL", 8), "BigDecimal" }, 
		        { StringUtils.center("SUPERMKT", 8), "BigDecimal" },
		        { StringUtils.center("SUPERSRP", 8), "BigDecimal" }, 
		        { StringUtils.center("SINCE", 10), "Date" },
		        { StringUtils.center("ENCODER", 7), "String" } 
		        };
		// @sql:off
		if (id != 0) {
			Object[] objects = sql.getData(id, ""
					// @sql:on
					+ "SELECT im.short_id,\n" 
					+ "	   im.name,\n" 
					+ "	   iy.name,\n" 
					+ "	   im.unspsc_id,\n" 
					+ "	   im.not_discounted,\n" 
					+ "	   if.name\n" 
					+ "  FROM item_master AS im\n" 
					+ "	   INNER JOIN item_tree AS it ON im.id = it.child_id\n" 
					+ "	   INNER JOIN item_family AS if ON it.parent_id = if.id\n" 
					+ "	   INNER JOIN item_type AS iy ON im.type_id = iy.id\n" 
					+ " WHERE im.id = ?\n" 
					// @sql:off
					);
			if (objects != null) {
				itemName = (String) objects[0];
				itemDescription = (String) objects[1];
				itemType = (String) objects[2];
				types = new String[] { itemType };
				unspscId = objects[3] == null ? 0L : (long) objects[3];
				isNotDiscounted = objects[4] == null ? false : (boolean) objects[4];
				productLine = (String) objects[5];
				productLines = new String[] { productLine };
				bomData = sql.getDataArray(id, ""
						// @sql:on
						+ "SELECT row_number () OVER (ORDER BY bom.part_id),\n" 
						+ "		 bom.part_id,\n" 
						+ "		 im.short_id,\n" 
						+ "		 uom.unit,\n" 
						+ "		 bom.qty\n" 
						+ "	FROM bom\n" 
						+ "		 INNER JOIN item_master AS im ON bom.part_id = im.id\n" 
						+ "		 INNER JOIN uom ON bom.uom = uom.id\n" 
						+ "   WHERE bom.item_id = ?\n" 
						+ "ORDER BY bom.part_id\n" 
						// @sql:off
						);
				qtyPerUOMData = sql.getDataArray(id, ""
						// @sql:on
						+ "SELECT row_number () OVER (ORDER BY uom.id),\n" 
						+ "		 CASE WHEN uom.unit IN ('CS', 'TE') THEN qp.qty ELSE 1 / qp.qty END,\n" 
						+ "		 uom.unit,\n" 
						+ "		 CASE WHEN qp.buy IS NULL THEN FALSE ELSE qp.buy END,\n" 
						+ "		 CASE WHEN qp.sell IS NULL THEN FALSE ELSE qp.sell END,\n" 
						+ "		 CASE WHEN qp.report IS NULL THEN FALSE ELSE qp.report END\n" 
						+ "	FROM uom INNER JOIN qty_per AS qp ON uom.id = qp.uom\n" 
						+ "   WHERE qp.item_id = ?\n" 
						+ "ORDER BY uom.id\n" 
						// @sql:off
						);
				priceData = sql.getDataArray(id, "" 
						// @sql:on
						+ "WITH item AS (SELECT ? AS id),\n" 
						+ "	 purchase AS\n" 
						+ "		 (SELECT item_id,\n" 
						+ "				 price,\n" 
						+ "				 start_date,\n" 
						+ "				 user_id\n" 
						+ "			FROM price INNER JOIN item ON item_id = id\n" 
						+ "		   WHERE tier_id = 0),\n" 
						+ "	 dealer AS\n" 
						+ "		 (SELECT item_id,\n" 
						+ "				 price,\n" 
						+ "				 start_date,\n" 
						+ "				 user_id\n" 
						+ "			FROM price INNER JOIN item ON item_id = id\n" 
						+ "		   WHERE tier_id = 1),\n" 
						+ "	 retail AS\n" 
						+ "		 (SELECT item_id,\n" 
						+ "				 price,\n" 
						+ "				 start_date,\n" 
						+ "				 user_id\n" 
						+ "			FROM price INNER JOIN item ON item_id = id\n" 
						+ "		   WHERE tier_id = 2),\n" 
						+ "	 list AS\n" 
						+ "		 (SELECT item_id,\n" 
						+ "				 price,\n" 
						+ "				 start_date,\n" 
						+ "				 user_id\n" 
						+ "			FROM price INNER JOIN item ON item_id = id\n" 
						+ "		   WHERE tier_id = 3),\n" 
						+ "	 srp AS\n" 
						+ "		 (SELECT item_id,\n" 
						+ "				 price,\n" 
						+ "				 start_date,\n" 
						+ "				 user_id\n" 
						+ "			FROM price INNER JOIN item ON item_id = id\n" 
						+ "		   WHERE tier_id = 4)\n" 
						+ "  SELECT ROW_NUMBER () OVER (ORDER BY dealer.start_date),\n" 
						+ "		 CASE WHEN purchase.price IS NULL THEN 0 ELSE purchase.price END,\n" 
						+ "		 CASE WHEN dealer.price IS NULL THEN 0 ELSE dealer.price END,\n" 
						+ "		 CASE WHEN retail.price IS NULL THEN 0 ELSE retail.price END,\n" 
						+ "		 CASE WHEN list.price IS NULL THEN 0 ELSE list.price END,\n" 
						+ "		 CASE WHEN srp.price IS NULL THEN 0 ELSE srp.price END,\n" 
						+ "		 dealer.start_date,\n" 
						+ "		 upper (dealer.user_id)\n" 
						+ "	FROM item AS i\n" 
						+ "		 LEFT JOIN dealer ON id = dealer.item_id\n" 
						+ "		 LEFT JOIN purchase\n" 
						+ "			 ON 	dealer.item_id = purchase.item_id\n" 
						+ "				AND dealer.start_date = purchase.start_date\n" 
						+ "				AND dealer.user_id = purchase.user_id\n" 
						+ "		 LEFT JOIN retail\n" 
						+ "			 ON 	dealer.item_id = retail.item_id\n" 
						+ "				AND dealer.start_date = retail.start_date\n" 
						+ "				AND dealer.user_id = retail.user_id\n" 
						+ "		 LEFT JOIN list\n" 
						+ "			 ON 	dealer.item_id = list.item_id\n" 
						+ "				AND dealer.start_date = list.start_date\n" 
						+ "				AND dealer.user_id = list.user_id\n" 
						+ "		 LEFT JOIN srp\n" 
						+ "			 ON 	dealer.item_id = srp.item_id\n" 
						+ "				AND dealer.start_date = srp.start_date\n" 
						+ "				AND dealer.user_id = srp.user_id\n" 
						+ "ORDER BY dealer.start_date\n" 
						// @sql:off
						);
				discountData = sql.getDataArray(id, "" 
						// @sql:on
						+ "SELECT row_number () OVER (ORDER BY vd.start_date),\n" 
						+ "		 vd.less,\n" 
						+ "		 vd.per_qty,\n" 
						+ "		 uom.unit,\n" 
						+ "		 ch.name,\n" 
						+ "		 vd.start_date\n" 
						+ "	FROM volume_discount AS vd\n" 
						+ "		 INNER JOIN uom ON uom.id = vd.uom\n" 
						+ "		 INNER JOIN channel AS ch ON vd.channel_id = ch.id\n" 
						+ "   WHERE vd.item_id = ?\n" 
						+ "ORDER BY vd.start_date\n" 
						// @sql:off
						);
			}
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return itemDescription;
	}

	public void setName(String name) {
		this.itemDescription = name;
	}

	public long getUnspscId() {
		return unspscId;
	}

	public void setUnspscId(long unspscId) {
		this.unspscId = unspscId;
	}

	public boolean isNotDiscounted() {
		return isNotDiscounted;
	}

	public void setNotDiscounted(boolean notDiscounted) {
		this.isNotDiscounted = notDiscounted;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
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

	public String[][] getBomHeaders() {
		return bomHeaders;
	}

	public String[][] getUomHeaders() {
		return qtyPerUOMHeaders;
	}

	public String[][] getPriceHeaders() {
		return priceHeaders;
	}

	public String[][] getDiscountHeaders() {
		return discountHeaders;
	}

	public Object[][] getBomData() {
		return bomData;
	}

	public Object[][] getUomData() {
		if (qtyPerUOMData == null)
			qtyPerUOMData = new Object[0][0];
		return qtyPerUOMData;
	}

	public Object[][] getPriceData() {
		return priceData;
	}

	public Object[][] getDiscountData() {
		return discountData;
	}

	public ArrayList<BOM> getBomList() {
		return bomList == null ? new ArrayList<BOM>() : bomList;
	}

	public void setBomList(ArrayList<BOM> bomList) {
		this.bomList = bomList;
	}

	public ArrayList<Integer> getChildIdList() {
		return childIdList == null ? new ArrayList<Integer>() : childIdList;
	}

	public ArrayList<QtyPerUOM> getQtyPerUOMList() {
		return qtyPerUOMList == null ? new ArrayList<QtyPerUOM>() : qtyPerUOMList;
	}

	public void setQtyPerUomList(ArrayList<QtyPerUOM> qtyPerUOMList) {
		this.qtyPerUOMList = qtyPerUOMList;
	}

	public ArrayList<Price> getPriceList() {
		return priceList == null ? new ArrayList<Price>() : priceList;
	}

	public void setPriceList(ArrayList<Price> priceList) {
		this.priceList = priceList;
	}

	public ArrayList<VolumeDiscount> getVolumeDiscountList() {
		return volumeDiscountList == null ? new ArrayList<VolumeDiscount>() : volumeDiscountList;
	}

	public void setDiscountList(ArrayList<VolumeDiscount> discountList) {
		this.volumeDiscountList = discountList;
	}

	public BigDecimal getPurchasePrice() {
		return purchasePrice;
	}

	public void setPurchasePrice(BigDecimal purchasePrice) {
		this.purchasePrice = purchasePrice;
	}

	public BigDecimal getDealerPrice() {
		return dealerPrice;
	}

	public void setDealerPrice(BigDecimal dealerPrice) {
		this.dealerPrice = dealerPrice;
	}

	public BigDecimal getRetailPrice() {
		return retailPrice;
	}

	public void setRetailPrice(BigDecimal retailPrice) {
		this.retailPrice = retailPrice;
	}

	public BigDecimal getSupermarketPrice() {
		return supermarketPrice;
	}

	public void setSupermarketPrice(BigDecimal supermarketPrice) {
		this.supermarketPrice = supermarketPrice;
	}

	public BigDecimal getSupermarketSRPrice() {
		return supermarketSRPrice;
	}

	public void setSupermarketSRPrice(BigDecimal supermarketSRPrice) {
		this.supermarketSRPrice = supermarketSRPrice;
	}

	public Date getPriceStartDate() {
		return priceStartDate;
	}

	public void setPriceStartDate(Date priceStartDate) {
		this.priceStartDate = priceStartDate;
	}

	public boolean isBundled() {
		return isBundled;
	}

	public void setBundled(boolean isBundled) {
		this.isBundled = isBundled;
	}

	public boolean isPromo() {
		return isPromo;
	}

	public void setPromo(boolean isPromo) {
		this.isPromo = isPromo;
	}

	public boolean isFreebie() {
		return isFree;
	}

	public void setFreebie(boolean isFree) {
		this.isFree = isFree;
	}
}

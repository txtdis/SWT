package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class ItemData extends OrderData {

	private boolean isNotDiscounted, isBundled, isPromo, isFree;
	private int itemTypeId;
	private long unspscId;
	private ArrayList<BOM> bomList;
	private ArrayList<Integer> childIdList;
	private ArrayList<Price> priceList;
	private ArrayList<QtyPerUOM> qtyPerUOMList;
	private ArrayList<VolumeDiscount> volumeDiscountList;
	private BigDecimal purchasePrice, dealerPrice, retailPrice, supermarketPrice, supermarketSRPrice;
	private Date priceStartDate;
	private Object[][] bomData, qtyPerUOMData, priceData, discountData;
	private String itemDescription, itemClass, itemType, itemName;
	private String[] itemClasses, itemTypes;
	private String[][] bomHeaders, qtyPerUOMHeaders, priceHeaders, discountHeaders;

	public ItemData(int id) {
		super();
		this.id = id;
		type = Type.ITEM;
		itemClasses = Item.getTypes();
		itemTypes = Item.getFamilies(3);
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
		        { StringUtils.center("PER QTY", 8), "Quantity" },
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
			Object[] objects = sql.getList(id, ""
					// @sql:on
					+ "SELECT h.short_id,\n" 
					+ "	   h.name,\n" 
					+ "	   ty.name,\n" 
					+ "	   h.unspsc_id,\n" 
					+ "	   h.not_discounted,\n" 
					+ "	   CASE WHEN f.name IS NULL THEN m.name ELSE f.name END AS name\n" 
					+ "  FROM item_header AS h\n" 
					+ "	   INNER JOIN item_tree AS tr ON h.id = tr.child_id\n" 
					+ "	   INNER JOIN item_type AS ty ON h.type_id = ty.id\n" 
					+ "	   LEFT JOIN item_family AS f ON tr.parent_id = f.id\n" 
					+ "	   LEFT JOIN monetary AS m ON tr.parent_id = m.id\n" 
					+ " WHERE h.id = ?\n" 
					// @sql:off
					);
			if (objects != null) {
				itemName = (String) objects[0];
				itemDescription = (String) objects[1];
				itemClass = (String) objects[2];
				itemClasses = new String[] { itemClass };
				unspscId = objects[3] == null ? 0L : (long) objects[3];
				isNotDiscounted = objects[4] == null ? false : (boolean) objects[4];
				itemType = (String) objects[5];
				itemTypes = new String[] { itemType };
				bomData = sql.getTableData(id, ""
						// @sql:on
						+ "SELECT row_number () OVER (ORDER BY bom.part_id),\n" 
						+ "		 bom.part_id,\n" 
						+ "		 im.short_id,\n" 
						+ "		 uom.unit,\n" 
						+ "		 bom.qty\n" 
						+ "	FROM bom\n" 
						+ "		 INNER JOIN item_header AS im ON bom.part_id = im.id\n" 
						+ "		 INNER JOIN uom ON bom.uom = uom.id\n" 
						+ "   WHERE bom.item_id = ?\n" 
						+ "ORDER BY bom.part_id\n" 
						// @sql:off
						);
				qtyPerUOMData = sql.getTableData(id, ""
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
				priceData = sql.getTableData(id, "" 
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
				discountData = sql.getTableData(id, "" 
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

	public String getItemClass() {
		return itemClass;
	}

	public void setItemClass(String itemClass) {
		this.itemClass = itemClass;
	}

	public String[] getItemClasses() {
		return itemClasses;
	}

	public String[] getItemTypes() {
		return itemTypes;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getItemType() {
		return itemType;
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

	public boolean isPromo() {
		return isPromo;
	}

	public boolean isFreebie() {
		return isFree;
	}

	public int getItemTypeId() {
	    return itemTypeId;
    }

	public void setItemTypeId(int itemTypeId) {
	    this.itemTypeId = itemTypeId;
    }
}

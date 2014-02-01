package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

public class ReceivingData extends OrderData implements Expirable {
	private ArrayList<Type> qualities;
	private ArrayList<Date> expiries;
	private HashMap<Integer, BigDecimal> itemIdQtyList;

	protected Date expiry;
	protected String[] locations;
	protected Type quality;
	protected int locationId;

	public ReceivingData() {
		super();
		// @sql:on
		tableHeaders = new String[][] {{
			StringUtils.center("#", 3), "Line" }, {
			StringUtils.center("ID", 4), "ID" }, {
			StringUtils.center("PRODUCT NAME", 18), "String" }, {
			StringUtils.center("UOM", 5), "String" }, {
			StringUtils.center("QUALITY", 7), "String" }, {
			StringUtils.center("EXPIRY", 10), "Date" }, {
			StringUtils.center("QUANTITY", 10), "BigDecimal" } };
		// @sql:off

		expiries = new ArrayList<>();
		itemIdQtyList = new HashMap<>();
		locations = new Location().getNames();
		qualities = new ArrayList<>();
	}

	public ReceivingData(int id) {
		this();
		this.id = id;
		isAnRR = true;
		// @sql:on
		tableHeaders = new String[][] {{
				StringUtils.center("#", 3), "Line" }, {
				StringUtils.center("ID", 4), "ID" }, {
				StringUtils.center("PRODUCT NAME", 18), "String" }, {
				StringUtils.center("UOM", 5), "String" }, {
				StringUtils.center("QUALITY", 7), "String" }, {
				StringUtils.center("EXPIRY", 10), "Date" }, {
				StringUtils.center("QUANTITY", 10), "BigDecimal" } };
		// @sql:off
		inputter = Login.user().toUpperCase();

		if (id != 0) {
			// @sql:on
			headerData = sql.getList(id, "" 
					+ "SELECT receiving_date, " 
					+ "		  partner_id, " 
					+ " 	  ref_id,"
					+ "       user_id, "
					+ "       time_stamp\n"
					+ "  FROM receiving_header " 
					+ " WHERE receiving_id = ? ");
			// @sql:off
			if (headerData != null) {
				date = (Date) headerData[0];
				setPartnerId((int) headerData[1]);
				referenceId = headerData[2] == null ? 0 : (int) headerData[2];
				inputter = ((String) headerData[3]).toUpperCase();
				timestamp = ((Timestamp) headerData[4]).getTime();
				inputDate = new Date(timestamp);
				inputTime = new Time(timestamp);
				// @sql:on
				tableData = sql.getTableData(id, "" 
						+ "SELECT rd.line_id, " 
						+ "       rd.item_id, " 
						+ "		  im.short_id, "
						+ "		  u.unit, " 
						+ "		  q.name, "
						+ "		  CASE WHEN rd.expiry IS NULL "
						+ "         THEN '9999-12-31' ELSE rd.expiry END AS expiry, " 
						+ "		  rd.qty "
						+ "  FROM receiving_detail AS rd, " 
						+ "		  item_header AS im, " 
						+ "		  uom AS u, " 
						+ "       quality AS q "
						+ " WHERE 	  rd.item_id = im.id " 
						+ "		  AND rd.uom = u.id " 
						+ "		  AND rd.qc_id = q.id "
						+ "		  AND rd.receiving_id = ? " 
						+ " ORDER BY line_id ");
				// @sql:off
			}
		} else {
			locations = new Location().getNames();
		}
	}

	@Override
    protected void setProperties() {
		type = Type.RECEIVING;
    }

	public ArrayList<Date> getExpiries() {
		return expiries;
	}

	@Override
    public Date getExpiry() {
		return expiry;
	}

	@Override
    public void setExpiry(Date expiry) {
		this.expiry = expiry;
	}

	public HashMap<Integer, BigDecimal> getItemIdQtyList() {
		return itemIdQtyList;
	}

	public int getLocationId() {
		return locationId;
	}

	public void setLocationId(int locationId) {
		this.locationId = locationId;
	}

	public String[] getLocations() {
		return locations;
	}

	public ArrayList<Type> getQualities() {
		return qualities;
	}

	@Override
    public Type getQuality() {
		return quality;
	}

	@Override
    public void setQuality(Type quality) {
		this.quality = quality;
	}

	@Override
	public boolean isEnteredItemQuantityValid(String qty){
		quantity = DIS.parseBigDecimal(qty);
		itemIdQtyList.put(itemId, quantity);
		
		BigDecimal qtyList = itemIdQtyList.get(itemId);
		if (qtyList == null)
			qtyList = BigDecimal.ZERO;
		
		BigDecimal qtyPer = QtyPerUOM.getQty(itemId, uom);
		quantity = quantity.multiply(qtyPer);
		BigDecimal qtyTakenFromReference = BigDecimal.ZERO; //item.getQtyTakenFromReference(itemId, referenceId);
		BigDecimal balance = referenceQuantity.subtract(qtyTakenFromReference).subtract(qtyList);
		
		if(quantity.compareTo(balance) > 0) {
			String remainingQty = DIS.INTEGER.format(balance) + " " + uom;
			BigDecimal fullQty, brokenQty;
			if (!uom.equals("PK")) {
				fullQty = balance.divideToIntegralValue(qtyPer);
				remainingQty = DIS.INTEGER.format(fullQty) + " " + uom; 
				brokenQty = balance.subtract(fullQty.multiply(qtyPer));
				if(brokenQty.compareTo(BigDecimal.ZERO) != 0) {
					remainingQty = remainingQty + " and " + DIS.INTEGER.format(brokenQty) + " PK";
				} 
			}
			new ErrorDialog("Only\n" + remainingQty + "\nremaining");
			return false;
		}
		
		return true;
    }

	@Override
    public void processQuantityInput(String qty, int rowIdx) {
		saveLineItem(itemIds, itemId, rowIdx);
        saveLineItem(qualities, quality, rowIdx);
        saveLineItem(uoms, uom, rowIdx);
        saveLineItem(quantities, DIS.parseBigDecimal(qty), rowIdx);
        saveLineItem(expiries, expiry, rowIdx);
    }
}

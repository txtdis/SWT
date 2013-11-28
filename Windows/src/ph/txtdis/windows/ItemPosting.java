package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

public class ItemPosting extends Posting {
	private final int PURCHASE = 0;
	private final int DEALER = 1;
	private final int RETAIL = 2;
	private final int SUPERMARKET = 3;
	private final int SUPER_SRP = 4;
	
	private ItemMaster item;

	public ItemPosting(Order order) {
		super(order);
		item = (ItemMaster) order;
	}

	@Override
	protected void postData() throws SQLException {
		id = item.getId();
			if (id == 0) {
				// Populate item_master
				ps = conn.prepareStatement("" 
						// @sql:on
						+ "INSERT INTO item_master (short_id, name, unspsc_id, type_id, not_discounted) " 
						+ "					VALUES (?, ?, ?, ?, ?) "
				        + "RETURNING id");
						// @sql:off
				ps.setString(1, item.getShortId());
				ps.setString(2, item.getName());
				ps.setLong(3, item.getUnspscId());
				ps.setInt(4, new ItemHelper().getTypeId(item.getItemType()));
				ps.setBoolean(5, item.isNotDiscounted());
				// Get item ID
				rs = ps.executeQuery();
				if (rs.next())
					id = rs.getInt(1);
				// Populate item_tree
				ps = conn.prepareStatement("" 
						// @sql:on
						+ "INSERT INTO item_tree (child_id, parent_id) "
						+ "               VALUES (?, ?)");
						// @sql:off
				ps.setInt(1, id);
				ps.setInt(2, new ItemHelper().getFamilyId(item.getProductLine()));
				ps.executeUpdate();
				
				// Populate qty_per
				ps = conn.prepareStatement("" 
						// @sql:on
						+ "INSERT INTO qty_per (item_id, qty, uom, buy, sell, report) "
				        + "	            VALUES (?, ?, ?, ?, ?, ?)");
						// @sql:off
				ArrayList<QtyPerUOM> qtyPerUOMList = item.getQtyPerUOMList();
				QtyPerUOM qtyPerUOM;
				BigDecimal qty;
				int uomId;
				int uomListSize = qtyPerUOMList.size();
				for (int i = 0; i < uomListSize; i++) {
					qtyPerUOM = qtyPerUOMList.get(i);
					qty = qtyPerUOM.getQty();
					uomId = qtyPerUOM.getUom();
					// uomId = [3] L, [2] kg 
					if (uomId == 3 || uomId == 2)
						qty = BigDecimal.ONE.divide(qty, BigDecimal.ROUND_HALF_EVEN);
					ps.setInt(1, id);
					ps.setBigDecimal(2, qty);
					ps.setInt(3, uomId);
					ps.setObject(4, qtyPerUOM.isBought() ? true : null);
					ps.setObject(5, qtyPerUOM.isSold() ? true : null);
					ps.setObject(6, qtyPerUOM.isReported() ? true : null);
					ps.executeUpdate();
				}
				// Populate volume_discount
				ps = conn.prepareStatement("" 
						// @sql:on
						+ "INSERT INTO volume_discount (item_id, per_qty, uom, less, channel_id, start_date) " 
						+ "	                    VALUES (?, ?, ?, ?, ?, ?)");
						// @sql:off
				ArrayList<VolumeDiscount> discountList = item.getVolumeDiscountList();
				VolumeDiscount discount;
				int discountListSize = discountList.size();
				for (int i = 0; i < discountListSize; i++) {
					discount = discountList.get(i);
					ps.setInt(1, id);
					ps.setInt(2, discount.getPerQty());
					ps.setInt(3, discount.getUom());
					ps.setBigDecimal(4, discount.getLess());
					ps.setInt(5, discount.getChannelId());
					ps.setDate(6, discount.getDate());
					ps.executeUpdate();
				}
				// Populate BOM
				ps = conn.prepareStatement("" 
						// @sql:on
						+ "INSERT INTO bom (item_id, part_id, qty, uom) " 
						+ "			VALUES (?, ?, ?, ?)");
						// @sql:off
				ArrayList<BOM> bomList = item.getBomList();
				BOM bom;
				int bomListSize = bomList.size();
				for (int i = 0; i < bomListSize; i++) {
					bom = bomList.get(i);
					ps.setInt(1, id);
					ps.setInt(2, bom.getItemId());
					ps.setBigDecimal(3, bom.getQty());
					ps.setInt(4, bom.getUom());
					ps.executeUpdate();
				}
			}
			// Populate priceData
			BigDecimal price = null;
			for (int tierId = 0; tierId < 5; tierId++) {
				switch (tierId) {
					case PURCHASE:
						price = item.getPurchasePrice();
						break;
					case DEALER:
						price = item.getDealerPrice();
						break;
					case RETAIL:
						price = item.getRetailPrice();
						break;
					case SUPERMARKET:
						price = item.getSupermarketPrice();
						break;
					case SUPER_SRP:
						price = item.getSupermarketSRPrice();
						break;
				}
				if (price != null)
					savePrice(tierId, price);
            }
	}

	private void savePrice(int tierId, BigDecimal price) throws SQLException {
	    ps = conn.prepareStatement("" 
	    		// @sql:on
	    		+ "INSERT INTO price (item_id, price, tier_id, start_date) "
	            + "	          VALUES (?, ?, ?, ?)");
	    		// @sql:off
	    	ps.setInt(1, id);
	    	ps.setBigDecimal(2, price);
	    	ps.setInt(3, tierId);
	    	ps.setDate(4, item.getPriceStartDate());
	    	ps.executeUpdate();
    }
}

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

	private ItemData item;

	public ItemPosting(OrderData order) {
		super(order);
		item = (ItemData) order;
	}

	@Override
	protected void postData() throws SQLException {
		id = item.getId();
		if (id == 0) {
			postHeaderData();
			id = getNewId();
			postTreeData();
			postQtyPerData();
			postVolumeDiscountData();
			postBomData();
		}
		postPriceData();
	}

	private void postPriceData() throws SQLException {
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

	private void postBomData() throws SQLException {
	    ps = conn.prepareStatement("INSERT INTO bom (item_id, part_id, qty, uom) VALUES (?, ?, ?, ?)");
	    ArrayList<BOM> bomList = item.getBomList();
	    int bomListSize = bomList.size();
	    for (int i = 0; i < bomListSize; i++) {
	    	BOM bom = bomList.get(i);
	    	ps.setInt(1, id);
	    	ps.setInt(2, bom.getItemId());
	    	ps.setBigDecimal(3, bom.getQty());
	    	ps.setInt(4, UOM.getId(bom.getUom()));
	    	ps.executeUpdate();
	    }
    }

	private void postVolumeDiscountData() throws SQLException {
	    ps = conn.prepareStatement("INSERT INTO volume_discount (item_id, per_qty, uom, less, channel_id, start_date) " 
	    			+ "	VALUES (?, ?, ?, ?, ?, ?)");
	    ArrayList<VolumeDiscount> discountList = item.getVolumeDiscountList();
	    int discountListSize = discountList.size();
	    for (int i = 0; i < discountListSize; i++) {
	    	VolumeDiscount discount = discountList.get(i);
	    	ps.setInt(1, id);
	    	ps.setInt(2, discount.getPerQty());
	    	ps.setInt(3, UOM.getId(discount.getUom()));
	    	ps.setBigDecimal(4, discount.getLess());
	    	ps.setInt(5, discount.getChannelId());
	    	ps.setDate(6, discount.getDate());
	    	ps.executeUpdate();
	    }
    }

	private void postQtyPerData() throws SQLException {
	    ps = conn.prepareStatement("INSERT INTO qty_per (item_id, qty, uom, buy, sell, report) VALUES (?, ?, ?, ?, ?, ?)");
	    ArrayList<QtyPerUOM> qtyPerUOMList = item.getQtyPerUOMList();
	    int uomListSize = qtyPerUOMList.size();
	    for (int i = 0; i < uomListSize; i++) {
	    	QtyPerUOM qtyPerUOM = qtyPerUOMList.get(i);
	    	BigDecimal qty = qtyPerUOM.getQty();
	    	Type uom = qtyPerUOM.getUom();
	    	if (uom == Type.L || uom == Type.KG)
	    		qty = DIS.divide(BigDecimal.ONE, qty);
	    	ps.setInt(1, id);
	    	ps.setBigDecimal(2, qty);
	    	ps.setInt(3, UOM.getId(uom));
	    	ps.setObject(4, qtyPerUOM.isBought() ? true : null);
	    	ps.setObject(5, qtyPerUOM.isSold() ? true : null);
	    	ps.setObject(6, qtyPerUOM.isReported() ? true : null);
	    	ps.executeUpdate();
	    }
    }

	private void postTreeData() throws SQLException {
	    ps = conn.prepareStatement("INSERT INTO item_tree (child_id, parent_id) VALUES (?, ?)");
	    ps.setInt(1, id);
	    ps.setInt(2, item.getItemTypeId());
	    ps.executeUpdate();
    }

	private int getNewId() throws SQLException {
		int id = 0;
	    if (rs.next())
	    	id = rs.getInt(1);
	    return id;
    }

	private void postHeaderData() throws SQLException {
	    ps = conn.prepareStatement("INSERT INTO item_header (short_id, name, unspsc_id, type_id, not_discounted) "
	    			+ "VALUES (?, ?, ?, ?, ?) RETURNING id");
	    ps.setString(1, item.getItemName());
	    ps.setString(2, item.getName());
	    ps.setLong(3, item.getUnspscId());
	    ps.setInt(4, Item.getTypeId(item.getItemClass()));
	    ps.setBoolean(5, item.isNotDiscounted());
	    rs = ps.executeQuery();
    }

	private void savePrice(int tierId, BigDecimal price) throws SQLException {
		ps = conn.prepareStatement("INSERT INTO price (item_id, price, tier_id, start_date) VALUES (?, ?, ?, ?)");
		ps.setInt(1, id);
		ps.setBigDecimal(2, price);
		ps.setInt(3, tierId);
		ps.setDate(4, item.getPriceStartDate());
		ps.executeUpdate();
	}
}

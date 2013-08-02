package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ItemPosting extends Data {
	private PreparedStatement ps;

	public ItemPosting() {
		super();
	}

	public boolean wasDataSaved(ItemMaster im) {
		Connection conn = null;
		ResultSet rs = null;
		int id = im.getId();
		try {
			conn = Database.getInstance().getConnection();
			conn.setAutoCommit(false);
			if (id == 0) {
				// Populate item_master
				ps = conn.prepareStatement("" 
						// @sql:on
						+ "INSERT INTO item_master (short_id, name, unspsc_id, type_id, not_discounted) " 
						+ "					VALUES (?, ?, ?, ?, ?) "
				        + "RETURNING id");
						// @sql:off
				ps.setString(1, im.getShortId());
				ps.setString(2, im.getName());
				ps.setLong(3, im.getUnspscId());
				ps.setInt(4, new ItemHelper().getTypeId(im.getType()));
				ps.setBoolean(5, im.isNotDiscounted());
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
				ps.setInt(2, new ItemHelper().getFamilyId(im.getProductLine()));
				ps.executeUpdate();
				// Populate qty_per
				ps = conn.prepareStatement("" 
						// @sql:on
						+ "INSERT INTO qty_per (item_id, qty, uom, buy, sell, report) "
				        + "	            VALUES (?, ?, ?, ?, ?, ?)");
						// @sql:off
				ArrayList<QtyPerUOM> uomList = im.getUomList();
				QtyPerUOM uom;
				BigDecimal qty;
				int uomId;
				int uomListSize = uomList.size();
				for (int i = 0; i < uomListSize; i++) {
					uom = uomList.get(i);
					qty = uom.getQty();
					uomId = uom.getUom();
					// uomId = [3] L, [2] kg 
					if (uomId == 3 || uomId == 2)
						qty = BigDecimal.ONE.divide(qty, BigDecimal.ROUND_HALF_EVEN);
					ps.setInt(1, id);
					ps.setBigDecimal(2, qty);
					ps.setInt(3, uomId);
					ps.setObject(4, uomList.get(i).isBought() ? true : null);
					ps.setObject(5, uomList.get(i).isSold() ? true : null);
					ps.setObject(6, uomList.get(i).isReported() ? true : null);
					ps.executeUpdate();
				}
				// Populate volume_discount
				ps = conn.prepareStatement("" 
						// @sql:on
						+ "INSERT INTO volume_discount (item_id, per_qty, uom, less, channel_id, start_date) " 
						+ "	                    VALUES (?, ?, ?, ?, ?, ?)");
						// @sql:off
				ArrayList<VolumeDiscount> discountList = im.getDiscountList();
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
				ArrayList<BOM> bomList = im.getBomList();
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
			// Populate price
			ps = conn.prepareStatement("" 
					// @sql:on
					+ "INSERT INTO price (item_id, price, tier_id, start_date) "
			        + "	          VALUES (?, ?, ?, ?)");
					// @sql:off
			ArrayList<Price> priceList = im.getPriceList();
			Price price;
			int priceListSize = priceList.size();
			for (int i = 0; i < priceListSize; i++) {
				price = priceList.get(i);
				ps.setInt(1, id);
				ps.setBigDecimal(2, price.getValue());
				ps.setInt(3, price.getTierId());
				ps.setDate(4, price.getDate());
				ps.executeUpdate();
			}
			im.setId(id);
			conn.commit();
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException er) {
					er.printStackTrace();
					new ErrorDialog(er);
					return false;
				}
			}
			e.printStackTrace();
			new ErrorDialog(e);
			return false;
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				new ErrorDialog(e);
				return false;
			}
		}
		return true;
	}
}

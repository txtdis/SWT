
package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ItemPosting extends SQL {

	public ItemPosting() {
		super();
	}

	public boolean set(ItemMaster im){
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		int id = 0; 
		try {
			conn = Database.getInstance().getConnection();
			conn.setAutoCommit(false);
			// Populate item_master
			ps = conn.prepareStatement("" +
					"INSERT INTO item_master " +
					"	(short_id, name, unspsc_id, type_id, not_discounted) " +
					"	VALUES (?, ?, ?, ?, ?) " +
					"	RETURNING id " +
					"");
			ps.setString(1, im.getShortId());
			ps.setString(2, im.getName());
			ps.setLong(3, im.getUnspscId());
			ps.setInt(4, new ItemHelper().getTypeId(im.getType()));
			ps.setBoolean(5, im.isNotDiscounted());
			// Get item ID
			rs = ps.executeQuery();		
			if (rs.next()) id = rs.getInt(1);
			// Populate item_tree
			ps = conn.prepareStatement("" +
					"INSERT INTO item_tree (child_id, parent_id) VALUES (?, ?)" +
					"");
			ps.setInt(1, id);
			ps.setInt(2, new ItemHelper().getFamilyId(im.getProductLine()));
			ps.executeUpdate();
			// Populate qty_per
			ArrayList<QtyPer> uomList = im.getUomList();
			ps = conn.prepareStatement("" +
					"INSERT INTO qty_per " +
					"	(item_id, qty, uom, buy, sell, report) " +
					"	VALUES (?, ?, ?, ?, ?, ?) " +
					"");
			for (int i = 0; i < uomList.size(); i++) {
				BigDecimal qty = uomList.get(i).getQty();
				int uom = uomList.get(i).getUom();
				if(uom == 3 || uom == 2)
					qty = BigDecimal.ONE.divide(qty);
				ps.setInt(1, id);
				ps.setBigDecimal(2, qty);
				ps.setInt(3, uom);
				ps.setObject(4, uomList.get(i).isBought() ? true : null);
				ps.setObject(5, uomList.get(i).isSold() ? true : null);
				ps.setObject(6, uomList.get(i).isReported() ? true : null);
				ps.executeUpdate();
			}
			// Populate volume_discount
			ps = conn.prepareStatement("" +
					"INSERT INTO volume_discount " +
					"	(item_id, per_qty, uom, less, channel_id, start_date) " +
					"	VALUES (?, ?, ?, ?, ?, ?)"
					);
			ArrayList<VolumeDiscount> discountList = im.getDiscountList();
			for (int i = 0; i < discountList.size(); i++) {
				ps.setInt(1, id);
				ps.setInt(2, discountList.get(i).getPerQty());
				ps.setInt(3, discountList.get(i).getUom());
				ps.setBigDecimal(4, discountList.get(i).getLess());
				ps.setInt(5, discountList.get(i).getChannelId());
				ps.setDate(6, discountList.get(i).getDate());
				ps.executeUpdate();
			}
			// Populate price
			ps = conn.prepareStatement("" +
					"INSERT INTO price (item_id, price, tier_id, start_date) " +
					"	VALUES (?, ?, ?, ?)"
					);
			ArrayList<Price> priceList = im.getPriceList();
			for (int i = 0; i < priceList.size(); i++) {
				ps.setInt(1, id);
				ps.setBigDecimal(2, priceList.get(i).getValue());
				ps.setInt(3, priceList.get(i).getTierId());
				ps.setDate(4, priceList.get(i).getDate());
				ps.executeUpdate();
			}
			// Populate BOM
			ps = conn.prepareStatement("" +
					"INSERT INTO bom (item_id, part_id, qty, uom) " +
					"	VALUES (?, ?, ?, ?)"
					);
			ArrayList<BOM> bomList = im.getBomList();
			for (int i = 0; i < bomList.size(); i++) {
				ps.setInt(1, id);
				ps.setInt(2, bomList.get(i).getItemId());
				ps.setBigDecimal(3, bomList.get(i).getQty());
				ps.setInt(4, bomList.get(i).getUom());
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
				if (rs != null ) rs.close();
				if (ps != null ) ps.close();
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				new ErrorDialog(e);
				return false;
			}
		}
		return true;
	}
}

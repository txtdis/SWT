package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;

import org.eclipse.swt.widgets.TableItem;

public class ItemSaving {
	private ItemView view;
	private ItemMaster im;

	public ItemSaving(ItemView view, ItemMaster im) {
		this.view = view;
		this.im = im;
	}

	public ItemMaster get() {
		try {
			Date date;
			if (im.getId() == 0) {
				im.setShortId(view.getTxtShortId().getText().trim());
				im.setType(view.getCmbType().getText());
				im.setName(view.getTxtName().getText().trim());
				String unspsc = view.getTxtUnspscId().getText().trim();
				im.setUnspscId(unspsc.isEmpty() ? 0 : Long.parseLong(unspsc));
				im.setNotDiscounted(view.getBtnDiscount().getSelection());
				im.setProductLine(view.getCmbProductLine().getText());

				BigDecimal qty;
				TableItem uomItem;
				TableItem[] uomItems = view.getTblUom().getItems();
				ArrayList<QtyPerUOM> uomList = new ArrayList<>();
				int itemSize = uomItems.length;
				//int uomDataSize = im.getUomData().length;
				int uomId;
				boolean isBought, isSold, isReported;
				//for (int i = uomDataSize; i < itemSize; i++) {
				for (int i = 0; i < itemSize; i++) {
					// [1]qty, [2]uom, [3]buy, [4]sell, [5]report
					uomItem = uomItems[i];
					qty = new BigDecimal(uomItems[i].getText(1));
					uomId = new UOM(uomItem.getText(2)).getId();
					isBought = uomItem.getText(3).equals("OK") ? true : false;
					isSold = uomItem.getText(4).equals("OK") ? true : false;
					isReported = uomItem.getText(5).equals("OK") ? true : false;
					uomList.add(new QtyPerUOM(qty, uomId, isBought, isSold, isReported));
				}
				im.setUomList(uomList);

				BigDecimal less;
				TableItem[] discountItems = view.getTblDiscount().getItems();
				ArrayList<VolumeDiscount> discountList = new ArrayList<>();
				int discountItemSize = discountItems.length;
				//int discountDataSize = im.getDiscountData().length;
				int perQty, channelId;
				//for (int i = discountDataSize; i < discountItemSize; i++) {
				for (int i = 0; i < discountItemSize; i++) {
					// [1]less, [2]per_qty, [3]uom, [4]channel_id, [5]date
					less = new BigDecimal(discountItems[i].getText(1));
					perQty = Integer.parseInt(discountItems[i].getText(2));
					uomId = new UOM(discountItems[i].getText(3)).getId();
					channelId = new Channel(discountItems[i].getText(4)).getId();
					date = new Date(DIS.POSTGRES_DATE.parse(discountItems[i].getText(5)).getTime());
					discountList.add(new VolumeDiscount(less, perQty, uomId, channelId, date));
				}
				im.setDiscountList(discountList);
			}

			TableItem[] priceItems = view.getTblPrice().getItems();
			TableItem priceItem = priceItems[priceItems.length - 1];
			ArrayList<Price> priceList = new ArrayList<>();
			// [1]purchase, [2]dealer, [3]retail, [4]mt_list, [5]mt_srp, [6]date
			date = new Date(DIS.POSTGRES_DATE.parse(priceItem.getText(6)).getTime());
			for (int tierId = 0; tierId < 5; tierId++) {
				String price = priceItem.getText(tierId + 1);
				BigDecimal value = BigDecimal.ZERO;
				if (!price.isEmpty()) {
					value = new BigDecimal(price);
					priceList.add(new Price(value, tierId, date));
				}
			}
			im.setPriceList(priceList);
			return im;
		} catch (Exception e) {
			e.printStackTrace();
			new ErrorDialog(e);
			return null;
		}
	}
}

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
			im.setShortId(view.getTxtShortId().getText().trim());
			im.setType(view.getCmbType().getText());
			im.setName(view.getTxtName().getText().trim());
			String unspsc = view.getTxtUnspscId().getText().trim();
			im.setUnspscId(unspsc.isEmpty() ? 0 : Long.parseLong(unspsc));
			im.setNotDiscounted(view.getBtnDiscount().getSelection());
			im.setProductLine(view.getCmbProductLine().getText());

			TableItem[] uomItems = view.getTblUom().getItems(); 
			ArrayList<QtyPer> uomList = new ArrayList<>();
			for (int i = im.getUomData().length; i < uomItems.length; i++) {
				//[1]qty, [2]uom, [3]buy, [4]sell, [5]report
				BigDecimal qty = new BigDecimal(uomItems[i].getText(1));	
				int uom = new UOM(uomItems[i].getText(2)).getId();	
				boolean buy = uomItems[i].getText(3).equals("OK") ? true : false;
				boolean sell = uomItems[i].getText(4).equals("OK") ? true : false;
				boolean report = uomItems[i].getText(5).equals("OK") ? true : false;
				uomList.add(new QtyPer(qty, uom, buy, sell, report));
			}
			im.setUomList(uomList);

			TableItem[] discountItems = view.getTblDiscount().getItems();
			ArrayList<VolumeDiscount> discountList = new ArrayList<>();
			for (int i = im.getDiscountData().length; i < discountItems.length; i++) {
				//[1]less, [2]per_qty, [3]uom, [4]channel_id, [5]date 
				BigDecimal less = new BigDecimal(discountItems[i].getText(1));	
				int perQty = Integer.parseInt(discountItems[i].getText(2));	
				int uom = new UOM(discountItems[i].getText(3)).getId();
				int channelId = new Channel(discountItems[i].getText(4)).getId();
				Date date = new Date(DIS.DF.parse(discountItems[i].getText(5)).getTime());
				discountList.add(new VolumeDiscount(less, perQty, uom, channelId, date));
			}
			im.setDiscountList(discountList);
			
			TableItem[] priceItems = view.getTblPrice().getItems();
			ArrayList<Price> priceList = new ArrayList<>();
			for (int i = im.getDiscountData().length; i < priceItems.length; i++) {
				//[1]purchase, [2]dealer, [3]retail, [4]mt_list, [5]mt_srp, [6]date 
				Date date = new Date(DIS.DF.parse(priceItems[i].getText(6)).getTime());
				for (int tierId = 0; tierId < 5; tierId++) {
					String price = priceItems[i].getText(tierId + 1);
					BigDecimal value = BigDecimal.ZERO;
					if (!price.isEmpty()) {
						value = new BigDecimal(price);
						priceList.add(new Price(value, tierId, date));
					}
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

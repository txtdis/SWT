package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.ArrayList;

public class SalesOrderItemIdEntry extends ItemIdInput {

	public SalesOrderItemIdEntry(OrderView orderView, Order report) {
		super(orderView, report);
	}

	@Override
	protected boolean isNegativeItemIdInputValid() {
		ArrayList<Integer> itemIds = order.getItemIds();
		if (isAtFirstRow) {
			if (itemIds.size() == 1 && !order.isAnRMA()) {
					itemIds.remove(0);
					for (int i = 2; i <= 6; i++) {
						tableItem.setText(i, "");
					}
					order.setComputedTotal(BigDecimal.ZERO);
					order.setFirstLevelDiscountTotal(BigDecimal.ZERO);
					order.setSecondLevelDiscountTotal(BigDecimal.ZERO);
					order.setTotalVatable(BigDecimal.ZERO);
					order.setTotalVat(BigDecimal.ZERO);
			}
			if (itemIds.isEmpty()) {
				// check for open RMA
				int openRmaId = helper.getOpenRmaId(partnerId);
				if (openRmaId != 0) {
					clearTableItemEntry("S/O #" + openRmaId + "\nmust be closed first\nbefore opening a new RMA");
					txtLimit.getShell().dispose();
					new SalesOrderView(openRmaId);
					return false;
				}
				BigDecimal rmaLimit = helper.getRmaLimit(partnerId, order.getDate()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
				order.setEnteredTotal(rmaLimit);
				txtLimit.setText(rmaLimit.toPlainString());
				order.setRMA(true);
			} 
		}  
		if (isOrderNotAnRMA()) {
			return false;
		}
		return true;
	}

	private boolean isOrderNotAnRMA() {
		if (!order.isAnRMA()) {
			clearTableItemEntry("RMA must be\ndone separately");
			return true;
		}
		return false;
	}

	@Override
	protected boolean hasItemBeenEnteredBefore() {
		if (itemId > 0 && order.isAnRMA()) {
			clearTableItemEntry("RMA must be\ndone separately");
			return true;
		}
		return super.hasItemBeenEnteredBefore();
	}

	@Override
	protected boolean isItemOnReferenceOrder() {
		return true;
	}

	@Override
	protected boolean isItemBizUnitSameAsPrevious() {
		if (order.isAnRMA())
			return true;
		return super.isItemBizUnitSameAsPrevious();
	}
}

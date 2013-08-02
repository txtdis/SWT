package ph.txtdis.windows;

import java.math.BigDecimal;

public class SalesOrderItemIdEntry extends OrderItemIdEntry {

	public SalesOrderItemIdEntry(OrderView orderView, Order report) {
		super(orderView, report);
	}

	@Override
	protected boolean isNegativeItemIdInputValid() {
		if (isFirstRow) {
			// check for open RMA
			int openRMA = helper.getOpenRMA(partnerId);
			if (openRMA != 0) {
				new ErrorDialog("S/O #" + openRMA + "\nmust be closed first\nbefore opening a new RMA");
				txtLimit.getShell().dispose();
				new SalesOrderView(openRMA);
				return false;
			}
			BigDecimal rmaLimit = helper.getRmaLimit(partnerId, postDate).setScale(2, BigDecimal.ROUND_HALF_EVEN);
			order.setEnteredTotal(rmaLimit);
			txtLimit.setText(rmaLimit.toPlainString());
		} else if (order.isRMA()) {
			clearEntry("RMA must be\ndone separately");
			return false;
		}
		order.setRMA(true);
		return true;
	}

	@Override
    protected boolean isItemOnReference() {
		// S/Os start paper trail
		return true;
    }	
}

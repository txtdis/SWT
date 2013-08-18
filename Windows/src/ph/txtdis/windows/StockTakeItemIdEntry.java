package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

public class StockTakeItemIdEntry extends ReceivingItemIdEntry {

	public StockTakeItemIdEntry(OrderView orderView, Order report) {
	    super(orderView, report);
    }
	
	@Override
	protected boolean isItemOnReferenceOrder() {
		return true;
	}

	@Override
    protected void setNextTableWidget(BigDecimal price) {
		order.setUoms(new UOM().getSellingUoms(itemId));
		new OrderItemUomCombo(view, order);
    }
}

package ph.txtdis.windows;

import java.math.BigDecimal;

public class CountItemIdEntry extends ReceivingItemIdEntry {

	public CountItemIdEntry(OrderView view, OrderData data) {
	    super(view, data);
    }
	
	@Override
    protected boolean isStockInputValid() {
		return Item.isPhysical(itemId);
    }

	@Override
	protected boolean isItemOnReferenceOrder() {
		return true;
	}

	@Override
    protected void setNextTableWidget(BigDecimal price) {
		data.setUnitsOfMeasure(UOM.getSellingUoms(itemId));
		new OrderItemUomCombo(view, data);
    }
}

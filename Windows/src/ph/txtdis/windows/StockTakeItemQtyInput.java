package ph.txtdis.windows;

public class StockTakeItemQtyInput extends OrderItemQtyInput {
	protected ReceivingView receivingView;
	protected Receiving receiving;
	
	public StockTakeItemQtyInput(final ReceivingView view, final Receiving order) {
		super(view, order);
		receivingView = view;
		receiving = order;
	}

	@Override
    protected boolean isQtyInputValid() {
		isReferenceQtyEnough(receiving);
		tableItem.setText(6, textInput);
		qtyInput.dispose();

        order.saveLineItem(order.getItemIds(), order.getItemId(), rowIdx);
        order.saveLineItem(receiving.getQualityStates(), receiving.getQualityState(), rowIdx);
        order.saveLineItem(order.getUomIds(), order.getUomId(), rowIdx);
        order.saveLineItem(order.getQtys(), quantity, rowIdx);
        order.saveLineItem(receiving.getExpiries(), receiving.getExpiry(), rowIdx);
		orderView.getPostButton().setEnabled(true);
		return true;
    }

	protected boolean isReferenceQtyEnough(Receiving receiving) {
		return true;
    }
}

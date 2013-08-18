package ph.txtdis.windows;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.TableItem;

public class OrderItemQualitySelector {

	public OrderItemQualitySelector(final ReceivingView view, final Receiving order) {
		final TableItem tableItem = view.getTableItem();
		final Combo qualityCombo = new TableCombo(tableItem, 4, new Quality().getStates()).getCombo();
		qualityCombo.setText(tableItem.getText(4));
		qualityCombo.setEnabled(true);
		qualityCombo.setFocus();
		new ComboSelector(qualityCombo, view.getExpiryInput()) {
			@Override
			protected void doAfterSelection() {
				order.setQualityState(selection);
				qualityCombo.dispose();
				tableItem.setText(4, selection);
				new OrderItemExpiryInput(view, order);
			}
		};
	}

}

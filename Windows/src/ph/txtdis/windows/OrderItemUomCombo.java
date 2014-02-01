package ph.txtdis.windows;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.TableItem;

public class OrderItemUomCombo {

	private Combo uomCombo;
	private TableItem tableItem;

	public OrderItemUomCombo(final OrderView view, final OrderData data) {
		tableItem = view.getTableItem();
		uomCombo = new TableCombo(tableItem, OrderView.UOM_COLUMN, data.getUomList()).getCombo();
		uomCombo.setFocus();

		new ComboSelector(uomCombo, null) { 
			@Override
			protected void processSelection() {
				tableItem.setText(OrderView.UOM_COLUMN, selection);
				uomCombo.dispose();
				data.setUom(Type.valueOf(selection));
				view.processUomSelection(selection);
			}
		};
	}
}

package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ItemQtyInput {
	public ItemQtyInput(final OrderView view, final OrderData data) {
		final TableItem tableItem = view.getTableItem();
		final Text qtyInput = new TableTextInput(tableItem, view.getQtyColumnIdx(), BigDecimal.ZERO).getText();
		qtyInput.setFocus();
		new DataInputter(qtyInput, null) {
			@Override
			protected Boolean isPositive() {
				if(!data.isEnteredItemQuantityValid(textInput))
					return false;
				qtyInput.dispose();
				int rowIdx = view.getRowIdx();
				data.processQuantityInput(textInput, rowIdx);
				view.setRowIdx(++rowIdx);
				view.processQuantityInput(textInput, rowIdx);
				return true;
			}
		};
	}	
}

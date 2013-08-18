package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class OrderItemExpiryInput {

	public OrderItemExpiryInput(final ReceivingView view, final Receiving order) {
		final String qualityState = order.getQualityState();
		Date date = qualityState.equals("BAD") ? DIS.TODAY : DIS.TOMORROW;
		final TableItem tableItem = view.getTableItem();
		final Text expiryInput = new TableTextInput(tableItem, order.getRowIdx(), 5, date).getText();
		final ReceivingView receivingView = view;
		expiryInput.setFocus();
		
		new DateInputter(expiryInput, view.getQtyInput()) {
			@Override
			protected boolean isTheDataInputValid() {
				if ((qualityState.equals("GOOD") || qualityState.equals("ON-HOLD")) && !date.after(order.getDate())) {
					new ErrorDialog("Good/on-hold items\ncannot be expired");
					return false;
				} else {
					tableItem.setText(5, textInput);
					order.setExpiry(date);
					expiryInput.dispose();
					new ItemQtyInput(receivingView, order);
					return true;
				}
			}
		};
	}
}

package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class OrderItemExpiryInput {
	private ReceivingView receivingView;
	private String qualityState;
	private TableItem tableItem;
	private Text expiryInput; 

	public OrderItemExpiryInput(ReceivingView view, final Receiving order) {
		receivingView = view;
		qualityState = order.getQualityState();
		tableItem = view.getTableItem();
		Date date = qualityState.equals("BAD") ? DIS.TODAY : DIS.TOMORROW;
		expiryInput = new TableTextInput(tableItem, order.getRowIdx(), 5, date).getText();
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
					order.setQtyColumnNo(6);
					new ItemQtyInput(receivingView, order);
					return true;
				}
			}
		};
	}
}

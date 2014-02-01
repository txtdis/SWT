package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class EnteredTotalAmountInputListener {
	private Text referenceIdInput, enteredTotalInput, partnerIdInput;
	private BigDecimal actual;
	private Button listButton, postButton;
	private DeliveryData data;
	private DeliveryView view;

	public EnteredTotalAmountInputListener(DeliveryView v, DeliveryData d) {
		view = v;
		data = d;
		referenceIdInput = view.getReferenceIdInput();
		enteredTotalInput = view.getEnteredTotalInput();
		partnerIdInput = view.getPartnerIdInput();
		listButton = view.getListButton();
		postButton = view.getPostButton();
		
		new DataInputter(enteredTotalInput, partnerIdInput) {
			
		};

		enteredTotalInput.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				String strActual = enteredTotalInput.getText().trim().replace(",", "");
				if (StringUtils.isBlank(strActual))
					return;
				actual = new BigDecimal(strActual);
				// save actual total
				BigDecimal sumTotal = ((OrderData) data).getComputedTotal();
				boolean isSpecial = Channel.isSpecial(data.getPartnerId());
				Type type = data.getType();
				if (actual.equals(BigDecimal.ZERO) && type != Type.DELIVERY && !isSpecial) {
					if (type == Type.INVOICE && sumTotal.equals(BigDecimal.ZERO)) {
						int lastId = data.getId() - 1;
						Date lastDate = OrderControl.getDate(lastId);
						data.setDate(lastDate);
						postButton.setEnabled(true);
					} else {
						enteredTotalInput.setText("");
						return;
					}
				} else {
					if (referenceIdInput.getText().trim().isEmpty() || ((OrderData) data).isForAnExTruck()) {
						// go to partner ID input
						enteredTotalInput.setTouchEnabled(false);
						listButton.setEnabled(true);
						partnerIdInput.setTouchEnabled(true);
						partnerIdInput.setFocus();
					} else {
						if (!isSpecial && sumTotal.subtract(actual).abs().compareTo(BigDecimal.ONE) > 0) {
							new ErrorDialog("Difference between\nencoded total amount\n "
							        + "versus system generated\nmust be within " + DIS.$ + "1.00");
							enteredTotalInput.setText("");
							enteredTotalInput.setEditable(true);
							enteredTotalInput.setBackground(UI.YELLOW);
							enteredTotalInput.setFocus();
							return;
						} else {
							postButton.setEnabled(true);
							postButton.setFocus();
						}
					}
				}
				((OrderData) data).setEnteredTotal(actual);
			}
		});
	}
}
package ph.txtdis.windows;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class OrderActualAmountEntry {
	private Text txtRefId, txtActual, txtPartnerId;
	private BigDecimal actual;
	private Button btnList, btnPost;

	public OrderActualAmountEntry(final OrderView view, final Order order) {
		txtRefId = view.getReferenceIdInput();
		txtActual = view.getTxtEnteredTotal();
		txtPartnerId = view.getTxtPartnerId();
		btnList = view.getListButton();
		btnPost = view.getPostButton();

		new DecimalVerifier(txtActual);
		txtActual.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				String strActual = txtActual.getText().trim().replace(",", "");
				if (StringUtils.isBlank(strActual))
					return;
				actual = new BigDecimal(strActual);
				// save actual total
				BigDecimal sumTotal = order.getComputedTotal();
				boolean isSpecialCustomer = new Customer().isInternalOrOthers(order.getPartnerId());
				String module = order.getModule();
				if (actual.equals(BigDecimal.ZERO) && !module.equals("Delivery Report") && !isSpecialCustomer) {
					if (module.equals("Invoice") && sumTotal.equals(BigDecimal.ZERO)) {
						btnPost.setEnabled(true);
						btnPost.setFocus();
					} else {
						txtActual.setText("");
						return;
					}
				} else {
					if (txtRefId.getText().trim().isEmpty() || order.isForAnExTruck()) {
						// go to partner ID input
						txtActual.setTouchEnabled(false);
						btnList.setEnabled(true);
						txtPartnerId.setTouchEnabled(true);
						txtPartnerId.setFocus();
					} else {
						if (!isSpecialCustomer && sumTotal.subtract(actual).abs().compareTo(BigDecimal.ONE) > 0) {
							new ErrorDialog("Difference between\nencoded total amount\n "
							        + "versus system generated\nmust be within " + DIS.CURRENCY_SIGN + "1.00");
							txtActual.setText("");
							txtActual.setEditable(true);
							txtActual.setBackground(DIS.YELLOW);
							txtActual.setFocus();
							return;
						} else {
							btnPost.setEnabled(true);
							btnPost.setFocus();
						}
					}
				}
				order.setEnteredTotal(actual);
			}
		});
	}
}
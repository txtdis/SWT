package ph.txtdis.windows;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class OrderActualAmountEntry{
	private Text txtRefId, txtActual, txtPartnerId, txtSumTotal;
	private BigDecimal actual;
	private Button btnList, btnPost;

	public OrderActualAmountEntry(final OrderView view, final Order order) {
		txtRefId = view.getTxtSoId();
		txtActual = view.getTxtActual();
		txtPartnerId = view.getTxtPartnerId();
		txtSumTotal = view.getTxtSumTotal();
		btnList = view.getBtnList();
		btnPost = view.getBtnPost();

		new DecimalVerifier(txtActual);
		txtActual.addListener (SWT.DefaultSelection, new Listener () {
			@Override
			public void handleEvent (Event e) {
				String strActual = txtActual.getText().trim().replace(",", "");
				if (StringUtils.isBlank(strActual)) return; 
				actual = new BigDecimal(strActual);
				// save actual total
				String strSumTotal = txtSumTotal.getText().trim().replace(",", "");
				BigDecimal sumTotal = (strSumTotal.isEmpty() ? 
						BigDecimal.ZERO : new BigDecimal(strSumTotal));
				order.setActual(actual);
				String module = order.getModule();
				if(actual.equals(BigDecimal.ZERO) && !module.equals("Delivery Report")) {
					if(module.equals("Invoice") && sumTotal.equals(BigDecimal.ZERO)) {
						btnPost.setEnabled(true);
						btnPost.setFocus();
					} else {
						txtActual.setText("");
						return;
					}
				} else {
					if (txtRefId.getText().trim().isEmpty()){
						// go to partner ID input
						txtActual.setTouchEnabled(false);
						btnList.setEnabled(true);
						txtPartnerId.setTouchEnabled(true);
						txtPartnerId.setFocus();
					} else {
						if(sumTotal.subtract(actual).abs().compareTo(BigDecimal.ONE) > 0){
							new ErrorDialog("Difference between\n" +
									"Encoded Total Amount\n " +
									"versus System Generated\n" +
									"must be within one(1)");
							txtActual.setText("");
							txtActual.setEditable(true);
							txtActual.setBackground(View.yellow());
							txtActual.setFocus();
							return;	
						} else {
							btnPost.setEnabled(true);
							btnPost.setFocus();
						}
					}
				}
			}
		});
	}
}
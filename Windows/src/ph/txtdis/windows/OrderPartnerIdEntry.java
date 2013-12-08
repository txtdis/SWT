package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class OrderPartnerIdEntry {
	private Text txtAddress, txtPartnerId, txtPartner, txtDate, txtDueDate;
	private Date postDate;
	private int partnerId, creditTerm;
	private String strPartnerId;
	private Button btnList;
	private BigDecimal actual;

	public OrderPartnerIdEntry(OrderView view, final Order order) {
		txtPartnerId = view.getTxtPartnerId();
		txtPartner = view.getTxtPartnerName();
		txtDate = view.getTxtPostDate();
		txtDueDate = view.getTxtDueDate();
		txtAddress = view.getAddressDisplay();
		btnList = view.getListButton();

		new IntegerVerifier(txtPartnerId);
		txtPartnerId.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event ev) {
				postDate = order.getDate();
				actual = order.getEnteredTotal();
				strPartnerId = txtPartnerId.getText().trim();
				if (StringUtils.isBlank(strPartnerId))
					return;
				partnerId = Integer.parseInt(strPartnerId);
				order.setPartnerId(partnerId);
				String name = order.getPartner();
				if (name.isEmpty()) {
					clearInput("Customer #" + partnerId + "\nis not on file.");
					return;
				}
				String route = order.getRoute();
				int refId = order.getReferenceId();
				String abbr = refId < 0 ? "P/O" : "S/O";
				if (order.isFromAnExTruck() && !order.isPartnerFromAnExTruckRoute()) {
					clearInput(name + "\nbelongs to " + route + "\nbut " + abbr + " #" + refId
					        + " is for an EX-TRUCK route");
					return;
				}

				// Ensure only internal and other channels are not payment-tracked
				if (order.isA_DR() && actual.equals(BigDecimal.ZERO) && !order.isForInternalCustomerOrOthers()) {
					clearInput("Only internal-customer and other\ntransactions do not involve payment");
					return;
				}

				if (order.isAnSO() || (order.isAnSI() && order.isForAnExTruck())) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							BigDecimal overdue = new Overdue(partnerId).getBalance();
							order.setOverdue(overdue);
						}
					}).start();
				}

				txtPartner.setText(name);
				
				creditTerm = new Credit().getTerm(partnerId, postDate);
				order.setLeadTime(creditTerm);
				txtDueDate.setText(new DateAdder(txtDate.getText()).add(creditTerm));
				
				txtAddress.setText(new Address(partnerId).getAddress());
				
				txtPartnerId.setTouchEnabled(false);
				btnList.setEnabled(false);
				
				txtDate.setTouchEnabled(true);
				txtDate.setFocus();
			}
		});
	}

	private void clearInput(String msg) {
		new ErrorDialog(msg);
		txtPartnerId.setText("");
		txtPartnerId.setFocus();
		txtPartnerId.setEditable(true);
		txtPartnerId.setBackground(DIS.YELLOW);
		txtPartnerId.selectAll();
	}
}

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
	private Text txtAddress, txtPartnerId, txtPartnerName, txtPostDate, txtDueDate;
	private Date postDate;
	private int partnerId, creditTerm;
	private String strPartnerId;
	private Button btnList;
	private BigDecimal actual;

	public OrderPartnerIdEntry(OrderView view, final Order order) {
		txtPartnerId = view.getTxtPartnerId();
		txtPartnerName = view.getTxtPartnerName();
		txtPostDate = view.getTxtPostDate();
		txtDueDate = view.getTxtDueDate();
		txtAddress = view.getTxtAddress();
		btnList = view.getBtnList();

		new IntegerVerifier(txtPartnerId);
		txtPartnerId.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event ev) {
				postDate = order.getPostDate();
				actual = order.getEnteredTotal();
				strPartnerId = txtPartnerId.getText().trim();
				if (StringUtils.isBlank(strPartnerId))
					return;
				partnerId = Integer.parseInt(strPartnerId);
				order.setPartnerId(partnerId);
				String name = order.getPartner();
				if (name == null) {
					clearInput("Customer #" + partnerId + "\nis not on file.");
					return;
				}
				
				String route = order.getRoute();
				int refId = order.getSoId();
				String abbr = refId < 0 ? "P/O" : "S/O";
				if (order.isFromExTruckRoute() && !route.contains("TRUCK")) {
					clearInput(name + "\nbelongs to " + route + "\nbut " + abbr + " #" + refId + " is for an EX-TRUCK route");
					return;
				}
				
				// Ensure only internal and other channels are not payment-tracked
				if (order.isDR() && actual.equals(BigDecimal.ZERO) && !order.isForInternalCustomerOrOthers()) {
					clearInput("Only internal-customer and other\ntransactions do not involve payment");
					return;
				}
				
				if (order.isSO()) {
					if (order.isForAnExTruck()) {
						new InfoDialog("NOTE WELL:\nOnly one Ex-Truck S/O\nper day is allowed.");
					} else {
						new InfoDialog("NOTE WELL:\nOnly one S/O per discount rate\nper outlet per day\nis allowed.");
					}
					new Thread(new Runnable() {
						@Override
						public void run() {
							BigDecimal overdue = new Overdue(partnerId, DIS.OVERDUE_CUTOFF).getBalance();
							order.setOverdue(overdue);						
						}
					}).start();
				}

				// show name
				txtPartnerName.setText(name);
				// show credit term on date due
				creditTerm = new Credit().getTerm(partnerId, postDate);
				order.setLeadTime(creditTerm);
				txtDueDate.setText(new DateAdder(txtPostDate.getText()).add(creditTerm));
				// show address
				txtAddress.setText(new Address(partnerId).getAddress());
				// disable partner ID input
				txtPartnerId.setTouchEnabled(false);
				btnList.setEnabled(false);
				// go to invoice date
				txtPostDate.setTouchEnabled(true);
				txtPostDate.setFocus();
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

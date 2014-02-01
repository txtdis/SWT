package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

public class OrderPartnerIdEntry {
	private Text addressDisplay, partnerIdInput, partnerDisplay, dateInput, dueDisplay;
	private int partnerId, creditTerm;
	private Button listButton;
	private BigDecimal actual;

	public OrderPartnerIdEntry(OrderView view, final OrderData data) {
		partnerIdInput = view.getPartnerIdInput();
		partnerDisplay = view.getPartnerDisplay();
		dateInput = view.getDateInput();
		dueDisplay = view.getDueDisplay();
		addressDisplay = view.getAddressDisplay();
		listButton = view.getListButton();
		
		new DataInputter(partnerIdInput, dateInput) {
			@Override
            protected Boolean isPositive() {
				partnerId = number.intValue();
				data.setPartnerId(partnerId);
				String name = data.getPartner();
				if (name == null) {
					new ErrorDialog("Customer #" + partnerId + "\nis not on file.");
					return false;
				}
				
				creditTerm = Credit.getTerm(partnerId, DIS.TODAY);
				String route = data.getRoute();
				int refId = data.getReferenceId();
				String abbr = refId < 0 ? "P/O" : "S/O";
				if (data.isFromAnExTruck()) {
					if (!data.isPartnerFromAnExTruckRoute()) {
						clearInput(name + "\nbelongs to " + route + "\nbut " + abbr + " #" + refId
								+ " is for an EX-TRUCK route");
						return false;
					} else if (creditTerm > 0) {
						clearInput("Outlets with credit terms\nmust have separate S/O's");
						return false;						
					}
				}

				if (isAnOwnerOrOtherTransactionNotToBePaid(data)) {
					clearInput("Only owner-related and other miscellaneous\ntransactions do not involve payment");
					return false;
				}

				partnerDisplay.setText(name);

				data.setLeadTime(creditTerm);
				dueDisplay.setText(DIS.addDays(DIS.parseDate(dateInput.getText()), creditTerm).toString());
				addressDisplay.setText(new Address(partnerId).getAddress());

				listButton.setEnabled(false);
				return true;
            }

			private boolean isAnOwnerOrOtherTransactionNotToBePaid(OrderData order) {
	            return order.isA_DR() && actual.equals(BigDecimal.ZERO) && !order.isForInternalCustomerOrOthers();
            }
		};
	}

	private void clearInput(String msg) {
		new ErrorDialog(msg);
		partnerIdInput.setText("");
		partnerIdInput.setFocus();
		partnerIdInput.setEditable(true);
		partnerIdInput.setBackground(UI.YELLOW);
		partnerIdInput.selectAll();
	}
}

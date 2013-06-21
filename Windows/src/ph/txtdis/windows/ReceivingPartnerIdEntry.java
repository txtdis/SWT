package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class ReceivingPartnerIdEntry {
	private Text txtAddress, txtPartnerId, txtPartnerName, txtDate;
	private int partnerId, oldPartnerId;
	private String strPartnerId;
	private Button btnList;

	public ReceivingPartnerIdEntry(ReceivingView view, final Receiving order) {
		txtPartnerId = view.getTxtPartnerId();
		txtPartnerName = view.getTxtPartnerName();
		txtDate = view.getTxtDate();
		txtAddress = view.getTxtAddress();
		btnList = view.getBtnList();

		new IntegerVerifier(txtPartnerId);
		Listener listener = new Listener () {
			@Override
			public void handleEvent (Event ev) {
				strPartnerId = txtPartnerId.getText().trim();
				btnList.setEnabled(true);
				switch (ev.type) {
				case SWT.MouseDoubleClick:
					if(order.getRrId() == 0) break; 
					txtPartnerName.setText("");
					txtAddress.setText("");
					txtPartnerId.setTouchEnabled(true);
					txtPartnerId.setFocus();
					txtPartnerId.setEditable(true);
					txtPartnerId.setBackground(View.yellow());
					txtPartnerId.selectAll();
					break;
				case SWT.DefaultSelection:
					if (!StringUtils.isBlank(strPartnerId)) {
						// retrieve name from id input
						oldPartnerId = order.getPartnerId();
						partnerId = Integer.parseInt(strPartnerId);
						if(partnerId != oldPartnerId) {
							String name = new CustomerHelper(partnerId).getName();
							if (name == null)  {
								// pop error dialog 
								new ErrorDialog("Sorry, Customer ID " + partnerId + "\nis not in our system.");
								// blank text input
								txtPartnerId.setText("");
								txtPartnerId.setFocus();
								txtPartnerId.setEditable(true);
								txtPartnerId.setBackground(View.yellow());
								txtPartnerId.selectAll();
							} else {						
								// save partner id
								order.setPartnerId(partnerId);
								// show name
								txtPartnerName.setText(name);
								// show address
								txtAddress.setText(new Address(partnerId).getAddress());
								// disable partner ID input
								//txtPartnerId.setTouchEnabled(false);
								// go to invoice date
								txtDate.setTouchEnabled(true);
								txtDate.setFocus(); 
							}
						} else {
							// go to receiving date
							txtDate.setTouchEnabled(true);
							txtDate.setFocus();
						}
					}
					break;
				default:
					//txtPartnerId.setTouchEnabled(false);
				}
			}
		};
		txtPartnerId.addListener (SWT.DefaultSelection, listener);
		txtPartnerId.addListener (SWT.MouseDoubleClick, listener);
	}
}

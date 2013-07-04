package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class ReceivingRefIdEntry {
	private ReceivingView view;
	private Receiving order;
	private Text txtRefId, txtItemId;

	public ReceivingRefIdEntry(ReceivingView receivingView, Receiving receiving) {
		view = receivingView;
		order = receiving;
		txtRefId = view.getTxtRefId();

		new IntegerVerifier(txtRefId);
		txtRefId.addListener(SWT.DefaultSelection, new Listener() {
			private String strRefId;
			private int refId;

			@Override
			public void handleEvent(Event event) {
				txtItemId = view.getTxtItemId();
				txtRefId = view.getTxtRefId();
				strRefId = txtRefId.getText().trim();
				if (!strRefId.isEmpty()) {
					CustomerHelper customer = new CustomerHelper();
					String partner = view.getTxtPartnerId().getText().trim();
					refId = Integer.parseInt(strRefId);
					int partnerIdInView = Integer.parseInt(partner);
					boolean isVendor = customer.isVendor(partnerIdInView);
					boolean isPO = refId < 0;
					boolean isVendorOrPO = isVendor || isPO;
					String orderType = isVendorOrPO ? "Purchase" : "Sales";
					order.setType(orderType);
					int partnerIdOnOrder = new OrderHelper().getPartnerId(Math.abs(refId), orderType);
					String partnerName = customer.getName(partnerIdInView);
					boolean isOrderOnFile = partnerIdInView == partnerIdOnOrder;
					if (!isOrderOnFile) {
						new ErrorDialog(orderType + " #" + refId + " for\n" + partnerName
								+ "\nis not in our system");
					} else {
						order.setRefId(refId);
						txtRefId.setTouchEnabled(false);
						if (txtItemId == null) {
							view.setTxtItemId(new ReceivingLineItem(view,
									order, order.getItemIds().size())
									.getTxtItemId());
							txtRefId.setText("");
							txtRefId.setTouchEnabled(true);
							txtRefId.setFocus();
						} else {
							txtItemId.setTouchEnabled(true);
							txtItemId.setFocus();
						}
					}
				}
			}
		});
	}
}
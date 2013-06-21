package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;
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
		txtRefId.addListener (SWT.DefaultSelection, new Listener () {
			private String strRefId;
			private int refId, oldRefId;

			@Override
			public void handleEvent (Event event) {
				txtItemId = view.getTxtItemId();
				txtRefId = view.getTxtRefId();
				strRefId = txtRefId.getText().trim();
				if (!StringUtils.isBlank(strRefId)) {
					refId = Integer.parseInt(strRefId);
					oldRefId = order.getRefId();
					if(refId != oldRefId) 
						order.setRefId(refId);
					
					txtRefId.setTouchEnabled(false);
					if(txtItemId == null) {
						view.setTxtItemId(new ReceivingLineItem(
								view, 
								order, 
								order.getItemIds().size()
								).getTxtItemId());
					} else {
						txtItemId.setTouchEnabled(true);
						txtItemId.setFocus();
					}
				}
			}
		});
	}
}
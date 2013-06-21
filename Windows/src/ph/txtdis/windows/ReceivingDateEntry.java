package ph.txtdis.windows;

import java.sql.Date;
import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class ReceivingDateEntry {
	private ReceivingView view;
	private Receiving order;
	private Text txtDate, txtRefId;
	private int refId, partnerId;

	public ReceivingDateEntry(ReceivingView receivingView, Receiving receiving) {
		view = receivingView;
		order = receiving;
		txtDate = view.getTxtDate();
		txtRefId = view.getTxtRefId();
		String strRefId = txtRefId.getText().trim();
		if (!strRefId.isEmpty())
			refId = Integer.parseInt(strRefId);
		String strPartnerId = view.getTxtPartnerId().getText().trim();
		if (!strPartnerId.isEmpty())
			partnerId = Integer.parseInt(strPartnerId);

		txtDate.addListener (SWT.DefaultSelection, new Listener() {
			private String strPostDate;
			private Date postDate;

			@Override
			public void handleEvent (Event event) {
				txtDate = view.getTxtDate();
				strPostDate = txtDate.getText().trim(); 
				try {							
					postDate = new Date(DIS.DF.parse(strPostDate).getTime());
					ReceivingHelper rrHelper = new ReceivingHelper();
					boolean isPO = new CustomerHelper().isVendor(partnerId) || refId < 0;
					boolean hasOpenPO = rrHelper.hasOpenPO(postDate, partnerId);
					if (!isPO || (isPO && hasOpenPO)){
						txtDate.setTouchEnabled(false);
						txtRefId.setTouchEnabled(true);
						txtRefId.setFocus();
					} else {
						new ErrorDialog("" +
								"There are no open P/O's\n" +
								"for " + postDate);
						txtDate.setText(order.getDate().toString());
						txtDate.setEditable(true);
						txtDate.setBackground(View.yellow());
						txtDate.selectAll();
					}
				} catch (ParseException e) {
					new ErrorDialog(e);
				}
			}
		});

	}
}
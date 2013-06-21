package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class ReceivingQcSelection {
	private ReceivingLineItem lineItem;
	private Combo cmbQc;
	private Text txtQty;

	public ReceivingQcSelection(
			ReceivingView view,
			ReceivingLineItem receivingLineItem, 
			Receiving receiving,
			int line) {
		lineItem = receivingLineItem;
		txtQty = lineItem.getTxtQty();
		cmbQc = lineItem.getCmbQc();

		cmbQc.addListener (SWT.DefaultSelection, new Listener () {
			@Override
			public void handleEvent (Event ev) {
				txtQty.setTouchEnabled(true);
				txtQty.setFocus();
			}
		});
	}
}

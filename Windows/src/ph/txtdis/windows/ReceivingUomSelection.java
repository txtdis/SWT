package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class ReceivingUomSelection {
	private Receiving order;
	private ReceivingLineItem lineItem;
	private Combo cmbUom, cmbQc;

	public ReceivingUomSelection(
			ReceivingView view,
			ReceivingLineItem receivingLineItem, 
			Receiving receiving,
			int line) {
		order = receiving;
		lineItem = receivingLineItem;
		cmbQc = lineItem.getCmbQc();
		cmbUom = lineItem.getCmbUom();
		
		cmbUom.addListener (SWT.DefaultSelection, new Listener () {
			@Override
			public void handleEvent (Event ev) {
				cmbQc.setEnabled(true);
				cmbQc.setFocus();
				cmbQc.setItems(order.getQcStates());
				cmbQc.select(0);
			}
		});
	}
}

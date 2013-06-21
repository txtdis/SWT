package ph.txtdis.windows;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ReceivingItemQtyEntry {
	private ReceivingView view;
	private ReceivingLineItem lineItem;
	private Receiving order;
	private Text txtItemId, txtQty;
	private Button btnPost;
	private Combo cmbUom;
	private TableItem tableItem;
	private BigDecimal qty; 
	private int row, uom;

	public ReceivingItemQtyEntry(
			ReceivingView receivingView,
			ReceivingLineItem receivingLineItem, 
			Receiving receiving,
			int line) {
		view = receivingView;
		order = receiving;
		lineItem = receivingLineItem;
		row = line;
		btnPost = view.getBtnPost();
		tableItem = lineItem.getTableItem();
		txtItemId = lineItem.getTxtItemId();
		txtQty = lineItem.getTxtQty();

		new DecimalVerifier(txtQty);
		txtQty.addListener (SWT.DefaultSelection, new Listener () {
			@Override
			public void handleEvent (Event event) {
				if (!StringUtils.isBlank(txtQty.getText())) {
					btnPost.setEnabled(true);
					// Item ID
					order.getItemIds().add(row, Integer.parseInt(txtItemId.getText()));
					txtItemId.dispose();
					// UOM
					cmbUom = lineItem.getCmbUom();
					uom = new UOM(cmbUom.getText()).getId();
					order.getUoms().add(row, uom);
					tableItem.setText(3, cmbUom.getText());
					cmbUom.dispose();
					// QC status
					order.getQcs().add(row, lineItem.getCmbQc().getSelectionIndex());
					tableItem.setText(4, lineItem.getCmbQc().getText());
					// Expiry Date
					order.getQcs().add(row, lineItem.getCmbQc().getSelectionIndex());
					tableItem.setText(4, lineItem.getCmbQc().getText());
					lineItem.getCmbQc().dispose();
					// Qty
					tableItem.setText(5, txtQty.getText());
					qty = new BigDecimal(txtQty.getText());
					order.getQtys().add(row, qty);
					txtQty.dispose();
					// Go to next line
					view.setTxtItemId(new ReceivingLineItem(
							view,
							order, 
							order.getItemIds().size()
							).getTxtItemId());
				}
			}
		});
	}
}

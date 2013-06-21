package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ReceivingItemIdEntry {
	private ReceivingLineItem lineItem;
	private ItemHelper item;
	private TableItem tableItem;
	private Text txtItemId;
	private Combo cmbUom;
	private Button btnPost;
	private String itemName;
	private int itemId, row;

	public ReceivingItemIdEntry(
			ReceivingView view,
			ReceivingLineItem receivingLineItem, 
			Receiving receiving,
			int line) {
		lineItem = receivingLineItem;
		row = line;
		btnPost = view.getBtnPost();
		tableItem = lineItem.getTableItem();
		txtItemId = lineItem.getTxtItemId();
		cmbUom = lineItem.getCmbUom();

		new IntegerVerifier(txtItemId);
		txtItemId.addListener (SWT.DefaultSelection, new Listener () {

			@Override
			public void handleEvent (Event ev) {
				item = new ItemHelper();
				if (StringUtils.isBlank(txtItemId.getText())) {
					if (row == 0) return;
					btnPost.setEnabled(true);
					btnPost.setFocus();
				} else {
					itemId = Integer.parseInt(txtItemId.getText());
					itemName = item.getName(itemId);
					if (itemName == null) {
						clearEntry("Item ID " + itemId + "\nis not in our system");
						return;
					} 
					tableItem.setText(1, "" + itemId);
					tableItem.setText(2, itemName);
					
					String[] soldUoms = new UOM().getSoldUoms(itemId);
					lineItem.getBtnItemId().dispose();
					lineItem.setSoldUoms(soldUoms);
					
					btnPost.setEnabled(false);
					cmbUom.setEnabled(true);
					cmbUom.setFocus();
					cmbUom.setItems(soldUoms);
					cmbUom.select(0);
				}
			}
		});
	}

	protected void clearEntry(String msg){
		new ErrorDialog(msg);
		txtItemId.setText("");
		tableItem.setText(2, "");
		txtItemId.setTouchEnabled(true);
		txtItemId.setFocus();
	}
	
}

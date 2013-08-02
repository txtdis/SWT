package ph.txtdis.windows;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class StockTakeLineItem {
	private StockTakeView view;
	private StockTake stockTake;
	private ItemHelper item;
	private Table table;
	private TableItem tableItem;
	private Text txtItemId, txtQty, txtExpiry;
	private Combo cmbUom, cmbQc;
	private Button btnItemId, btnPost;
	private String itemName;
	private int itemId, rowIdx;

	public StockTakeLineItem(StockTakeView stv, StockTake st, int line) {
		view = stv;
		stockTake = st;
		rowIdx = line;
		table = view.getTable();
		tableItem = view.getTableItem(rowIdx);
		btnPost = view.getBtnPost();
		// Dispose all input controls
		cmbUom = view.getCmbUom();
		if(cmbUom != null) cmbUom.dispose();
		cmbQc = view.getCmbQc();
		if(cmbQc != null) cmbQc.dispose();
		txtQty = view.getTxtQty();
		if(txtQty != null) txtQty.dispose();
		txtExpiry = view.getTxtExpiry();
		if(txtExpiry != null) txtExpiry.dispose();
		txtItemId = view.getTxtItemId();
		if(txtItemId != null) txtItemId.dispose();
		btnItemId = view.getBtnItemId();
		if(btnItemId != null) btnItemId.dispose();
		// Create new Item ID button & input
		btnItemId = new TableButton(tableItem, rowIdx, 0, "Item List").getButton();
		txtItemId = new TableInput(tableItem, rowIdx, 1, 0).getText();
		view.setTxtItemId(txtItemId);
		view.setBtnItemId(btnItemId);
		txtItemId.setText(tableItem.getText(1));
		txtItemId.setTouchEnabled(true);
		txtItemId.setFocus();
		txtItemId.addSelectionListener(new SelectionListener() {			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				item = new ItemHelper();
				if (StringUtils.isBlank(txtItemId.getText())) {
					if (rowIdx == 0) return;
					btnPost.setEnabled(true);
					btnPost.setFocus();
				} else {
					itemId = Integer.parseInt(txtItemId.getText());
					itemName = item.getName(itemId);
					if (itemName == null) {
						clearEntry("Item ID " + itemId + "\nis not in our system");
						return;
					} 
					tableItem.setText(0, "" + (rowIdx + 1));
					tableItem.setText(1, "" + itemId);
					tableItem.setText(2, itemName);
					btnItemId.dispose();
					txtItemId.dispose();
					btnPost.setEnabled(false);
					cmbUom = new TableSelection(tableItem, rowIdx, 3).getCombo();
					view.setCmbUom(cmbUom);
					cmbUom.setItems(new UOM().getSellingUoms(itemId));
					cmbUom.setEnabled(true);
					cmbUom.setFocus();
					cmbUom.select(0);
					setUomSelector();
				}
			}
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
	}

	private void clearEntry(String msg){
		new ErrorDialog(msg);
		new StockTakeLineItem(view, stockTake, rowIdx);
	}

	private void setUomSelector() {
		new DataSelector(cmbUom, txtQty) {
			@Override
			protected void doWhenSelected() {
				tableItem.setText(3, cmbUom.getText());
				cmbUom.dispose();
				txtQty = new TableInput(
						tableItem, rowIdx, 4, BigDecimal.ZERO).getText();
				view.setTxtQty(txtQty);
				setNext(txtQty);
				setQtyInput();
			}
		};		
	}

	private void setQtyInput() {
		new DataInput(txtQty, cmbQc) {
			@Override
			protected boolean isDataInputValid() {
				if(new BigDecimal(string).compareTo(BigDecimal.ZERO) < 0)
					return false;
				tableItem.setText(4, string);
				txtQty.dispose();
				cmbQc = new TableSelection(
						tableItem, rowIdx, 5, new Quality().getStates(), null).getCombo();
				view.setCmbQc(cmbQc);
				cmbQc.setText(tableItem.getText(5));				
				cmbQc.setEnabled(true);
				cmbQc.setFocus();
				setNext(txtQty);
				setQualitySelector();
				return true;
			}
		};	
	}

	private void setQualitySelector() {
		new DataSelector(cmbQc, txtExpiry) {
			@Override
			protected void doWhenSelected() {
				tableItem.setText(5, cmbQc.getText());
				cmbQc.dispose();
				txtExpiry = new TableInput(
						tableItem, rowIdx, 6, new DateAdder().plus(1)).getText();
				view.setTxtExpiry(txtExpiry);
				setNext(txtExpiry);
				setExpiryInput();
			}				
		};
	}

	private void setExpiryInput() {
		new DataInput(txtExpiry, txtItemId) {
			@Override
			protected boolean isInputValid() {
				tableItem.setText(6, txtExpiry.getText());
				txtExpiry.dispose();
				int lastRowIdx = table.getItems().length - 1;
				if(rowIdx != lastRowIdx) {
					rowIdx = lastRowIdx;
					tableItem = table.getItem(rowIdx);
					for (int i = 0; i < table.getColumnCount(); i++) 
						tableItem.setText(i, "");					
				} else {
					tableItem = new TableItem(table, SWT.NONE, ++rowIdx); 
				}
				if(rowIdx > 9) table.setTopIndex(rowIdx - 9);
				new StockTakeLineItem(view, stockTake, rowIdx);
	
				setNext(txtItemId);
				return true;		
				
			}				
		};
	}

	public Text getTxtItemId() {
		return txtItemId;
	}

	public Combo getCmbUom() {
		return cmbUom;
	}
}

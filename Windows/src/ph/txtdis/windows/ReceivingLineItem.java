package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Calendar;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

public class ReceivingLineItem extends TableLineItem {
	private Text txtItemId, txtExpiry, txtQty;
	private Combo cmbUom, cmbQc;
	private Button btnItemId;
	private ReceivingView view;
	private Receiving order;
	private String[] soldUoms;
	
	public ReceivingLineItem(ReceivingView receivingView, Receiving receiving, int row) {
		super(receivingView, receiving, row);
		view = receivingView;
		order = receiving;
		btnItemId = new TableButton(tableItem, row, 0, "Item List").getButton();
		txtItemId = new TableInput(tableItem, row, 1, 0).getText();
		view.setTxtItemId(txtItemId);
		cmbUom = new TableSelection(tableItem, row, 3).getCombo();
		cmbQc = new TableSelection(tableItem, row, 4).getCombo();
		Calendar calendar = Calendar.getInstance();
		calendar.set(9999, Calendar.DECEMBER, 31);
		txtExpiry = new TableInput(tableItem, row, 5, new Date(calendar.getTimeInMillis())).getText();
		txtQty = new TableInput(tableItem, row, 6, BigDecimal.ZERO).getText();
		txtItemId.setTouchEnabled(true);
		txtItemId.setFocus();
		
		//Listeners
		new ReceivingItemIdEntry(view, this, order, row);
		new ReceivingQcSelection(view, this, order, row);
		new ReceivingUomSelection(view, this, order, row);
		new ReceivingItemQtyEntry(view, this, order, row);
	}
	
	public Text getTxtQty() {
		return txtQty;
	}

	public Combo getCmbUom() {
		return cmbUom;
	}

	public Button getBtnItemId() {
		return btnItemId;
	}

	public Text getTxtItemId() {
		return txtItemId;
	}

	public Combo getCmbQc() {
		return cmbQc;
	}

	public Text getTxtExpiry() {
		return txtExpiry;
	}

	public String[] getSoldUoms() {
		return soldUoms;
	}
	
	public void setSoldUoms(String[] soldUoms) {
		this.soldUoms = soldUoms;
	}
}

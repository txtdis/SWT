package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public abstract class OrderView extends ReportView {
	private Button tableListButton;
	private Combo routeCombo, uomCombo;
	private Text txtDueDate, txtTotalVatable, txtTotalVat, computedTotalDisplay,
	        inputterDisplay, inputDateDisplay, inputTimeDisplay;
	private TextDisplayBox firstLevelDiscountBox, secondLevelDiscountBox;

	protected int id, itemId, partnerId, referenceId, rowIdx;
	protected Customer customer;
	protected String series, bizUnit;
	protected Text addressDisplay, dateInput, enteredTotalInput, idDisplay, itemIdInput, partnerIdInput, partnerDisplay, qtyInput, referenceIdInput,
	        seriesDisplay;
	protected Order order;
	protected OrderHelper helper;

	protected Button postButton, listButton, newButton;
	protected Boolean isUomOrDayBased;
	protected Integer uomOrDayCount;

	public OrderView() {
	}

	public OrderView(Order soOrPo) {
		order = soOrPo;
		order.setId(0);
		order.setModule("Invoice");
		setProgress();
		setTitleBar();
		setHeader();
		getTable();
		setFooter();
		setListener();
		setFocus();
		showReport();
	}

	public OrderView(int orderId) {
		this(null, orderId, null, null, null);
	}

	public OrderView(int orderId, String series) {
		this(series, orderId, null, null, null);
	}

	public OrderView(int orderId, String bizUnit, Boolean isUomOrDayBased, Integer uomOrDayCount) {
		this(null, orderId, bizUnit, isUomOrDayBased, uomOrDayCount);
	}

	public OrderView(String invoiceSeries, int orderId, String poBizUnit, Boolean isPOUomOrDayBased,
	        Integer uomOrDayCount) {
		series = invoiceSeries;
		id = orderId;
		bizUnit = poBizUnit;
		isUomOrDayBased = isPOUomOrDayBased;
		this.uomOrDayCount = uomOrDayCount;
		setProgress();
		setTitleBar();
		setHeader();
		getTable();
		setFooter();
		setListener();
		setFocus();
		showReport();
	}

	@Override
	protected void runClass() {
		if (order == null)
			order = new Invoice(id, series);
		report = order;
		report.setModule(getModule());
	}

	@Override
	protected void setTitleBar() {
		MasterTitleBar mtb = new MasterTitleBar(this, order);
		newButton = mtb.getBtnNew();
		postButton = mtb.getSaveButton();
	}

	@Override
	protected void setHeader() {
		new OrderHeaderBar(this, order);
	}

	@Override
	protected void setFooter() {
		new InvoiceFooter(this, order);
	}

	public Text getItemIdInput() {
		return itemIdInput;
	}

	@Override
	protected void setListener() {
		if (order.getId() == 0) {
			new OrderActualAmountEntry(this, order);
			new SalesOrderIdEntry(this, order);
			new OrderPartnerIdEntry(this, order);
			new OrderDateEntry(this, order);
		}
	}

	@Override
	protected void setFocus() {
		String module = order.getModule();
		if (id == 0) {
			if (order.getReferenceId() == 0) {
				referenceIdInput.setTouchEnabled(true);
				referenceIdInput.setFocus();
			} else if (module.equals("Invoice")) {
				seriesDisplay.setTouchEnabled(true);
				seriesDisplay.setFocus();
			} else if (module.equals("Delivery Report")) {
				enteredTotalInput.setTouchEnabled(true);
				enteredTotalInput.setFocus();
			}
		}
	}

	public void disposeAllTableWidgets(int rowIdx) {
		if (itemIdInput != null)
			itemIdInput.dispose();
		if (routeCombo != null)
			routeCombo.dispose();
		if (uomCombo != null)
			uomCombo.dispose();
		if (qtyInput != null)
			qtyInput.dispose();
		if (tableListButton != null)
			tableListButton.dispose();

		int oldRowIdx = order.getRowIdx();
		TableItem oldTableItem = table.getItem(oldRowIdx);
		if (rowIdx != oldRowIdx && oldTableItem.getText(order.TOTAL_COLUMN).isEmpty()) {
			oldTableItem.dispose();

			ArrayList<Integer> itemIds = order.getItemIds();
			ArrayList<Integer> uomIds = order.getUomIds();
			if (itemIds.size() > oldRowIdx) {
				itemIds.remove(oldRowIdx);
				uomIds.remove(oldRowIdx);
			}

			ArrayList<BigDecimal> qtys = order.getQtys();
			if (qtys.size() > oldRowIdx)
				qtys.remove(oldRowIdx);

			int i = 0;
			if (table.getItemCount() > oldRowIdx)
				for (TableItem tableItem : table.getItems())
					tableItem.setText(i, String.valueOf(++i));
		}
	}

	protected String getModule() {
		return report.getModule();
	}

	public Text getTxtSeries() {
		return seriesDisplay;
	}

	public void setTxtSeries(Text txtSeries) {
		this.seriesDisplay = txtSeries;
	}

	public Text getTxtPostDate() {
		return dateInput;
	}

	public void setTxtPostDate(Text txtPostDate) {
		this.dateInput = txtPostDate;
	}

	public Text getTxtDueDate() {
		return txtDueDate;
	}

	public void setTxtDueDate(Text txtDueDate) {
		this.txtDueDate = txtDueDate;
	}

	public Text getTxtPartnerId() {
		return partnerIdInput;
	}

	public void setTxtPartnerId(Text txtPartnerId) {
		this.partnerIdInput = txtPartnerId;
	}

	public Text getTxtPartnerName() {
		return partnerDisplay;
	}

	public void setTxtPartnerName(Text txtPartnerName) {
		this.partnerDisplay = txtPartnerName;
	}

	public Text getAddressDisplay() {
		return addressDisplay;
	}

	public void setAddressDisplay(Text addressDisplay) {
		this.addressDisplay = addressDisplay;
	}

	public Text getTxtTotalVatable() {
		return txtTotalVatable;
	}

	public void setTxtTotalVatable(Text txtTotalVatable) {
		this.txtTotalVatable = txtTotalVatable;
	}

	public Text getTxtTotalVat() {
		return txtTotalVat;
	}

	public void setTxtTotalVat(Text txtTotalVat) {
		this.txtTotalVat = txtTotalVat;
	}

	public Text getTxtEnteredTotal() {
		return enteredTotalInput;
	}

	public void setTxtEnteredTotal(Text txtActual) {
		this.enteredTotalInput = txtActual;
	}

	public Text getComputedTotalDisplay() {
		return computedTotalDisplay;
	}

	public void setComputedTotalDisplay(Text computedTotalDisplay) {
		this.computedTotalDisplay = computedTotalDisplay;
	}

	public TextDisplayBox getFirstLevelDiscountBox() {
		return firstLevelDiscountBox;
	}

	public void setFirstLevelDiscountBox(TextDisplayBox firstLevelDiscountBox) {
		this.firstLevelDiscountBox = firstLevelDiscountBox;
	}

	public Text getIdInput() {
		return idDisplay;
	}

	public void setIdInput(Text idInput) {
		this.idDisplay = idInput;
	}

	public Text getInputDateDisplay() {
		return inputDateDisplay;
	}

	public void setInputDateDisplay(Text inputDateDisplay) {
		this.inputDateDisplay = inputDateDisplay;
	}

	public Text getInputterDisplay() {
		return inputterDisplay;
	}

	public void setInputterDisplay(Text inputterDisplay) {
		this.inputterDisplay = inputterDisplay;
	}

	public Text getInputTimeDisplay() {
		return inputTimeDisplay;
	}

	public void setInputTimeDisplay(Text inputTimeDisplay) {
		this.inputTimeDisplay = inputTimeDisplay;
	}

	public void setItemIdInput(Text itemIdInput) {
		this.itemIdInput = itemIdInput;
	}

	public Button getListButton() {
		return listButton;
	}

	public void setListButton(Button listButton) {
		this.listButton = listButton;
	}

	public Button getNewButton() {
		return newButton;
	}

	public void setNewButton(Button newButton) {
		this.newButton = newButton;
	}

	public Button getPostButton() {
		return postButton;
	}

	public void setPostButton(Button postButton) {
		this.postButton = postButton;
	}

	public Text getQtyInput() {
		return qtyInput;
	}

	public void setQtyInput(Text qtyInput) {
		this.qtyInput = qtyInput;
	}

	public Text getReferenceIdInput() {
		return referenceIdInput;
	}

	public void setReferenceIdInput(Text referenceIdInput) {
		this.referenceIdInput = referenceIdInput;
	}

	public TextDisplayBox getSecondLevelDiscountBox() {
		return secondLevelDiscountBox;
	}

	public void setSecondLevelDiscountBox(TextDisplayBox secondLevelDiscountBox) {
		this.secondLevelDiscountBox = secondLevelDiscountBox;
	}

	public Button getTableListButton() {
		return tableListButton;
	}

	public void setTableListButton(Button tableListButton) {
		this.tableListButton = tableListButton;
	}

	public Combo getUomCombo() {
		return uomCombo;
	}

	public void setUomCombo(Combo uomCombo) {
		this.uomCombo = uomCombo;
	}
}

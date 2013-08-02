package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

public abstract class OrderView extends ReportView {
	private Text txtItemId, txtQty; 
	private Text txtDueDate, txtPartnerName, txtAddress; 
	private Text txtTotalVatable, txtTotalVat;
	protected Text txtSoId, txtOrderId, txtSeries, txtActual;
	private Text txtSumTotal;
	private Text txtEncoder, txtEncDate, txtEncTime;
	private DataDisplay discount1, discount2;
	private Combo cmbUom;
	private Button btnItem;
	private String itemIdText;
	private int rowIdx;

	protected Text txtPartnerId, txtPostDate;
	protected Order order;
	protected String series, bizUnit;
	protected Button btnPost, btnList, btnNew;
	protected Boolean isUomOrDayBased;
	protected Integer uomOrDayCount;
	protected int orderId;

	public OrderView(){
	}

	public OrderView(Order soOrPo){
		order = soOrPo;
		order.setModule("Invoice");
		order.setId(0);
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

	public OrderView(
			int orderId,
			String bizUnit,
			Boolean isUomOrDayBased, 
			Integer uomOrDayCount
			) {
		this(null, orderId, bizUnit, isUomOrDayBased, uomOrDayCount);
	}

	public OrderView(
			String series, 
			int orderId, 
			String bizUnit, 
			Boolean isUomOrDayBased, 
			Integer uomOrDayCount
			) {
		this.series = series;
		this.orderId = orderId;
		this.bizUnit = bizUnit;
		this.isUomOrDayBased = isUomOrDayBased;
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
		if(order == null) order = new Invoice(orderId, series);
		report = order;
		report.setModule(getModule());
	}

	@Override 
	protected void setTitleBar() {
		MasterTitleBar mtb = new MasterTitleBar(this, order);
		btnNew = mtb.getBtnNew();
		btnPost = mtb.getBtnPost();
	}

	@Override
	protected void setHeader() {
		new OrderHeaderBar(this, order);
	}

	@Override
	protected void setFooter() {
		new InvoiceFooter(this, order);
	}

	@Override
	protected void setListener() {
		if(order.getId() == 0) {
			new OrderActualAmountEntry(this, order);
			new SalesOrderIdEntry(this, order);
			new OrderPartnerIdEntry(this, order);
			new OrderDateEntry(this, order);
		}
	}

	@Override
	protected void setFocus() {
		String module = order.getModule();
		if(orderId == 0) {
			if(order.getSoId() == 0) {
				txtSoId.setTouchEnabled(true);
				txtSoId.setFocus();
			} else if (module.equals("Invoice")){
				txtSeries.setTouchEnabled(true);
				txtSeries.setFocus();
			} else if (module.equals("Delivery Report")) {
				txtActual.setTouchEnabled(true);
				txtActual.setFocus();
			}
		}
	}

	protected String getModule() {
		return report.getModule();
	}

	public Text getTxtSeries() {
		return txtSeries;
	}

	public void setTxtSeries(Text txtSeries) {
		this.txtSeries = txtSeries;
	}

	public Text getTxtPostDate() {
		return txtPostDate;
	}

	public void setTxtPostDate(Text txtPostDate) {
		this.txtPostDate = txtPostDate;
	}

	public Text getTxtDueDate() {
		return txtDueDate;
	}

	public void setTxtDueDate(Text txtDueDate) {
		this.txtDueDate = txtDueDate;
	}

	public Text getTxtPartnerId() {
		return txtPartnerId;
	}

	public void setTxtPartnerId(Text txtPartnerId) {
		this.txtPartnerId = txtPartnerId;
	}

	public Text getTxtPartnerName() {
		return txtPartnerName;
	}

	public void setTxtPartnerName(Text txtPartnerName) {
		this.txtPartnerName = txtPartnerName;
	}

	public Text getTxtAddress() {
		return txtAddress;
	}

	public void setTxtAddress(Text txtAddress) {
		this.txtAddress = txtAddress;
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
		return txtActual;
	}

	public void setTxtEnteredTotal(Text txtActual) {
		this.txtActual = txtActual;
	}

	public void setBtnPost(Button btnPost) {
		this.btnPost = btnPost;
	}

	public Button getBtnList() {
		return btnList;
	}

	public void setBtnList(Button btnList) {
		this.btnList = btnList;
	}

	public Text getTxtId() {
		return txtOrderId;
	}

	public void setTxtOrderId(Text txtOrderId) {
		this.txtOrderId = txtOrderId;
	}

	public Text getTxtSoId() {
		return txtSoId;
	}

	public void setTxtSoId(Text txtSoId) {
		this.txtSoId = txtSoId;
	}

	public Text getTxtComputedTotal() {
		return txtSumTotal;
	}

	public void setTxtSumTotal(Text txtSumTotal) {
		this.txtSumTotal = txtSumTotal;
	}

	public Text getTxtEncoder() {
		return txtEncoder;
	}

	public void setTxtEncoder(Text txtEncoder) {
		this.txtEncoder = txtEncoder;
	}

	public Text getTxtEncDate() {
		return txtEncDate;
	}

	public void setTxtEncDate(Text txtEncDate) {
		this.txtEncDate = txtEncDate;
	}

	public Text getTxtEncTime() {
		return txtEncTime;
	}

	public void setTxtEncTime(Text txtEncTime) {
		this.txtEncTime = txtEncTime;
	}

	public Button getBtnPost() {
		return btnPost;
	}

	public DataDisplay getDiscount1() {
		return discount1;
	}

	public void setDiscount1(DataDisplay discount1) {
		this.discount1 = discount1;
	}

	public DataDisplay getDiscount2() {
		return discount2;
	}

	public void setDiscount2(DataDisplay discount2) {
		this.discount2 = discount2;
	}

	public Button getBtnNew() {
		return btnNew;
	}

	public void setBtnNew(Button btnNew) {
		this.btnNew = btnNew;
	}

	public Combo getCmbUom() {
		if (cmbUom == null || cmbUom.isDisposed()) {
			rowIdx = order.getRowIdx();
			cmbUom = new TableSelection(getTableItem(rowIdx), rowIdx, 3).getCombo();
		}
		return cmbUom;
	}

	public Text getTxtQty() {
		if (txtQty == null || txtQty.isDisposed()) {
			rowIdx = order.getRowIdx();
			txtQty = new TableInput(getTableItem(rowIdx), rowIdx, 4, BigDecimal.ZERO).getText();
		}
		return txtQty;
	}
	
	public Text getTxtItemId() {
		if (txtItemId == null || txtItemId.isDisposed()) {
			rowIdx = order.getRowIdx();
			tableItem = getTableItem(rowIdx);
			itemIdText = tableItem.getText(1);
			int itemId = itemIdText.isEmpty() ? 0 : Integer.parseInt(itemIdText.replace("(", "-").replace(")", ""));
			tableItem.setText(1, "");
			txtItemId = new TableInput(tableItem, rowIdx, 1, itemId).getText();
		}
		return txtItemId;
	}

	public String getItemIdText() {
		return itemIdText;
	}

	public Button getBtnItem() {
		if (btnItem == null || btnItem.isDisposed()) {
			rowIdx = order.getRowIdx();
			btnItem = new TableButton(getTableItem(rowIdx), rowIdx, 0, "Item List").getButton();
		}
		return btnItem;
	}

	public void disposeAllTableWidgets() {
		if (btnItem != null && !btnItem.isDisposed())
			btnItem.dispose();
		if (cmbUom != null && !cmbUom.isDisposed())
			cmbUom.dispose();
		if (txtQty != null && !txtQty.isDisposed())
			txtQty.dispose();
		if (txtItemId != null && !txtItemId.isDisposed())
			txtItemId.dispose();
		tableItem = table.getItem(order.getRowIdx());
		if(tableItem.getText(6).isEmpty())
			tableItem.dispose();
		else if(tableItem.getText(1).isEmpty())
			tableItem.setText(1, itemIdText);
    }
}

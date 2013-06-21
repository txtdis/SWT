package ph.txtdis.windows;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public abstract class OrderView extends ReportView {
	private Text txtItemId;
	private Text txtDueDate;
	private Text txtPartnerName, txtAddress; 
	private Text txtTotalDiscount1, txtTotalDiscount2, txtTotalVatable, txtTotalVat;
	protected Text txtSoId, txtOrderId, txtSeries, txtActual;
	private Text txtSumTotal;
	private Text txtEncoder, txtEncDate, txtEncTime;
	private DataDisplay discount1, discount2;

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
		setTableBar();
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
		setTableBar();
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
		new ReportTitleBar(this, order) {
			@Override
			protected void layButtons() {
				btnNew = new NewButton(buttons, module).getButton();
				new RetrieveButton(buttons, report);	
				btnPost = new PostButton(buttons, reportView, report).getButton();
				new ExitButton(buttons, module);
			}
		};
	}

	@Override
	protected void setHeader() {
		new InvoiceHeaderBar(this, order);
	}

	@Override
	protected void setTableBar() {
		new InvoiceTable(this, order);
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
			new InvoicePartnerIdEntry(this, order);
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

	public Text getTxtTotalDiscount1() {
		return txtTotalDiscount1;
	}

	public void setTxtTotalDiscount1(Text txtTotalDiscount1) {
		this.txtTotalDiscount1 = txtTotalDiscount1;
	}

	public Text getTxtTotalDiscount2() {
		return txtTotalDiscount2;
	}

	public void setTxtTotalDiscount2(Text txtTotalDiscount2) {
		this.txtTotalDiscount2 = txtTotalDiscount2;
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

	public Text getTxtActual() {
		return txtActual;
	}

	public void setTxtActual(Text txtActual) {
		this.txtActual = txtActual;
	}

	public Text getTxtItemId() {
		return txtItemId;
	}

	public void setTxtItemId(Text txtItemId) {
		this.txtItemId = txtItemId;
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

	public Text getTxtSumTotal() {
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

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
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
}

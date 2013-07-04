package ph.txtdis.windows;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

public class ReceivingView extends ReportView {
	protected Receiving receiving;
	private Text txtPartnerId, txtPartnerName, txtAddress, txtDate;
	private Text txtOrderId, txtTotal, txtRefId, txtItemId;
	private Button btnPost, btnList;
	protected int rrId;

	public ReceivingView(int rrId) {
		this.rrId = rrId;
		setProgress();
		setTitleBar();
		setHeader();
		setTableBar();
		setListener();
		setFocus();
		showReport();
	}

	@Override
	protected void runClass() {
		report = receiving = new Receiving(rrId);
	}

	@Override
	protected void setTitleBar() {
		new ReceivingTitleBar(this, receiving);
	}

	@Override
	protected void setHeader() {
		new ReceivingHeaderBar(this, receiving);
	}

	@Override
	protected void setTableBar() {
		new ReportTable(this, report);
	}

	@Override
	protected void setListener() {
		new ReceivingPartnerIdEntry(this, receiving);
		new ReceivingDateEntry(this, receiving); 
		new ReceivingRefIdEntry(this, receiving); 
	}


	@Override
	protected void setFocus() {
		txtPartnerId.setTouchEnabled(true);
		txtPartnerId.setFocus();
		btnList.setEnabled(true);
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

	public Text getTxtDate() {
		return txtDate;
	}

	public void setTxtDate(Text txtDate) {
		this.txtDate = txtDate;
	}

	public Text getTxtOrderId() {
		return txtOrderId;
	}

	public void setTxtOrderId(Text txtOrderId) {
		this.txtOrderId = txtOrderId;
	}

	public Text getTxtTotal() {
		return txtTotal;
	}

	public void setTxtTotal(Text txtTotal) {
		this.txtTotal = txtTotal;
	}

	public Text getTxtRefId() {
		return txtRefId;
	}

	public void setTxtRefId(Text txtRefId) {
		this.txtRefId = txtRefId;
	}

	public Text getTxtItemId() {
		return txtItemId;
	}

	public void setTxtItemId(Text txtItemId) {
		this.txtItemId = txtItemId;
	}

	public Button getBtnPost() {
		return btnPost;
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

	public static void main(String[] args) {
//		Database.getInstance().getConnection("irene","ayin");
		Database.getInstance().getConnection("sheryl","10-8-91");
		Login.group = "user_supply";
		new ReceivingView(0);
		Database.getInstance().closeConnection();
	}

}
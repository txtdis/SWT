package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class CustomerView extends ReportView {
	private Button btnPost;
	private int customerId;
	private Combo cmbCity, cmbDistrict, cmbProvince, cmbRoute, cmbChannel;
	private Text txtId, txtSmsId, txtName, txtStreet;
	private Text txtFirstName, txtSurname, txtJob, txtPhone;
	private Table tblCredit, tblDiscount;
	private CustomerMaster cm;
	private Group grpContact, grpAddress;

	public CustomerView(int customerId) {
		super();
		this.customerId = customerId;
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
		report = cm = new CustomerMaster(customerId);
	}

	@Override
	protected void setTitleBar() {
		btnPost = new MasterTitleBar(this, report).getBtnPost();
	}

	@Override
	protected void setHeader() {
		Composite header = new Composite(shell, SWT.NO_TRIM);
		header.setLayout(new GridLayout(2, false));
		header.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		/// PARTNER DETAILS
		Composite left = new Composite(header, SWT.NONE);
		left.setLayout(new GridLayout(2, false));

		// NAME
		Group grpPartner = new Group(left, SWT.NONE);
		grpPartner.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false,2, 1));
		grpPartner.setText("PARTNER");
		grpPartner.setLayout(new GridLayout(4, false));

		int id = cm.getId();
		String smsId = cm.getSmsId();
		String name = cm.getName();

		txtId 	= 	new DataEntry(grpPartner, "ID        ", id).getText();
		txtSmsId = 	new DataEntry(grpPartner, "SMS ID", smsId, 1, 4).getText();

		txtName = new DataEntry(grpPartner, "NAME      ", name, 3, 32).getText();
		txtName.setTouchEnabled(true);
		txtName.setFocus();

		cmbChannel = new DataSelection(
				grpPartner, 
				cm.getChannels(), 
				"CHANNEL   ", 
				cm.getChannel()
				).getCombo();

		// ADDRESS
		grpAddress = new Group(left, SWT.NONE);
		grpAddress.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false,2, 1));
		grpAddress.setText("ADDRESS");
		grpAddress.setLayout(new GridLayout(2, false));

		cmbProvince = new DataSelection(
				grpAddress, 
				cm.getProvinces(),
				"PROVINCE  ", 
				cm.getProvince()
				).getCombo();
		cmbCity = new DataSelection(
				grpAddress, 
				cm.getCities(),
				"CITY      ", 
				cm.getCity()
				).getCombo();
		cmbDistrict = new DataSelection(
				grpAddress,
				cm.getDistricts(),
				"DISTRICT  ",
				cm.getDistrict()).getCombo();
		txtStreet 	= new DataEntry(
				grpAddress,
				"STREET    ", 
				cm.getStreet(), 
				1, 
				32
				).getText();

		// CONTACT DETAIL
		grpContact = new Group(left, SWT.NONE);
		grpContact.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false,2, 1));
		grpContact.setText("CONTACT");
		grpContact.setLayout(new GridLayout(2, false));

		String firstName = cm.getFirstName();
		String surname = cm.getSurname();
		String designation = cm.getDesignation();
		long phone = cm.getPhone();

		txtFirstName = new DataEntry(grpContact, "NAME", firstName, 1, 16).getText();
		txtSurname = new DataEntry(grpContact, "SURNAME", surname, 1, 16).getText();
		txtJob = new DataEntry(grpContact, "DESIGNATION", designation, 1, 16).getText();
		txtPhone = new DataEntry(grpContact, "PHONE", phone).getText();

		/// MONETARY DETAILS
		Composite right = new Composite(header, SWT.NONE);
		right.setLayout(new GridLayout(2, false));

		// ROUTE
		cmbRoute = new DataSelection(right, cm.getRoutes(), "ROUTE", cm.getRoute()).getCombo();

		// CREDIT
		Label lblCredit = new Label(right, SWT.NONE);
		lblCredit.setText("CREDIT");
		String[][] creditHeaders = new String[][]{
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("LIMIT", 13), "Integer"},
				{StringUtils.center("TERM", 10), "Integer"},
				{StringUtils.center("GRACE", 10), "Integer"},
				{StringUtils.center("SINCE", 10), "Date"}};
		Object[][] credits = cm.getCreditData();
		tblCredit = new ReportTable(
				right, credits, creditHeaders, "", 100, true).getTable();

		// DISCOUNT
		Label lblDiscount = new Label(right, SWT.NONE);
		lblDiscount.setText("DISCOUNT");
		String[][] discountHeaders = new String[][]{
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("ITEM FAMILY", 28), "String"},
				{StringUtils.center("LEVEL 1", 6), "BigDecimal"},
				{StringUtils.center("LEVEL 2", 6), "BigDecimal"},
				{StringUtils.center("SINCE", 10), "Date"}};
		Object[][] discounts = cm.getDiscountData();
		tblDiscount = new ReportTable(
				right, discounts, discountHeaders, "", 100, true).getTable();
	}

	@Override
	protected void setTableBar() {
		// This has no table but superclass has
	}

	@Override
	protected void setListener() {
		new DataInput(txtSmsId, txtName) {
			@Override
			protected boolean act() {
				String smsId = txtSmsId.getText().trim();
				if(smsId.isEmpty()) {
					return false;
				} else if (new CustomerHelper().hasSmsId(smsId)) {
					new ErrorDialog(
							smsId + " has been used;\n" +
							"try another.");
					return false;
				} else {
					return true;
				}
			}
		};
		new DataInput(txtName, cmbChannel);
		new DataSelector(cmbChannel, cmbCity) {
			@Override
			protected void act() {
				switch (cmbChannel.getText()) {
					case "OTHERS":
					case "ROUTE":
						cmbProvince.removeAll();
						cmbCity.removeAll();
						cmbDistrict.removeAll();
						cmbRoute.removeAll();
						setNext(btnPost);
						break;
				}
			}
		};
		new DataSelector(cmbCity, cmbDistrict) {
			@Override
			protected void act() {
				int districtId = new Area(cmbCity.getText()).getId();
				cmbDistrict.setItems(new Area(districtId).getAreas());
				cmbDistrict.select(0);
			}
		};
		new DataSelector(cmbDistrict, txtStreet);
		new DataInput(txtStreet, txtFirstName) {
			@Override
			protected boolean ifBlank() {
				return true;
			}
		};
		new DataInput(txtFirstName, txtSurname){
			@Override
			protected boolean act() {
				if(txtFirstName.getText().trim().isEmpty()) {
					txtSurname.setText("");
					txtJob.setText("");
					txtPhone.setText("");
					setNext(cmbRoute);
				} else {
					setNext(txtSurname);
				}
				return true;
			}
		};
		new DataInput(txtSurname, txtJob);
		new DataInput(txtJob, txtPhone);
		new DataInput(txtPhone, cmbRoute);
		new DataSelector(cmbRoute, btnPost);
	}

	@Override
	protected void setFocus() {
		txtSmsId.setTouchEnabled(true);
		txtSmsId.setFocus();
	}

	public Button getBtnPost() {
		return btnPost;
	}

	public void setBtnPost(Button btnPost) {
		this.btnPost = btnPost;
	}

	public Combo getCmbCity() {
		return cmbCity;
	}

	public void setCmbCity(Combo cmbCity) {
		this.cmbCity = cmbCity;
	}

	public Combo getCmbDistrict() {
		return cmbDistrict;
	}

	public void setCmbDistrict(Combo cmbDistrict) {
		this.cmbDistrict = cmbDistrict;
	}

	public Combo getCmbProvince() {
		return cmbProvince;
	}

	public void setCmbProvince(Combo cmbProvince) {
		this.cmbProvince = cmbProvince;
	}

	public Combo getCmbRoute() {
		return cmbRoute;
	}

	public void setCmbRoute(Combo cmbRoute) {
		this.cmbRoute = cmbRoute;
	}

	public Combo getCmbChannel() {
		return cmbChannel;
	}

	public void setCmbChannel(Combo cmbChannel) {
		this.cmbChannel = cmbChannel;
	}

	public Text getTxtId() {
		return txtId;
	}

	public void setTxtId(Text txtId) {
		this.txtId = txtId;
	}

	public Text getTxtSmsId() {
		return txtSmsId;
	}

	public void setTxtSmsId(Text txtSmsId) {
		this.txtSmsId = txtSmsId;
	}

	public Text getTxtName() {
		return txtName;
	}

	public void setTxtName(Text txtName) {
		this.txtName = txtName;
	}

	public Text getTxtStreet() {
		return txtStreet;
	}

	public void setTxtStreet(Text txtStreet) {
		this.txtStreet = txtStreet;
	}

	public Text getTxtFirstName() {
		return txtFirstName;
	}

	public void setTxtFirstName(Text txtFirstName) {
		this.txtFirstName = txtFirstName;
	}

	public Text getTxtSurname() {
		return txtSurname;
	}

	public void setTxtSurname(Text txtSurname) {
		this.txtSurname = txtSurname;
	}

	public Text getTxtJob() {
		return txtJob;
	}

	public void setTxtJob(Text txtJob) {
		this.txtJob = txtJob;
	}

	public Text getTxtPhone() {
		return txtPhone;
	}

	public void setTxtPhone(Text txtPhone) {
		this.txtPhone = txtPhone;
	}

	public Table getTblCredit() {
		return tblCredit;
	}

	public void setTblCredit(Table tblCredit) {
		this.tblCredit = tblCredit;
	}

	public Table getTblDiscount() {
		return tblDiscount;
	}

	public void setTblDiscount(Table tblDiscount) {
		this.tblDiscount = tblDiscount;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		new CustomerView(0);
		Database.getInstance().closeConnection();
	}
}

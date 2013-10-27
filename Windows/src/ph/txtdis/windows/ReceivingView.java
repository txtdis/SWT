package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class ReceivingView extends OrderView {
	private Receiving receiving;
	private ReceivingView receivingView;
	private String partner, referenceType;
	
	protected Combo locationCombo, qualityCombo;
	protected Text expiryInput;

	public ReceivingView() {
	}

	public ReceivingView(int id) {
		super(id);
	}

	@Override
	protected void runClass() {
		report = order = receiving = new Receiving(id);
	}

	@Override
	protected void setTitleBar() {
		postButton = new MasterTitleBar(this, order).getSaveButton();
	}

	@Override
	protected void setHeader() {
		Composite composite = new Compo(shell, 3, GridData.FILL_HORIZONTAL).getComposite();

		Group detail = new Grp(composite, 2, "DETAILS", GridData.FILL_VERTICAL).getGroup();
		idDisplay = new TextDisplayBox(detail, "R/R #", order.getId()).getText();
		locationCombo = new ComboBox(detail, receiving.getLocations(), "LOCATION").getCombo();

		// PARTNER GROUP
		Group partner = new Grp(composite, 5, "PARTNER INFO", GridData.FILL_HORIZONTAL).getGroup();
		partnerIdInput = new TextInputBox(partner, "ID", order.getPartnerId()).getText();
		listButton = new ListButton(partner, "Customer List").getButton();
		partnerDisplay = new TextDisplayBox(partner, "", order.getPartner(), 1).getText();
		addressDisplay = new TextDisplayBox(partner, "ADDRESS", order.getAddress(), 4).getText();
		listButton.setEnabled(false);

		// DETAIL SUBGROUP
		Group receipt = new Grp(composite, 2, "RECEIPT", GridData.FILL_VERTICAL).getGroup();
		dateInput = new TextInputBox(receipt, "DATE", order.getDate()).getText();
		referenceIdInput = new TextInputBox(receipt, "S/O(P/O) #", order.getReferenceId()).getText();
	}

	@Override
	protected void setListener() {
		receivingView = this;
		
		new TextInputter(partnerIdInput, dateInput) {
			@Override
			protected boolean isThePositiveNumberValid() {
				partnerId = numericInput.intValue();
				order.setPartnerId(partnerId);
				String partner = order.getPartner();
				if (partner.isEmpty()) {
					new ErrorDialog("Sorry, Partner #" + partnerId + "\nis not in our system.");
					return false;
				} else {
					partnerDisplay.setText(partner);
					addressDisplay.setText(order.getAddress());
					return true;
				}
			}
		};

		// Received Date Input
		new DateInputter(dateInput, referenceIdInput) {
			@Override
			protected boolean isTheDataInputValid() {
				customer = new Customer();
				helper = new OrderHelper();
				boolean isVendor = customer.isVendor(partnerId);
				Date[] dates = new Date[] {
					date };
				if (!new CalendarDialog(dates, false).isEqual())
					return false;
				if (!isVendor || (isVendor && helper.hasOpenPO(date, partnerId))) {
					order.setDate(date);
					return true;
				} else {
					new ErrorDialog("There are no open P/O's\nfor " + partner);
					return false;
				}
			}
		};

		// Reference ID input
		new TextInputter(referenceIdInput, itemIdInput) {
			@Override
			protected boolean isTheNegativeNumberNotValid() {
				referenceId = numericInput.intValue();
				referenceType = "P/O";
				shouldReturn = false;
				return false;
			}

			@Override
			protected boolean isThePositiveNumberValid() {
				referenceId = numericInput.intValue();
				referenceType = "S/O";
				shouldReturn = false;
				return true;
			}

			@Override
			protected boolean isTheSignedNumberValid() {
				int partnerIdOnOrder = helper.getPartnerId(referenceId);
				boolean isOrderOnFile = partnerId == partnerIdOnOrder;
				if (!isOrderOnFile) {
					new ErrorDialog(referenceType + " #" + Math.abs(referenceId) + " for\n" + partner
					        + "\nis not in on file");
					return false;
				}
				order.setReferenceId(referenceId);
				new ReceivingItemIdEntry(receivingView, order);
				return true;
			}
		};
	}

	@Override
	protected void setFocus() {
		partnerIdInput.setTouchEnabled(true);
		partnerIdInput.setFocus();
		listButton.setEnabled(true);
	}

	public Text getExpiryInput() {
		return expiryInput;
	}

	public Combo getQualityCombo() {
		return qualityCombo;
	}

	public void setQualityCombo(Combo qualityCombo) {
		this.qualityCombo = qualityCombo;
	}

	public static void main(String[] args) {
		// Database.getInstance().getConnection("irene","ayin","localhost");
		//Database.getInstance().getConnection("sheryl", "10-8-91", "localhost");
		Database.getInstance().getConnection("sheryl", "10-8-91", "192.168.1.100");
		Login.setGroup("super_supply");
		new ReceivingView(0);
		Database.getInstance().closeConnection();
	}

}
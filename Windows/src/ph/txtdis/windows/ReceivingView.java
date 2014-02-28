package ph.txtdis.windows;

import java.sql.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class ReceivingView extends OrderView {
	private boolean isVendor;
	private int partnerId, referenceId;
	
	private ReceivingData data;
	private String partner, referenceType;

	protected Combo locationCombo, qualityCombo;
	protected Text expiryInput, idDisplay;
	protected static int QTY_COLUMN = 6;

	public ReceivingView() {
		this(0);
	}

	public ReceivingView(int id) {
		this(new ReceivingData(id));
    }

	public ReceivingView(ReceivingData data) {
		super(data);
		this.data = data;
		type = Type.RECEIVING;
		display();
	}

	@Override
	protected void addSubheader() {
		Composite composite = new Compo(shell, 3, GridData.FILL_HORIZONTAL).getComposite();

		Group detail = new Grp(composite, 2, "DETAILS", GridData.FILL_VERTICAL).getGroup();
		idDisplay = new TextDisplayBox(detail, "R/R #", data.getId()).getText();
		locationCombo = new ComboBox(detail, data.getLocations(), "LOCATION").getCombo();

		// PARTNER GROUP
		Group partner = new Grp(composite, 5, "PARTNER INFO", GridData.FILL_HORIZONTAL).getGroup();
		partnerIdInput = new TextInputBox(partner, "ID", data.getPartnerId()).getText();
		listButton = new ListButton(partner, "Customer List").getButton();
		partnerDisplay = new TextDisplayBox(partner, "", data.getPartner(), 1).getText();
		addressDisplay = new TextDisplayBox(partner, "ADDRESS", data.getAddress(), 4).getText();
		listButton.setEnabled(false);

		// DETAIL SUBGROUP
		Group receipt = new Grp(composite, 2, "RECEIPT", GridData.FILL_VERTICAL).getGroup();
		dateInput = new TextInputBox(receipt, "DATE", data.getDate()).getText();
		referenceIdInput = new TextInputBox(receipt, "S/O(P/O) #", data.getReferenceId()).getText();
	}

	@Override
    protected void setFooter() {
		new EncodingDataFooter(shell, this, data);
    }

	@Override
	protected void addListener() {
		new DataInputter(partnerIdInput, dateInput) {
			@Override
			protected Boolean isPositive() {
				partnerId = number.intValue();
				data.setPartnerId(partnerId);
				partner = data.getPartner();
				if (partner.isEmpty()) {
					new ErrorDialog("Sorry, Partner #" + partnerId + "\nis not in our system.");
					return false;
				} else {
					isVendor = partnerId == DIS.PRINCIPAL;
					partnerDisplay.setText(partner);
					addressDisplay.setText(data.getAddress());
					return true;
				}
			}
		};

		// Received Date Input
		new DataInputter(dateInput, referenceIdInput) {
			@Override
			protected Boolean isNonBlank() {
				Date date = DIS.parseDate(textInput);
				Date[] dates = new Date[] { date };
				if (!new CalendarDialog(dates).isEqual())
					return false;
				if (!isVendor || (isVendor && OrderControl.hasOpenPO(date, partnerId))) {
					data.setDate(date);
					return true;
				} else {
					new ErrorDialog("There are no open P/O's\nfor " + partner);
					return false;
				}
			}
		};

		// Reference ID input
		final ReceivingView view = this;
		new DataInputter(referenceIdInput, itemIdInput) {
			
			@Override
            protected Boolean isNegativeNot() {
				if (data.isMaterialTransfer())
					return true;
				referenceId = number.intValue();
				referenceType = "P/O";
				return null;
            }
			
			@Override
            protected Boolean isPositive() {
				referenceId = number.intValue();
				if (data.isMaterialTransfer()) {
					referenceType = "D/R";
				} else {
					referenceType = "S/O";
					data.setReferenceAnSO(true);
				}
				return null;
            }
			
			

			@Override
            protected boolean isAnyNonZero() {
				String dueDate = " due";
				Date dateOnOrder = OrderControl.getReferenceDueDate(referenceId);
				if (data.isMaterialTransfer()) {
					dateOnOrder = OrderControl.getTransferDate(referenceId);
					if (dateOnOrder == null) {
						new ErrorDialog(referenceType + " #" + Math.abs(referenceId) + " for\n" + partner
						        + "\nis not in on file");
						return false;
					}
				} else {
					int partnerIdOnOrder = OrderControl.getPartnerId(referenceId);
					boolean isOrderOnFile = partnerId == partnerIdOnOrder;
					if (!isOrderOnFile) {
						new ErrorDialog(referenceType + " #" + Math.abs(referenceId) + " for\n" + partner
						        + "\nis not in on file");
						return false;
					}
					if (data.isReferenceAnSO()) {
						dateOnOrder = OrderControl.getReferenceDate(Math.abs(referenceId));
						dueDate = "";
					}
				}
				Date dateOrderIsDue = DIS.addDays(dateOnOrder, DIS.LEAD_TIME);  
				if (!DateUtils.isSameDay(dateOrderIsDue, data.getDate()) && isVendor) {
					new ErrorDialog("R/R date must be\nthe same as " + referenceType + dueDate + "'s: " + dateOrderIsDue);
					return false;
				}
				data.setReferenceId(referenceId);
				new ReceivingItemIdEntry(view, data);
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

	@Override
    public Posting getPosting() {
	    return new ReceivingPosting(data);
    }

	@Override
    public void processUomSelection(String selection) {
        Type quality = Type.GOOD;
        if (data.isReferenceAnSO()) {
        	String partner = data.getPartner();
        	if (OrderControl.isBadOrder(data.getReferenceId()) || partner.equals("ITEM REJECTION"))
        		quality = Type.BAD;
        	else if (partner.equals("ITEM ON-HOLD"))
        		quality = Type.ONHOLD;
        }
        tableItem.setText(4, quality.toString());
        data.setQuality(quality);
        new ItemExpiryInput(this, data, DIS.getDatePerQuality(quality));
    }

	@Override
    public boolean isEnteredItemQuantityValid(String quantity) {
	    return true;
    }

	@Override
    public void processQuantityInput(String quantity, int rowIdx) {
		tableItem.setText(6, quantity);
    }
}
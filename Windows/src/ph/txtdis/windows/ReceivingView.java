package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ReceivingView extends ReportView {
	private Receiving order;
	private OrderHelper helper;
	private CustomerHelper customer;
	private Text txtPartnerId, txtPartnerName, txtAddress, txtDate, txtOrderId, txtTotal, txtRefId;
	private Text txtItemId, txtExpiry, txtQty;
	private Button btnPost, btnList;
	private Combo cmbUom;
	private TableItem tableItem;
	private String partner, refOrderType, refUom, uom, qualityState;
	private String[] uoms;
	private BigDecimal refQty, refQtyPer, qtyPer;
	private Date date, expiry;
	private HashMap<Integer, BigDecimal> itemIdsAndQtys;
	private int id, partnerId, rowIdx, refId, itemId;
	private boolean isVendor, isPO, isSO, isRefUomPack;

	public ReceivingView(int id) {
		this.id = id;
		setProgress();
		setTitleBar();
		setHeader();
		getTable();
		setListener();
		setFocus();
		showReport();
	}

	@Override
	protected void runClass() {
		report = order = new Receiving(id);
	}

	@Override
	protected void setTitleBar() {
		btnPost = new MasterTitleBar(this, order).getBtnPost();
	}

	@Override
	protected void setHeader() {
		Composite cmp = new Compo(shell, 2, GridData.FILL_HORIZONTAL).getComposite();

		// PARTNER GROUP
		Group partnerGrp = new Grp(cmp, 3, "PARTNER INFO", GridData.FILL_HORIZONTAL).getGroup();
		partnerId = order.getPartnerId();
		txtPartnerId = new DataEntry(partnerGrp, "ID", partnerId).getText();
		btnList = new ListButton(partnerGrp, "Customer List").getButton();
		txtPartnerName = new DataDisplay(partnerGrp, "NAME", order.getPartner(), 2).getText();
		txtAddress = new DataDisplay(partnerGrp, "ADDRESS", order.getAddress(), 2).getText();
		btnList.setEnabled(false);

		// DETAIL SUBGROUP
		Group detailGrp = new Grp(cmp, 2, "DETAILS", GridData.FILL_VERTICAL).getGroup();
		txtDate = new DataEntry(detailGrp, "DATE", order.getPostDate()).getText();
		txtOrderId = new DataEntry(detailGrp, "R/R #", order.getId()).getText();
		txtRefId = new DataEntry(detailGrp, "S/O(P/O) #", order.getRefId()).getText();
	}

	@Override
	protected void setListener() {
		// Partner ID input
		new DataInput(txtPartnerId, txtDate) {
			@Override
			protected boolean isDataInputValid() {
				partnerId = Integer.parseInt(string);
				order.setPartnerId(partnerId);
				partner = order.getPartner();
				if (partner == null) {
					new ErrorDialog("Sorry, Partner #" + partnerId + "\nis not in our system.");
					return false;
				} else {
					txtPartnerName.setText(partner);
					txtAddress.setText(order.getAddress());
					return true;
				}
			}
		};

		// Received Date Input
		new DataInput(txtDate, txtRefId, order.getPostDate().toString()) {
			@Override
			protected boolean isInputValid() {
				customer = new CustomerHelper();
				helper = new OrderHelper();
				isVendor = customer.isVendor(partnerId);
				date = DIS.parseDate(string);
				Date[] dates = new Date[] {
					date };
				if (!new CalendarDialog(dates, false).isEqual())
					return false;
				if (!isVendor || (isVendor && helper.hasOpenPO(date, partnerId))) {
					order.setPostDate(date);
					return true;
				} else {
					new ErrorDialog("There are no open P/O's\nfor " + partner);
					return false;
				}
			}
		};

		// Reference ID input
		new DataInput(txtRefId, txtItemId) {
			@Override
			protected boolean isDataInputValid() {
				refId = Integer.parseInt(string);
				isPO = refId < 0; // negative reference Ids for P/Os
				isSO = !isPO;
				refOrderType = isPO ? "P/O" : "S/O";
				int partnerIdOnOrder = helper.getPartnerId(refId);
				boolean isOrderOnFile = partnerId == partnerIdOnOrder;
				if (!isOrderOnFile) {
					new ErrorDialog(refOrderType + " #" + Math.abs(refId) + " for\n" + partner + "\nis not in on file");
					return false;
				}
				int rrId = helper.getRRid(refId);
				if (rrId != 0) {
					new ErrorDialog("R/R #" + rrId + "\nhas been used for\n" + refOrderType + " #" + Math.abs(refId));
					return false;
				}
				order.setRefId(refId);
				tableItem = new TableItem(table, SWT.NONE, rowIdx);
				setItemIdInput();
				return true;
			}
		};
	}

	// Item ID input
	private void setItemIdInput() {
		final Button btnItem = new TableButton(tableItem, rowIdx, 0, "Item List").getButton();
		txtItemId = new TableInput(tableItem, rowIdx, 1, 0).getText();
		txtItemId.setTouchEnabled(true);
		txtItemId.setFocus();
		new DataInput(txtItemId, txtExpiry) {
			@Override
			protected boolean isBlankInputNotValid() {
				if (rowIdx == 0) {
					return false;
				} else {
					setNext(btnPost);
					return true;
				}
			}

			@Override
			protected boolean isDataInputValid() {
				itemId = Integer.parseInt(string);
				ItemHelper item = new ItemHelper();
				String itemName = item.getName(itemId);

				if (itemName.isEmpty()) {
					new ErrorDialog("Item ID " + itemId + "\nis not on file.");
					return false;
				}

				Object[] refQtyAndUom = item.getRefQtyAndUOM(itemId, refId);
				if (refQtyAndUom == null) {
					new ErrorDialog(itemName + "\nis not in " + refOrderType + " #" + Math.abs(refId));
					return false;
				} else {
					refQty = (BigDecimal) refQtyAndUom[0];
					refUom = (String) refQtyAndUom[1];
					refQtyPer = (BigDecimal) refQtyAndUom[2];
					isRefUomPack = refUom.equals("PK");
				}

				qualityState = "GOOD";
				if (isSO) {
					if (helper.isRMA(refId) || partner.equals("ITEM REJECTION")) {
						qualityState = "BAD";
					} else if (partner.equals("ITEM ON-HOLD")) {
						qualityState = "ON-HOLD";
					}
				}

				tableItem.setText(0, String.valueOf(rowIdx + 1));
				tableItem.setText(1, String.valueOf(itemId));
				tableItem.setText(2, itemName);
				tableItem.setText(4, qualityState);
				txtItemId.dispose();
				btnPost.setEnabled(false);
				btnItem.dispose();
				if (id == 0 && isSO && !isRefUomPack) {
					uoms = new UOM().getSellingUoms(itemId);
					cmbUom = new TableSelection(tableItem, rowIdx, 3).getCombo();
					cmbUom.setItems(uoms);
					cmbUom.select(1);
					cmbUom.setEnabled(true);
					cmbUom.setFocus();
					setUomSelector();
				} else {
					uom = refUom;
					tableItem.setText(3, uom);
					setItemExpiryInput();
				}
				return true;
			}
		};
	}

	// Item UOM selector
	private void setUomSelector() {
		new DataSelector(cmbUom, txtExpiry) {
			@Override
			protected void doWhenSelected() {
				uom = cmbUom.getText();
				qtyPer = new QtyPerUOM().get(itemId, new UOM(uom).getId());
				cmbUom.dispose();
				tableItem.setText(3, uom);
				setItemExpiryInput();
			}
		};
	}

	// Item expiry input listener
	private void setItemExpiryInput() {
		txtExpiry = new TableInput(tableItem, rowIdx, 5, qualityState.equals("BAD") ? DIS.TODAY : DIS.TOMORROW)
		        .getText();
		txtExpiry.setTouchEnabled(true);
		txtExpiry.setFocus();
		new DataInput(txtExpiry, txtQty) {
			@Override
			protected boolean isInputValid() {
				expiry = DIS.parseDate(string);
				if ((qualityState.equals("GOOD") || qualityState.equals("ON-HOLD"))
				        && DateUtils.truncatedCompareTo(expiry, date, Calendar.DAY_OF_MONTH) < 1) {
					new ErrorDialog("Good/on-hold items\ncannot be expired");
					return false;
				} else {
					tableItem.setText(5, expiry.toString());
					txtExpiry.dispose();
					setItemQtyInput();
					return true;
				}
			}
		};
	}

	// Item quantity input
	private void setItemQtyInput() {
		txtQty = new TableInput(tableItem, rowIdx, 6, BigDecimal.ZERO).getText();
		txtQty.setTouchEnabled(true);
		txtQty.setFocus();
		new DataInput(txtQty, txtItemId) {
			@Override
			protected boolean isDataInputValid() {
				BigDecimal qty = new BigDecimal(string);
				if (qty.compareTo(BigDecimal.ZERO) < 1)
					return false;
				BigDecimal qtyInRefUom = uom.equals(refUom) ? qty : qty.multiply(qtyPer).divide(refQtyPer,
				        BigDecimal.ROUND_HALF_EVEN);
				BigDecimal balance = refQty;
				BigDecimal total = BigDecimal.ZERO;
				itemIdsAndQtys = order.getItemIdsAndQtys();
				if (itemIdsAndQtys.containsKey(itemId)) {
					total = itemIdsAndQtys.get(itemId);
					balance = refQty.subtract(total);
				}
				if (balance.compareTo(qtyInRefUom) < 0) {
					new ErrorDialog("Only\n" + balance + refUom + "\n"
					        + (isSO ? "left" : "is on P/O"));
					return false;
				}
				itemIdsAndQtys.put(itemId, total.add(qtyInRefUom));
				tableItem.setText(6, DIS.NO_COMMA_DECIMAL.format(qty));
				txtQty.dispose();
				order.getItemIds().add(itemId);
				order.getUomIds().add(new UOM(uom).getId());
				order.getQualityStates().add(qualityState);
				order.getExpiries().add(expiry);
				order.getQtys().add(qty);
				tableItem = new TableItem(table, SWT.NONE, ++rowIdx);
				setItemIdInput();
				return true;
			}
		};
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
	}//

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
		// Database.getInstance().getConnection("irene","ayin");
		Database.getInstance().getConnection("sheryl", "10-8-91");
		Login.setGroup("super_supply");
		new ReceivingView(0);
		Database.getInstance().closeConnection();
	}

}
package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class CustomerView extends OrderView {
	private boolean isEditable, isThereAnError;
	private int customerId, hqId, rowIdx;
	private Combo cityCombo, districtCombo, provinceCombo, channelCombo, routeCombo;
	private ComboBox cityComboBox, districtComboBox, provinceComboBox, channelComboBox;
	private Text idInput, hqText, hqInput, smsIdInput, nameInput, streetInput, firstNameInput, surnameInput,
	        designationInput, phoneInput, routeStartDateInput, creditLimitInput, creditTermInput, gracePeriodInput,
	        creditStartDateInput, familyIdInput, firstLevelDiscountInput, secondLevelDiscountInput,
	        discountStartDateInput;
	private Table creditTable, discountTable, routeTable;
	private Customer customer;

	public CustomerView(int customerId) {
		this(customerId, false);
	}

	public CustomerView(int customerId, boolean isEditable) {
		super();
		this.customerId = customerId;
		this.isEditable = isEditable;
		setProgress();
		setTitleBar();
		setHeader();
		getTable();
		if (customer.getId() == 0 || isEditable)
			setListener();
		setFocus();
		showReport();
	}

	@Override
	protected void runClass() {
		report = order = customer = new Customer(customerId, isEditable);
	}

	@Override
	protected void setTitleBar() {
		postButton = new MasterTitleBar(this, order) {
			@Override
			protected void insertButtons() {
				new BackwardButton(buttons, report);
				new ForwardButton(buttons, report);
				createEditButton();
			}

			private void createEditButton() {
				if (!isEditable && customerId != 0) {
					new ReportButton(buttons, report, "Write", "Edit Customer Data") {
						@Override
						protected void doWhenSelected() {
							buttons.getShell().dispose();
							new CustomerView(report.getId(), true);
						}
					};
				}
			}
		}.getSaveButton();
		if (isEditable)
			postButton.setEnabled(true);
	}

	@Override
	protected void setHeader() {
		Composite header = new Compo(shell, 2, GridData.FILL_HORIZONTAL).getComposite();

		Composite left = new Compo(header, 2, GridData.END).getComposite();
		Group partner = new Grp(left, 5, "PARTNER", SWT.BEGINNING, SWT.BEGINNING, true, false, 2, 1).getGroup();

		idInput = new TextInputBox(partner, "ID", customer.getId()).getText();

		TextInputBox smsIdInputBox = new TextInputBox(partner, "SMS ID", customer.getSmsId(), 1, 4);
		smsIdInput = smsIdInputBox.getText();
		smsIdInputBox.getLabel().setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, true, 2, 1));

		nameInput = new TextInputBox(partner, "NAME", customer.getName(), 4, 32).getText();

		hqId = customer.getHqId();
		hqInput = new TextInputBox(partner, "BRANCH-OF ID", hqId).getText();

		listButton = new ListButton(partner, customer.getModule()).getButton();
		listButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 3, 1));

		String hq = hqId == 0 ? "" : customer.getName(hqId);
		hqText = new TextDisplayBox(partner, "HEADQUARTER", hq, 4).getText();

		channelComboBox = new ComboBox(partner, customer.getChannels(), "CHANNEL", customer.getChannel());
		channelCombo = channelComboBox.getCombo();
		channelCombo.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));

		Group address = new Grp(left, 2, "ADDRESS", SWT.BEGINNING, SWT.BEGINNING, true, false, 2, 1).getGroup();
		provinceComboBox = new ComboBox(address, customer.getProvinces(), "PROVINCE/CITY", customer.getProvince());
		provinceCombo = provinceComboBox.getCombo();
		cityComboBox = new ComboBox(address, customer.getCities(), "TOWN/DISTRICT", customer.getCity());
		cityCombo = cityComboBox.getCombo();
		districtComboBox = new ComboBox(address, customer.getDistricts(), "BARANGAY", customer.getDistrict());
		districtCombo = districtComboBox.getCombo();
		streetInput = new TextInputBox(address, "STREET", customer.getStreet(), 1, 32).getText();

		Group contact = new Grp(left, 2, "CONTACT", SWT.FILL, SWT.FILL, true, false, 2, 1).getGroup();
		firstNameInput = new TextInputBox(contact, "NAME", customer.getFirstName(), 1, 16).getText();
		surnameInput = new TextInputBox(contact, "SURNAME", customer.getSurname(), 1, 16).getText();
		designationInput = new TextInputBox(contact, "DESIGNATION", customer.getDesignation(), 1, 16).getText();
		phoneInput = new TextInputBox(contact, "PHONE", customer.getPhone()).getText();

		Composite right = new Compo(header, 1, SWT.RIGHT).getComposite();

		Group route = new Grp(right, 1, "ROUTE", GridData.HORIZONTAL_ALIGN_CENTER).getGroup();
		routeTable = new ReportTable(route, customer.getRouteData(), customer.getRouteHeaders(), "", 70, true) {
			@Override
			protected void doubleClickListener() {
			}
		}.getTable();
		routeTable.setTopIndex(routeTable.getItemCount() - 1);

		Group credit = new Grp(right, 1, "CREDIT", GridData.HORIZONTAL_ALIGN_CENTER).getGroup();
		creditTable = new ReportTable(credit, customer.getCreditData(), customer.getCreditHeaders(), "", 70, true) {
			@Override
			protected void doubleClickListener() {
			}
		}.getTable();
		creditTable.setTopIndex(creditTable.getItemCount() - 1);

		Group discount = new Grp(right, 1, "DISCOUNT", GridData.HORIZONTAL_ALIGN_CENTER).getGroup();
		discountTable = new ReportTable(discount, customer.getDiscountData(), customer.getDiscountHeaders(), "", 85,
		        true) {
			@Override
			protected void doubleClickListener() {
			}
		}.getTable();
		discountTable.setTopIndex(discountTable.getItemCount() - 3);
	}

	@Override
	public Table getTable() {
		// This has no table but superclass has
		return null;
	}

	@Override
	protected void setListener() {
		if (!isEditable) {
			new TextInputter(smsIdInput, nameInput) {
				@Override
				protected boolean isTheDataInputValid() {
					if (customer.hasSmsId(textInput)) {
						new ErrorDialog(textInput + " has been used;\ntry another.");
						shouldReturn = true;
						return false;
					} else {
						customer.setSmsId(textInput);
						shouldReturn = true;
						return true;
					}
				}
			};

			new TextInputter(nameInput, hqInput) {
				@Override
				protected boolean isTheDataInputValid() {
					customer.setName(textInput);
					shouldReturn = true;
					return true;
				}
			};

			new TextInputter(hqInput, channelCombo) {
				@Override
				protected boolean isThePositiveNumberValid() {
					hqId = numericInput.intValue();
					String hq = customer.getName(hqId);
					if (hq.isEmpty()) {
						new ErrorDialog("Partner #" + hqId + "is not file.");
						hqText.setText("");
						shouldReturn = true;
						return false;
					} else {
						customer.setHqId(hqId);
						hqText.setText(hq);
						shouldReturn = true;
						return true;
					}
				}

				@Override
				protected boolean isABlankInputNotValid() {
					shouldReturn = true;
					return false;
				}
			};
		}

		// Editables
		new ComboSelector(channelComboBox, provinceComboBox) {
			@Override
			protected void doAfterSelection() {
				switch (selection) {
					case "OTHERS":
					case "ROUTE":
						provinceCombo.removeAll();
						cityCombo.removeAll();
						districtCombo.removeAll();
						setNext(postButton);
						break;
					default:
						customer.setChannel(selection);
				}
			}
		};

		new ComboSelector(provinceComboBox, cityComboBox) {
			@Override
			protected void doAfterSelection() {
				int cityId = new Area(provinceCombo.getText()).getId();
				cityCombo.setItems(new Area(cityId).getAreas());
				cityCombo.select(0);
				customer.setProvince(selection);
				customer.setCity(cityCombo.getText());
				int districtId = new Area(cityCombo.getText()).getId();
				districtCombo.setItems(new Area(districtId).getAreas());
				districtCombo.select(0);
			}
		};

		new ComboSelector(cityComboBox, districtComboBox) {
			@Override
			protected void doAfterSelection() {
				int districtId = new Area(cityCombo.getText()).getId();
				districtCombo.setItems(new Area(districtId).getAreas());
				districtCombo.select(0);
				customer.setCity(selection);
				customer.setDistrict(districtCombo.getText());
			}
		};

		new ComboSelector(districtComboBox, streetInput) {
			@Override
			protected void doAfterSelection() {
				customer.setDistrict(selection);
			}
		};

		new TextInputter(streetInput, firstNameInput) {
			@Override
			protected boolean isABlankInputNotValid() {
				return false;
			}

			@Override
			protected boolean isTheDataInputValid() {
				customer.setStreet(textInput);
				return true;
			}
		};

		new TextInputter(firstNameInput, surnameInput) {
			@Override
			protected boolean isABlankInputNotValid() {
				surnameInput.setText("");
				designationInput.setText("");
				phoneInput.setText("");

				customer.setFirstName("");
				customer.setSurname("");
				customer.setDesignation("");
				customer.setPhone(0);

				setNext(getUneditedTable());
				shouldReturn = true;
				return false;
			}

			@Override
			protected boolean isTheDataInputValid() {
				customer.setFirstName(textInput);
				setNext(surnameInput);
				shouldReturn = true;
				return true;
			}
		};

		new TextInputter(surnameInput, designationInput) {
			@Override
			protected boolean isABlankInputNotValid() {
				return false;
			}

			@Override
			protected boolean isTheDataInputValid() {
				customer.setSurname(textInput);
				shouldReturn = true;
				return true;
			}
		};

		new TextInputter(designationInput, phoneInput) {
			@Override
			protected boolean isABlankInputNotValid() {
				return false;
			}

			@Override
			protected boolean isTheDataInputValid() {
				customer.setDesignation(textInput);
				shouldReturn = true;
				return true;
			}
		};

		new TextInputter(phoneInput, routeCombo) {
			@Override
			protected boolean isABlankInputNotValid() {
				customer.setPhone(0);
				setNext(getUneditedTable());
				shouldReturn = true;
				return false;
			}

			@Override
			protected boolean isThePositiveNumberValid() {
				customer.setPhone(numericInput.longValue());
				numericInput = BigDecimal.ZERO;
				setNext(getUneditedTable());
				shouldReturn = true;
				return true;
			}
		};
	}

	private Control getUneditedTable() {
		if (customer.isRouteChanged()) {
			if (customer.isCreditChanged()) {
				if (customer.isDiscountChanged())
					return postButton;
				else {
					setFamilyIdInput();
					return familyIdInput;
				}
			} else {
				setCreditLimitInput();
				return creditLimitInput;
			}
		} else {
			setRouteCombo();
			return routeCombo;
		}
	}

	private void setRouteCombo() {
		rowIdx = routeTable.getItemCount();
		tableItem = new TableItem(routeTable, SWT.NONE);
		tableItem.setText(0, String.valueOf(rowIdx + 1));
		tableItem.setBackground(rowIdx % 2 != 0 ? UI.GRAY : UI.WHITE);
		routeTable.setTopIndex(rowIdx);
		routeCombo = new TableCombo(tableItem, 1, new Route().getList(), "").getCombo();
		routeCombo.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(routeCombo, tableItem);
		new ComboSelector(routeCombo, routeStartDateInput) {
			@Override
			protected void doAfterSelection() {
				routeCombo.dispose();
				if (selection.isEmpty()) {
					tableItem.dispose();
					if (Login.getGroup().equals("super_user")) {
						setNext(creditLimitInput);
						setCreditLimitInput();
					} else {
						setNext(postButton);
					}
				} else {
					customer.setRoute(selection);
					tableItem.setText(1, selection);
					setRouteStartDateInput();
				}
			}
		};
	}

	private void setRouteStartDateInput() {
		routeStartDateInput = new TableTextInput(tableItem, rowIdx, 2, DIS.TODAY).getText();
		routeStartDateInput.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(routeStartDateInput, tableItem);
		new DateInputter(routeStartDateInput, postButton) {
			@Override
			protected boolean isTheDataInputValid() {
				if (customer.isRouteStartDateOnFile(date, customerId)) {
					isThereAnError = true;
					new ErrorDialog("Only one(1) route update\nper customer per day\nis allowed.");
					return false;
				}
				routeStartDateInput.dispose();
				customer.setRouteStartDate(date);
				tableItem.setText(2, textInput);
				tableItem.setText(3, customer.getInputter());
				if (Login.getGroup().equals("super_user")) {
					setNext(creditLimitInput);
					setCreditLimitInput();
				}
				shouldReturn = true;
				return true;
			}
		};
	}

	private void setCreditLimitInput() {
		rowIdx = creditTable.getItemCount();
		tableItem = new TableItem(creditTable, SWT.NONE);
		tableItem.setText(0, String.valueOf(rowIdx + 1));
		tableItem.setBackground(rowIdx % 2 != 0 ? UI.GRAY : UI.WHITE);
		routeTable.setTopIndex(rowIdx);
		creditLimitInput = new TableTextInput(tableItem, rowIdx, 1, 0).getText();
		creditLimitInput.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(creditLimitInput, tableItem);
		new TextInputter(creditLimitInput, creditTermInput) {
			@Override
			protected boolean isABlankInputNotValid() {
				creditLimitInput.dispose();
				tableItem.dispose();
				setNext(familyIdInput);
				setFamilyIdInput();
				shouldReturn = true;
				return false;
			}

			@Override
			protected boolean isThePositiveNumberValid() {
				creditLimitInput.dispose();
				customer.setCreditLimit(numericInput);
				tableItem.setText(1, DIS.INTEGER.format(numericInput));
				setCreditTermInput();
				shouldReturn = true;
				return true;
			}
		};
	}

	private void setCreditTermInput() {
		creditTermInput = new TableTextInput(tableItem, rowIdx, 2, 0).getText();
		creditTermInput.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(creditTermInput, tableItem);
		new TextInputter(creditTermInput, gracePeriodInput) {
			@Override
			protected boolean isThePositiveNumberValid() {
				int creditTerm = numericInput.intValue();
				creditTermInput.dispose();
				customer.setCreditTerm(creditTerm);
				tableItem.setText(2, DIS.INTEGER.format(creditTerm));
				setGracePeriodInput();
				shouldReturn = true;
				return true;
			}
		};
	}

	private void setGracePeriodInput() {
		gracePeriodInput = new TableTextInput(tableItem, rowIdx, 3, 0).getText();
		gracePeriodInput.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(gracePeriodInput, tableItem);
		new TextInputter(gracePeriodInput, creditStartDateInput) {
			@Override
			protected boolean isThePositiveNumberValid() {
				int gracePeriod = numericInput.intValue();
				gracePeriodInput.dispose();
				customer.setGracePeriod(gracePeriod);
				tableItem.setText(3, DIS.INTEGER.format(gracePeriod));
				setCreditStartDateInput();
				shouldReturn = true;
				return true;
			}
		};
	}

	private void setCreditStartDateInput() {
		creditStartDateInput = new TableTextInput(tableItem, rowIdx, 4, DIS.TODAY).getText();
		creditStartDateInput.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(creditStartDateInput, tableItem);
		new DateInputter(creditStartDateInput, familyIdInput) {
			@Override
			protected boolean isTheDataInputValid() {
				if (date.before(DIS.TODAY)) {
					isThereAnError = true;
					new ErrorDialog("Date cannot be\nearlier than today.");
					return false;
				}
				if (customer.isCreditStartDateOnFile(date, customerId)) {
					isThereAnError = true;
					new ErrorDialog("Only one(1) credit update\nper customer per day\nis allowed.");
					return false;
				}
				creditStartDateInput.dispose();
				customer.setCreditStartDate(date);
				tableItem.setText(4, textInput);
				tableItem.setText(5, customer.getInputter());
				setFamilyIdInput();
				shouldReturn = true;
				return true;
			}
		};
	}

	private void setFamilyIdInput() {
		rowIdx = discountTable.getItemCount();
		tableItem = new TableItem(discountTable, SWT.NONE);
		tableItem.setText(0, String.valueOf(rowIdx + 1));
		tableItem.setBackground(rowIdx % 2 != 0 ? UI.GRAY : UI.WHITE);
		discountTable.setTopIndex(rowIdx - 3);
		familyIdInput = new TableTextInput(tableItem, rowIdx, 1, 0).getText();
		familyIdInput.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(familyIdInput, tableItem);
		new TextInputter(familyIdInput, creditTermInput) {
			@Override
			protected boolean isABlankInputNotValid() {
				familyIdInput.dispose();
				tableItem.dispose();
				setNext(postButton);
				shouldReturn = true;
				return false;
			}

			@Override
			protected boolean isThePositiveNumberValid() {
				int familyId = numericInput.intValue();
				String family = new ItemHelper().getFamily(-familyId);
				if (family.isEmpty()) {
					isThereAnError = true;
					new ErrorDialog("Item Family ID#" + familyId + "\nis not on file.");
					return false;
				}
				familyIdInput.dispose();
				customer.setFamilyId(-familyId);
				tableItem.setText(1, String.valueOf(familyId));
				tableItem.setText(2, family);
				setFirstLevelDiscountInput();
				shouldReturn = true;
				return true;
			}
		};
	}

	private void disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(final Control control,
	        final TableItem tableItem) {
		control.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!control.isDisposed() && !isThereAnError) {
					control.dispose();
					tableItem.dispose();
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				isThereAnError = false;
			}
		});
	}

	private void setFirstLevelDiscountInput() {
		firstLevelDiscountInput = new TableTextInput(tableItem, rowIdx, 3, 0).getText();
		firstLevelDiscountInput.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(firstLevelDiscountInput, tableItem);
		new TextInputter(firstLevelDiscountInput, secondLevelDiscountInput) {
			@Override
			protected boolean isThePositiveNumberValid() {
				firstLevelDiscountInput.dispose();
				customer.setFirstLevelDiscount(numericInput);
				tableItem.setText(3, DIS.TWO_PLACE_DECIMAL.format(numericInput));
				setSecondLevelDiscountInput();
				shouldReturn = true;
				return true;
			}
		};
	}

	private void setSecondLevelDiscountInput() {
		secondLevelDiscountInput = new TableTextInput(tableItem, rowIdx, 4, 0).getText();
		secondLevelDiscountInput.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(secondLevelDiscountInput, tableItem);
		new TextInputter(secondLevelDiscountInput, discountStartDateInput) {
			@Override
			protected boolean isABlankInputNotValid() {
				secondLevelDiscountInput.dispose();
				setNext(discountStartDateInput);
				setDiscountStartDateInput();
				shouldReturn = true;
				return false;
			}
			@Override
			protected boolean isThePositiveNumberValid() {
				secondLevelDiscountInput.dispose();
				customer.setSecondLevelDiscount(numericInput);
				tableItem.setText(4, DIS.TWO_PLACE_DECIMAL.format(numericInput));
				setDiscountStartDateInput();
				shouldReturn = true;
				return true;
			}
		};
	}

	private void setDiscountStartDateInput() {
		discountStartDateInput = new TableTextInput(tableItem, rowIdx, 5, DIS.TODAY).getText();
		discountStartDateInput.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(discountStartDateInput, tableItem);
		new DateInputter(discountStartDateInput, postButton) {
			@Override
			protected boolean isTheDataInputValid() {
				if (date.before(DIS.TODAY)) {
					isThereAnError = true;
					new ErrorDialog("Date cannot be\nearlier than today.");
					return false;
				}
				if (customer.isDiscountStartDateOnFile(date, customerId)) {
					isThereAnError = true;
					new ErrorDialog("Only one(1) discount update\nper customer per day\nis allowed.");
					return false;
				}
				discountStartDateInput.dispose();
				customer.setDiscountStartDate(date);
				tableItem.setText(5, textInput);
				tableItem.setText(6, customer.getInputter());
				shouldReturn = true;
				return true;
			}
		};
	}

	@Override
	protected void setFocus() {
		if (!isEditable) {
			smsIdInput.setTouchEnabled(true);
			smsIdInput.setFocus();
		} else {
			phoneInput.setFocus();
		}
	}

	public Button getPostButton() {
		return postButton;
	}

	public void setPostButton(Button btnPost) {
		this.postButton = btnPost;
	}

	public Combo getCmbCity() {
		return cityCombo;
	}

	public void setCmbCity(Combo cmbCity) {
		this.cityCombo = cmbCity;
	}

	public Combo getCmbDistrict() {
		return districtCombo;
	}

	public void setCmbDistrict(Combo cmbDistrict) {
		this.districtCombo = cmbDistrict;
	}

	public Combo getCmbProvince() {
		return provinceCombo;
	}

	public void setCmbProvince(Combo cmbProvince) {
		this.provinceCombo = cmbProvince;
	}

	public Combo getCmbChannel() {
		return channelCombo;
	}

	public void setCmbChannel(Combo cmbChannel) {
		this.channelCombo = cmbChannel;
	}

	public Text getIdInput() {
		return idInput;
	}

	public void setTxtId(Text txtId) {
		this.idInput = txtId;
	}

	public Text getTxtSmsId() {
		return smsIdInput;
	}

	public void setTxtSmsId(Text txtSmsId) {
		this.smsIdInput = txtSmsId;
	}

	public Text getTxtName() {
		return nameInput;
	}

	public void setTxtName(Text txtName) {
		this.nameInput = txtName;
	}

	public Text getTxtStreet() {
		return streetInput;
	}

	public void setTxtStreet(Text txtStreet) {
		this.streetInput = txtStreet;
	}

	public Text getTxtFirstName() {
		return firstNameInput;
	}

	public void setTxtFirstName(Text txtFirstName) {
		this.firstNameInput = txtFirstName;
	}

	public Text getTxtSurname() {
		return surnameInput;
	}

	public void setTxtSurname(Text txtSurname) {
		this.surnameInput = txtSurname;
	}

	public Text getTxtJob() {
		return designationInput;
	}

	public void setTxtJob(Text txtJob) {
		this.designationInput = txtJob;
	}

	public Text getTxtPhone() {
		return phoneInput;
	}

	public void setTxtPhone(Text txtPhone) {
		this.phoneInput = txtPhone;
	}

	public Table getTblCredit() {
		return creditTable;
	}

	public void setTblCredit(Table tblCredit) {
		this.creditTable = tblCredit;
	}

	public Table getTblDiscount() {
		return discountTable;
	}

	public void setTblDiscount(Table tblDiscount) {
		this.discountTable = tblDiscount;
	}

	public static void main(String[] args) {
		//Database.getInstance().getConnection("maricel", "maricel", "localhost");
		//Database.getInstance().getConnection("badette", "013094", "192.168.1.100");
		Database.getInstance().getConnection("badette", "013094", "localhost");
		new CustomerView(0);
		Database.getInstance().closeConnection();
	}
}

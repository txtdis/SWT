package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;

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

public class CustomerView extends InputView {

	private final int START_DATE_COLUMN = 5;
	private boolean isError;
	private int hqId;
	private Button editButton;
	private Combo cityCombo, districtCombo, provinceCombo, channelCombo, routeCombo;
	private ComboBox cityComboBox, districtComboBox, provinceComboBox, channelComboBox;
	private Composite body;
	private CustomerData customer;
	private Text hqText, hqInput, smsInput, nameInput, streetInput, firstNameInput, surnameInput, designationInput,
	        phoneInput, routeStartDateInput, creditLimitInput, creditTermInput, gracePeriodInput, creditStartDateInput,
	        familyIdInput, discount1Input, discount2Input, discountStartDateInput;
	private Table creditTable, discountTable, routeTable;

	public CustomerView() {
		this(0);
	}

	public CustomerView(int id) {
		this(new CustomerData(id));
	}

	public CustomerView(CustomerData data) {
		super(data);
		type = Type.CUSTOMER;
		customer = data;
		display();
		show();
	}

	@Override
	protected void addHeader() {
		new Header(this, customer) {
			@Override
			protected void layButtons() {
				new ImgButton(buttons, Type.NEW, type);
				new ImgButton(buttons, Type.BACKWARD, view);
				new ImgButton(buttons, Type.OPEN, view);
				new ImgButton(buttons, Type.FORWARD, view);
				if (!User.isSales())
					return;
				if (id != 0)
					editButton = new ImgButton(buttons, Type.EDIT, view).getButton();
				postButton = new ImgButton(buttons, Type.SAVE, view).getButton();
			}
		};
	}

	@Override
	protected void addSubheader() {
		body = new Compo(shell, 2).getComposite();

		Composite left = new Compo(body, 2, GridData.END).getComposite();
		Group partner = new Grp(left, 5, "PARTNER", SWT.BEGINNING, SWT.BEGINNING, true, false, 2, 1).getGroup();

		idDisplay = new TextDisplayBox(partner, "ID", id).getText();

		TextInputBox smsIdInputBox = new TextInputBox(partner, "SMS ID", customer.getSmsId(), 1, 4);
		smsInput = smsIdInputBox.getText();
		smsIdInputBox.getLabel().setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, true, 2, 1));

		nameInput = new TextInputBox(partner, "NAME", customer.getName(), 4, 32).getText();

		hqId = customer.getHqId();
		hqInput = new TextInputBox(partner, "BRANCH-OF ID", hqId).getText();

		listButton = new ListButton(partner, Type.CUSTOMER_LIST.getName()).getButton();
		listButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 3, 1));

		String hq = hqId == 0 ? "" : Customer.getName(hqId);
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
	}

	@Override
	public void addTable() {
		Composite right = new Compo(body, 1, SWT.RIGHT).getComposite();

		Group route = new Grp(right, 1, "ROUTE", GridData.HORIZONTAL_ALIGN_CENTER).getGroup();
		routeTable = new ReportTable(route, customer.getRouteData(), customer.getRouteHeaders(), 70).getTable();
		routeTable.setTopIndex(routeTable.getItemCount() - 1);

		Group credit = new Grp(right, 1, "CREDIT", GridData.HORIZONTAL_ALIGN_CENTER).getGroup();
		creditTable = new ReportTable(credit, customer.getCreditData(), customer.getCreditHeaders(), 70).getTable();
		creditTable.setTopIndex(creditTable.getItemCount() - 1);

		Group discount = new Grp(right, 1, "DISCOUNT", GridData.HORIZONTAL_ALIGN_CENTER).getGroup();
		discountTable = new ReportTable(discount, customer.getDiscountData(), customer.getDiscountHeaders(), 85)
		        .getTable();
		discountTable.setTopIndex(discountTable.getItemCount() - 3);
	}

	@Override
	protected void addListener() {
		if (customer.getId() == 0) {
			new DataInputter(smsInput, nameInput) {
				@Override
				protected Boolean isNonBlank() {
					if (Customer.isShortIdOnFile(textInput)) {
						new ErrorDialog(textInput + " has been used;\ntry another.");
						return false;
					} else {
						customer.setSmsId(textInput);
						return true;
					}
				}

			};

			new DataInputter(nameInput, hqInput) {
				@Override
				protected Boolean isNonBlank() {
					customer.setName(textInput);
					return true;
				}
			};

			new DataInputter(hqInput, channelCombo) {
				private String hq;

				@Override
				protected Boolean isBlankNot() {
					return true;
				}

				@Override
				protected Boolean isPositive() {
					hqId = number.intValue();
					if (!Customer.isOnFile(hqId)) {
						return false;
					} else {
						hq = Customer.getName(hqId);
						customer.setHqId(hqId);
						hqText.setText(hq);
						return true;
					}
				}
			};
		}

		new ComboSelector(channelComboBox, provinceComboBox) {

			@Override
			protected void processSelection() {
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
			protected void processSelection() {
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
			protected void processSelection() {
				int districtId = new Area(cityCombo.getText()).getId();
				districtCombo.setItems(new Area(districtId).getAreas());
				districtCombo.select(0);
				customer.setCity(selection);
				customer.setDistrict(districtCombo.getText());
			}
		};

		new ComboSelector(districtComboBox, streetInput) {

			@Override
			protected void processSelection() {
				customer.setDistrict(selection);
			}
		};

		new DataInputter(streetInput, firstNameInput) {

			@Override
			protected Boolean isNonBlank() {
				customer.setStreet(textInput);
				return true;
			}
		};

		new DataInputter(firstNameInput, surnameInput) {

			@Override
			protected Boolean isBlankNot() {
				surnameInput.setText("");
				designationInput.setText("");
				phoneInput.setText("");

				customer.setFirstName("");
				customer.setSurname("");
				customer.setDesignation("");
				customer.setPhone(0);

				setNext(getUneditedTable());
				return false;
			}

			@Override
			protected Boolean isNonBlank() {
				customer.setFirstName(textInput);
				setNext(surnameInput);
				return true;
			}
		};

		new DataInputter(surnameInput, designationInput) {
			@Override
			protected Boolean isBlankNot() {
				return true;
			}

			@Override
			protected Boolean isNonBlank() {
				customer.setSurname(textInput);
				return true;
			}
		};

		new DataInputter(designationInput, phoneInput) {
			@Override
			protected Boolean isBlankNot() {
				return true;
			}

			@Override
			protected Boolean isNonBlank() {
				customer.setDesignation(textInput);
				return true;
			}
		};

		new DataInputter(phoneInput, routeCombo) {
			@Override
			protected Boolean isBlankNot() {
				customer.setPhone(0);
				setNext(getUneditedTable());
				return false;
			}

			@Override
			protected Boolean isPositive() {
				customer.setPhone(number.longValue());
				number = BigDecimal.ZERO;
				setNext(getUneditedTable());
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
		routeCombo = new TableCombo(tableItem, 1, Route.getList(), "").getCombo();
		routeCombo.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(routeCombo, tableItem);
		new ComboSelector(routeCombo, routeStartDateInput) {
			@Override
			protected void processSelection() {
				routeCombo.dispose();
				if (selection.isEmpty()) {
					tableItem.dispose();
					if (Login.group().equals("super_user")) {
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
		routeStartDateInput = new TableTextInput(tableItem, 2, DIS.TODAY).getText();
		routeStartDateInput.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(routeStartDateInput, tableItem);
		new DataInputter(routeStartDateInput, postButton) {
			private Date date;

			@Override
			protected Boolean isNonBlank() {
				date = DIS.parseDate(textInput);
				if (OrderControl.isOnFile(Type.ACCOUNT, date, customer.getId())) {
					isError = true;
					new ErrorDialog("Only one(1) route update\nper customer per day\nis allowed.");
					return false;
				}
				routeStartDateInput.dispose();
				customer.setRouteStartDate(date);
				tableItem.setText(2, textInput);
				tableItem.setText(3, customer.getInputter());
				if (User.isAdmin()) {
					setNext(creditLimitInput);
					setCreditLimitInput();
				}
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
		creditLimitInput = new TableTextInput(tableItem, 1, 0).getText();
		creditLimitInput.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(creditLimitInput, tableItem);
		new DataInputter(creditLimitInput, creditTermInput) {
			@Override
			protected Boolean isBlankNot() {
				creditLimitInput.dispose();
				tableItem.dispose();
				setNext(familyIdInput);
				setFamilyIdInput();
				return true;
			}

			@Override
			protected Boolean isPositive() {
				creditLimitInput.dispose();
				customer.setCreditLimit(number);
				tableItem.setText(1, DIS.INTEGER.format(number));
				setCreditTermInput();
				return true;
			}
		};
	}

	private void setCreditTermInput() {
		creditTermInput = new TableTextInput(tableItem, 2, 0).getText();
		creditTermInput.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(creditTermInput, tableItem);
		new DataInputter(creditTermInput, gracePeriodInput) {
			@Override
			protected Boolean isPositive() {
				int creditTerm = number.intValue();
				creditTermInput.dispose();
				customer.setCreditTerm(creditTerm);
				tableItem.setText(2, DIS.INTEGER.format(creditTerm));
				setGracePeriodInput();
				return true;
			}
		};
	}

	private void setGracePeriodInput() {
		gracePeriodInput = new TableTextInput(tableItem, 3, 0).getText();
		gracePeriodInput.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(gracePeriodInput, tableItem);
		new DataInputter(gracePeriodInput, creditStartDateInput) {
			@Override
			protected Boolean isBlankNot() {
				gracePeriodInput.dispose();
				setCreditStartDateInput();
				return false;
			}

			protected Boolean isPositive() {
				int gracePeriod = number.intValue();
				gracePeriodInput.dispose();
				customer.setGracePeriod(gracePeriod);
				tableItem.setText(3, DIS.INTEGER.format(gracePeriod));
				setCreditStartDateInput();
				return true;
			}
		};
	}

	private void setCreditStartDateInput() {
		creditStartDateInput = new TableTextInput(tableItem, 4, DIS.TODAY).getText();
		creditStartDateInput.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(creditStartDateInput, tableItem);
		new DataInputter(creditStartDateInput, familyIdInput) {
			private Date date;

			@Override
			protected Boolean isNonBlank() {
				date = DIS.parseDate(textInput);
				if (date.before(DIS.TODAY)) {
					isError = true;
					new ErrorDialog("Date cannot be\nearlier than today.");
					return false;
				}
				if (OrderControl.isOnFile(Type.CREDIT, date, customer.getId())) {
					isError = true;
					new ErrorDialog("Only one(1) credit update\nper customer per day\nis allowed.");
					return false;
				}
				creditStartDateInput.dispose();
				customer.setCreditStartDate(date);
				tableItem.setText(4, textInput);
				tableItem.setText(5, customer.getInputter());
				setFamilyIdInput();
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
		familyIdInput = new TableTextInput(tableItem, 1, 0).getText();
		familyIdInput.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(familyIdInput, tableItem);
		new DataInputter(familyIdInput, creditTermInput) {
			@Override
			protected Boolean isBlankNot() {
				familyIdInput.dispose();
				tableItem.dispose();
				setNext(postButton);
				return false;
			}

			@Override
			protected Boolean isPositive() {
				int familyId = number.intValue();
				String family = Item.getFamily(-familyId);
				if (family.isEmpty()) {
					isError = true;
					new ErrorDialog("Item Family #" + familyId + "\nis not on file.");
					return false;
				}
				familyIdInput.dispose();
				customer.setFamilyId(-familyId);
				tableItem.setText(1, String.valueOf(familyId));
				tableItem.setText(2, family);
				setFirstLevelDiscountInput();
				return true;
			}
		};
	}

	private void disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(final Control control,
	        final TableItem tableItem) {
		control.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!control.isDisposed() && !isError) {
					control.dispose();
					tableItem.dispose();
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				isError = false;
			}
		});
	}

	private void setFirstLevelDiscountInput() {
		discount1Input = new TableTextInput(tableItem, 3, 0).getText();
		discount1Input.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(discount1Input, tableItem);
		new DataInputter(discount1Input, discount2Input) {
			@Override
			protected Boolean isPositive() {
				discount1Input.dispose();
				customer.setDiscount1Percent(number);
				tableItem.setText(3, DIS.formatTo2Places(number));
				setSecondLevelDiscountInput();
				return true;
			}
		};
	}

	private void setSecondLevelDiscountInput() {
		discount2Input = new TableTextInput(tableItem, 4, 0).getText();
		discount2Input.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(discount2Input, tableItem);
		new DataInputter(discount2Input, discountStartDateInput) {
			@Override
			protected Boolean isBlankNot() {
				discount2Input.dispose();
				setNext(discountStartDateInput);
				setDiscountStartDateInput();
				return true;
			}

			@Override
			protected Boolean isPositive() {
				discount2Input.dispose();
				customer.setDiscount2Percent(number);
				tableItem.setText(4, DIS.formatTo2Places(number));
				setDiscountStartDateInput();
				return true;
			}
		};
	}

	private void setDiscountStartDateInput() {
		discountStartDateInput = new TableTextInput(tableItem, START_DATE_COLUMN, DIS.TODAY).getText();
		discountStartDateInput.setFocus();
		disposeControlWhenFocusWasLostButLineEntryIsStillIncomplete(discountStartDateInput, tableItem);
		new DataInputter(discountStartDateInput, postButton) {
			private Date date;

			@Override
			protected Boolean isNonBlank() {
				date = DIS.parseDate(textInput);
				if (date.before(DIS.TODAY)) {
					isError = true;
					new ErrorDialog("Date cannot be\nearlier than today.");
					return false;
				}
				if (OrderControl.isOnFile(Type.DISCOUNT, date, customer.getId())) {
					isError = true;
					new ErrorDialog("Only one(1) discount update\nper customer per day\nis allowed.");
					return false;
				}
				discountStartDateInput.dispose();
				customer.setDiscountStartDate(date);
				tableItem.setText(5, textInput);
				tableItem.setText(6, customer.getInputter());
				return true;
			}
		};
	}

	@Override
	protected void setFocus() {
		smsInput.setTouchEnabled(true);
		smsInput.setFocus();
	}

	public void edit() {
		editButton.setEnabled(false);

		String channel = channelCombo.getText();
		String[] channels = new String[] { "AMBULANT", channel };
		provinceCombo.setItems(channels);
		provinceCombo.select(0);

		String province = provinceCombo.getText();
		String[] provinces = new Area(0).getAreas();
		int provinceIdx = Arrays.binarySearch(provinces, province);
		provinceCombo.setItems(provinces);
		provinceCombo.select(provinceIdx);

		String city = cityCombo.getText();
		String[] cities = new Area(province).getAreas();
		int cityIdx = Arrays.binarySearch(cities, city);
		cityCombo.setItems(cities);
		cityCombo.select(cityIdx);

		String district = cityCombo.getText();
		String[] districts = new Area(city).getAreas();
		int districtIdx = Arrays.binarySearch(districts, district);
		districtCombo.setItems(districts);
		districtCombo.select(districtIdx);

		addListener();
		phoneInput.setFocus();
	}

	@Override
	public Posting getPosting() {
		return new CustomerPosting(customer);
	}

	/*
	 * @Override public void goPrevious() { shell.close(); new CustomerView(id -
	 * 1); }
	 * 
	 * @Override public void goNext() { shell.close(); int next = id + 1; int
	 * max = Order.getMaxId(type); new CustomerView(id + 1); }
	 */
}

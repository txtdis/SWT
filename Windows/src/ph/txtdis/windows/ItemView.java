package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ItemView extends InputView {
	private int columnIdx, childId, itemTypeId, perQty, channelId;
	private boolean isPurchased, isMonetary, isFree, isReturnable, isBoughtUomEntered, isSoldUomEntered,
	        isReportedUomEntered, isNoUomChecked, isRefMeat;
	private ArrayList<BOM> bomList;
	private ArrayList<Integer> childIdList;
	private ArrayList<QtyPerUOM> qtyPerUomList;
	private ArrayList<String> usedUoms;
	private BigDecimal less;
	private Button editButton, itemIdButton, notDiscountedCheckBox, uomCheckBox;
	private Combo classCombo, typeCombo, uomCombo, childUomCombo, discountUomCombo, channelCombo;
	private ComboBox classComboBox, typeComboBox;
	private ItemData data;
	private String itemClass, discountUom;
	private Text nameInput, descriptionInput, unspscInput, childIdInput, childQtyInput, qtyPerUomInput, txtPrice,
	        priceStartDateInput, txtDiscount, txtVolume, discountStartDateInput;
	private TextInputBox unspscInputBox;
	private Type uom;
	private Table bomTable, uomTable, priceTable, discountTable;
	private TableItem bomTableItem, uomTableItem, priceTableItem, discountTableItem;

	final private static int PURCHASE_COLUMN = 1;
	final private static int DEALER_COLUMN = 2;
	final private static int RETAIL_COLUMN = 3;
	final private static int SUPERMKT_COLUMN = 4;
	final private static int SUPERSRP_COLUMN = 5;
	final private static int START_DATE_COLUMN = 6;

	final private static int BOUGHT_UOM_COLUMN = 3;
	final private static int SOLD_UOM_COLUMN = 4;
	final private static int REPORTED_UOM_COLUMN = 5;

	public ItemView() {
		this(0);
	}

	public ItemView(int id) {
		this(new ItemData(id));
	}

	public ItemView(ItemData data) {
		this.data = data;
		type = Type.ITEM;
		isNoUomChecked = true;
		usedUoms = new ArrayList<>();
		display();
	}

	@Override
	protected void addHeader() {
		new Header(this, data) {
			@Override
			protected void layButtons() {
				new ImgButton(buttons, Type.NEW, type);
				new BackwardButton(buttons, data);
				new ImgButton(buttons, Type.OPEN, view);
				new ForwardButton(buttons, data);
				if (id != 0 && User.isSupply()) {
					editButton = new ImageButton(buttons, module, "Tag", "Update prices") {
						@Override
						protected void proceed() {
							columnIdx = 0;
							rowIdx = ((ItemData) data).getPriceData().length;
							isSoldUomEntered = Item.isSold(id);
							isRefMeat = Item.isRefMeat(typeCombo.getText());
							priceTableItem = new TableItem(priceTable, SWT.NONE, rowIdx);
							priceTable.setTopIndex(priceTable.getItemCount() - 1);
							priceTableItem.setText(columnIdx++, String.valueOf(rowIdx + 1));
							getButton().setEnabled(false);
							setPriceInput();
						}
					}.getButton();
				}
				if (id == 0 || editButton != null)
					postButton = new ImgButton(buttons, Type.SAVE, view).getButton();
			}
		};
	}

	@Override
	protected void addSubheader() {
		Group header = new Grp(shell, 8, "DETAILS", SWT.CENTER, SWT.BEGINNING, true, false, 2, 1).getGroup();

		new TextDisplayBox(header, "ITEM ID", data.getId()).getText();
		nameInput = new TextInputBox(header, "NAME", data.getItemName(), 1, 16).getText();
		descriptionInput = new TextInputBox(header, "DESCRIPTION", data.getName(), 3, 52).getText();

		classComboBox = new ComboBox(header, data.getItemClasses(), "CLASS");
		classCombo = classComboBox.getCombo();
		itemClass = data.getItemClass();

		typeComboBox = new ComboBox(header, data.getItemTypes(), "TYPE");
		typeCombo = typeComboBox.getCombo();

		unspscInputBox = new TextInputBox(header, "VENDOR ID #", data.getUnspscId());
		unspscInput = unspscInputBox.getText();

		notDiscountedCheckBox = new CheckButton(header, "NOT DISCOUNTED", data.isNotDiscounted()).getButton();
		notDiscountedCheckBox.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
	}

	@Override
	public void addTable() {
		Composite table = new Compo(shell, 2).getComposite();

		Group bom = new Grp(table, 1, "BILL OF MATERIALS", SWT.CENTER, SWT.BEGINNING, true, false, 1, 1).getGroup();
		bomTable = new ReportTable(bom, data.getBomData(), data.getBomHeaders(), 50).getTable();
		bomTable.setTopIndex(bomTable.getItemCount());

		Group discount = new Grp(table, 1, "VOLUME DISCOUNT", SWT.CENTER, SWT.BEGINNING, true, false, 1, 1).getGroup();
		discountTable = new ReportTable(discount, data.getDiscountData(), data.getDiscountHeaders(), 70).getTable();
		discountTable.setTopIndex(discountTable.getItemCount());

		Group uom = new Grp(table, 1, "QUANTITY PER UOM RELATIVE TO \"PK\"", SWT.CENTER, SWT.BEGINNING, true, false, 1,
		        1).getGroup();
		uomTable = new ReportTable(uom, data.getUomData(), data.getUomHeaders(), 90).getTable();
		uomTable.setTopIndex(uomTable.getItemCount());

		Group price = new Grp(table, 1, "PRICE PER PK", SWT.CENTER, SWT.BEGINNING, true, false, 1, 1).getGroup();
		priceTable = new ReportTable(price, data.getPriceData(), data.getPriceHeaders(), 90).getTable();
		priceTable.setTopIndex(priceTable.getItemCount());
	}

	@Override
	protected void addListener() {
		new DataInputter(nameInput, descriptionInput) {
			@Override
			protected Boolean isNonBlank() {
				if (Item.getId(textInput) != 0) {
					new ErrorDialog(textInput + " has been used;\ntry another.");
					return false;
				}
				data.setItemName(textInput);
				return true;
			}
		};

		new DataInputter(descriptionInput, classCombo) {
			@Override
			protected Boolean isNonBlank() {
				data.setName(textInput);
				return true;
			}
		};

		new ComboSelector(classComboBox, typeCombo) {
			@Override
			protected void processSelection() {
				itemClass = selection;
				isMonetary = itemClass.equals("MONETARY");
				isPurchased = itemClass.equals("PURCHASED");
				isReturnable = itemClass.equals("RETURNABLE");
				isFree = itemClass.equals("FREEBIE");
				data.setItemClass(itemClass);
				data.setNotDiscounted(isFree || isMonetary);
				if (isMonetary) {
					typeCombo.setItems(Item.getMonetaryTypes());
					typeCombo.pack();
					unspscInput.setVisible(false);
					unspscInputBox.getLabel().setVisible(false);
				}
			}
		};

		new ComboSelector(typeComboBox, unspscInput) {
			@Override
			protected void processSelection() {
				if (isMonetary) {
					itemTypeId = Item.getMonetaryId(selection);
					data.setItemTypeId(itemTypeId);
					setNext(postButton);
				} else {
					isRefMeat = Item.isRefMeat(selection);
					itemTypeId = Item.getFamilyId(selection);
					data.setItemTypeId(itemTypeId);
					if (isFree) {
						notDiscountedCheckBox.setSelection(true);
						setChildIdInput();
						setNext(childIdInput);
					} else if (!isPurchased || DIS.VENDOR_ITEM_ID_MINIMUM_LENGTH == null)
						setNext(notDiscountedCheckBox);
				}
			}
		};

		new DataInputter(unspscInput, notDiscountedCheckBox) {

			@Override
			protected Boolean isPositive() {
				long unspscId = number.longValue();
				if (textInput.length() != DIS.VENDOR_ITEM_ID_MINIMUM_LENGTH) {
					new ErrorDialog("UNSPSC # must be " + DIS.VENDOR_ITEM_ID_MINIMUM_LENGTH
					        + " digits long;\ntry again.");
					return false;
				} else if (Item.getId(unspscId) != 0) {
					new ErrorDialog(textInput + " has been used;\ntry another.");
					return false;
				} else {
					data.setUnspscId(unspscId);
					return true;
				}
			}
		};

		new CheckBoxSelector(notDiscountedCheckBox, uomCheckBox) {
			@Override
			protected void processSelection() {
				data.setNotDiscounted(checkBox.getSelection());
				if (Item.isWithBOM(itemClass)) {
					setChildIdInput();
				} else
					setUomTableData();
			}
		};
	}

	private void setChildIdInput() {
		bomList = data.getBomList();
		childIdList = data.getChildIdList();
		bomTableItem = new TableItem(bomTable, SWT.NONE, rowIdx);
		bomTable.setTopIndex(bomTable.getItemCount() - 1);
		bomTableItem.setText(0, String.valueOf(rowIdx + 1));
		itemIdButton = new TableButton(bomTableItem, rowIdx, 0, "Item List").getButton();
		childIdInput = new TableTextInput(bomTableItem, 1, 0).getText();
		new DataInputter(childIdInput, uomCombo) {

			@Override
			protected Boolean isBlankNot() {
				if (rowIdx == 0)
					return false;
				childIdInput.dispose();
				itemIdButton.dispose();
				bomTableItem.dispose();
				rowIdx = 0;
				setUomTableData();
				return true;
			}

			@Override
			protected Boolean isPositive() {
				childId = number.intValue();
				if (childIdList.contains(childId)) {
					new ErrorDialog("Item ID " + childId + "\nis already on the list");
					return false;
				}
				String name = Item.getShortId(childId);
				if (name.isEmpty()) {
					new ErrorDialog("Item ID " + childId + "\nis not in our system");
					return false;
				}
				bomTableItem.setText(1, textInput);
				bomTableItem.setText(2, name);
				itemIdButton.dispose();
				childIdInput.dispose();
				setChildUomCombo();
				return true;
			}
		};
		childIdInput.setFocus();
	}

	private void setChildUomCombo() {
		childUomCombo = new TableCombo(bomTableItem, 3, UOM.getUoms(childId)).getCombo();
		new ComboSelector(childUomCombo, qtyPerUomInput) {
			@Override
			protected void processSelection() {
				uom = Type.valueOf(selection);
				bomTableItem.setText(3, selection);
				childUomCombo.dispose();
				setChildQtyInput();
			}
		};
		childUomCombo.setFocus();
	}

	private void setChildQtyInput() {
		childQtyInput = new TableTextInput(bomTableItem, 4, BigDecimal.ZERO).getText();
		new DataInputter(childQtyInput, childIdInput) {
			@Override
			protected Boolean isPositive() {
				bomTableItem.setText(4, textInput);
				childQtyInput.dispose();
				childIdList.add(childId);
				bomList.add(new BOM(childId, uom, number));
				data.setBomList(bomList);
				if (isFree) {
					rowIdx = 0;
					setUomTableData();
				} else {
					rowIdx++;
					setChildIdInput();
				}
				return true;
			}
		};
		childQtyInput.setFocus();
	}

	private void setUomTableData() {
		uomTableItem = new TableItem(uomTable, SWT.NONE, rowIdx);
		uomTableItem.setText(0, "1");
		uomTableItem.setText(1, "1.0000");
		uomTableItem.setText(2, "PK");
		usedUoms.add("PK");
		data.setQty(BigDecimal.ONE);
		data.setUom(Type.PK);
		qtyPerUomList = data.getQtyPerUOMList();
		columnIdx = isPurchased ? BOUGHT_UOM_COLUMN : SOLD_UOM_COLUMN;

		if (isFree) {
			uomTableItem.setText(columnIdx, "OK");
			isSoldUomEntered = true;
			saveUomTableData();
		} else {
			setBuyOrSellOrReportCheckBoxes();
		}
	}

	private void saveUomTableData() {
		columnIdx = 0;
		uomTableItem = new TableItem(uomTable, SWT.NONE, ++rowIdx);
		UI.setTableItemText(uomTableItem, columnIdx, rowIdx + 1);
		qtyPerUomList.add(new QtyPerUOM(data.getQty(), data.getUom(), isBoughtUomEntered, isSoldUomEntered,
		        isReportedUomEntered));
		data.setQtyPerUomList(qtyPerUomList);
		setQtyPerUomInput();
	}

	private void setBuyOrSellOrReportCheckBoxes() {
		uomCheckBox = new TableCheckButton(uomTableItem, rowIdx, columnIdx).getButton();
		uomCheckBox.setBackground(UI.YELLOW);

		new CheckBoxSelector(uomCheckBox, qtyPerUomInput) {
			@Override
			protected void processSelection() {
				String ok = "";
				if (checkBox.getSelection()) {
					if (isFree) {
						uomTableItem.setText(columnIdx, "OK");
						uomCheckBox.dispose();
						saveUomTableData();
						setNext(postButton);
						return;
					} else {
						if (!isReportedUomCheckedAndAtItsColumn() && !isBoughtUomCheckedAndAtItsColumn())
							ok = "OK";
						switch (columnIdx) {
						case BOUGHT_UOM_COLUMN:
							isBoughtUomEntered = true;
							break;
						case SOLD_UOM_COLUMN:
							isSoldUomEntered = true;
							break;
						case REPORTED_UOM_COLUMN:
							isReportedUomEntered = true;
							break;
						}
						isNoUomChecked = false;
						uomTableItem.setText(columnIdx, ok);
					}
				}
				uomCheckBox.dispose();
				if (isReportUomCheckedAndAtSoldColumn() || isAtEndOfLine() || isFree) {
					if ((rowIdx != 0 && isNoUomChecked)) {
						usedUoms.remove(uomTableItem.getText(2));
						uomTableItem.dispose();
						rowIdx--;
					}
					isNoUomChecked = true;
					uomTable.setTopIndex(uomTable.getItemCount() - 1);
					saveUomTableData();
					return;
				}
				++columnIdx;
				setBuyOrSellOrReportCheckBoxes();
			}

			private boolean isAtEndOfLine() {
				return columnIdx == REPORTED_UOM_COLUMN;
			}

			private boolean isReportUomCheckedAndAtSoldColumn() {
				return columnIdx == SOLD_UOM_COLUMN && isReportedUomEntered;
			}

			private boolean isBoughtUomCheckedAndAtItsColumn() {
				return columnIdx == BOUGHT_UOM_COLUMN && isBoughtUomEntered;
			}

			private boolean isReportedUomCheckedAndAtItsColumn() {
				return isAtEndOfLine() && isReportedUomEntered;
			}
		};
		uomCheckBox.setFocus();
	}

	private void setQtyPerUomInput() {
		qtyPerUomInput = new TableTextInput(uomTableItem, ++columnIdx, BigDecimal.ZERO).getText();
		new DataInputter(qtyPerUomInput, uomCombo) {

			@Override
			protected Boolean isBlankNot() {
				if (isFree) {
					setNext(postButton);
					return true;
				}
				if (rowIdx > 0 && ((!isPurchased || isBoughtUomEntered) && isReportedUomEntered && isSoldUomEntered)
				        || isReturnable) {
					qtyPerUomInput.dispose();
					uomTableItem.dispose();
					columnIdx = 0;
					rowIdx = 0;
					if (isSoldUomEntered) {
						discountTableItem = new TableItem(discountTable, SWT.NONE, rowIdx);
						discountTableItem.setText(columnIdx++, String.valueOf(rowIdx + 1));
						setDiscountInput();
						setNext(txtDiscount);
					} else if (isBoughtUomEntered) {
						priceTableItem = new TableItem(priceTable, SWT.NONE, rowIdx);
						priceTableItem.setText(columnIdx++, String.valueOf(rowIdx + 1));
						setPriceInput();
						setNext(txtPrice);
					} else {
						setNext(postButton);
					}
					return true;
				}
				return false;
			}

			@Override
			protected Boolean isPositive() {
				uomTableItem.setText(columnIdx++, DIS.FOUR_PLACE_DECIMAL.format(number));
				data.setQty(number);
				qtyPerUomInput.dispose();
				setUomCombo();
				return true;
			}
		};
		qtyPerUomInput.setTouchEnabled(true);
		qtyPerUomInput.setFocus();
	}

	private void setUomCombo() {
		String[] uoms = UOM.getUoms(usedUoms);
		if (uoms.length == 0) {
			if (isPurchased && !isBoughtUomEntered) {
				new ErrorDialog("Purchased item must have a buying UOM;\ntry again from the top.");
				shell.close();
				new ItemView();
				return;
			}
			uomTableItem.dispose();
			rowIdx = 0;
			columnIdx = 0;
			priceTableItem = new TableItem(priceTable, SWT.NONE, rowIdx);
			priceTableItem.setText(columnIdx++, String.valueOf(rowIdx + 1));
			setPriceInput();
			txtPrice.setTouchEnabled(true);
			txtPrice.setFocus();
			return;
		}
		uomCombo = new TableCombo(uomTableItem, columnIdx, uoms).getCombo();
		uomCombo.setEnabled(true);
		uomCombo.setFocus();
		new ComboSelector(uomCombo, uomCheckBox) {
			@Override
			protected void processSelection() {
				uomTableItem.setText(columnIdx, selection);
				usedUoms.add(selection);
				uomCombo.dispose();
				data.setUom(Type.valueOf(selection));
				columnIdx = isPurchased ? BOUGHT_UOM_COLUMN : SOLD_UOM_COLUMN;
				setBuyOrSellOrReportCheckBoxes();
			}
		};
	}

	// Item volume discount amount input
	private void setDiscountInput() {
		txtDiscount = new TableTextInput(discountTableItem, columnIdx, BigDecimal.ZERO).getText();
		txtDiscount.setTouchEnabled(true);
		txtDiscount.setFocus();
		new DataInputter(txtDiscount, txtVolume) {
			@Override
			protected Boolean isBlankNot() {
				txtDiscount.dispose();
				discountTableItem.dispose();
				rowIdx = 0;
				columnIdx = 0;
				priceTableItem = new TableItem(priceTable, SWT.NONE, rowIdx);
				priceTableItem.setText(columnIdx++, String.valueOf(rowIdx + 1));
				setPriceInput();
				setNext(txtPrice);
				return true;
			}

			@Override
			protected Boolean isPositive() {
				less = number;
				textInput = DIS.NO_COMMA_DECIMAL.format(less);
				discountTableItem.setText(columnIdx++, textInput);
				txtDiscount.dispose();
				setVolumeInput();
				return true;
			}
		};
	}

	// Item volume discount quantity cut-off
	private void setVolumeInput() {
		txtVolume = new TableTextInput(discountTableItem, columnIdx, 0).getText();
		txtVolume.setTouchEnabled(true);
		txtVolume.setFocus();
		new DataInputter(txtVolume, discountUomCombo) {
			@Override
			protected Boolean isPositive() {
				perQty = number.intValue();
				discountTableItem.setText(columnIdx++, textInput);
				txtVolume.dispose();
				if (discountUom == null) {
					setDiscountUomCombo();
				} else {
					discountTableItem.setText(columnIdx++, discountUom);
					setChannelCombo();
					setNext(channelCombo);
				}
				return true;
			}
		};
	}

	// Item volume discount UOM selector
	private void setDiscountUomCombo() {
		String[] uoms = UOM.getUoms();
		discountUomCombo = new TableCombo(discountTableItem, columnIdx, uoms).getCombo();
		discountUomCombo.setEnabled(true);
		discountUomCombo.setFocus();
		new ComboSelector(discountUomCombo, channelCombo) {
			@Override
			protected void processSelection() {
				discountTableItem.setText(columnIdx++, selection);
				uom = Type.valueOf(selection);
				discountUomCombo.dispose();
				setChannelCombo();
			}
		};
	}

	// Item volume discount applicable channel
	private void setChannelCombo() {
		channelCombo = new TableCombo(discountTableItem, columnIdx, Channel.getList()).getCombo();
		channelCombo.setEnabled(true);
		channelCombo.setFocus();
		new ComboSelector(channelCombo, discountStartDateInput) {
			@Override
			protected void processSelection() {
				discountTableItem.setText(columnIdx++, selection);
				channelId = Channel.getId(selection);
				channelCombo.dispose();
				setDiscountStartDateInput();
			}
		};
	}

	private void setDiscountStartDateInput() {
		discountStartDateInput = new TableTextInput(discountTableItem, columnIdx, DIS.TOMORROW).getText();
		discountStartDateInput.setTouchEnabled(true);
		discountStartDateInput.setFocus();
		new DataInputter(discountStartDateInput, txtDiscount) {

			@Override
			protected Boolean isNonBlank() {
				date = DIS.parseDate(textInput);
				discountTableItem.setText(columnIdx++, textInput);
				discountStartDateInput.dispose();
				columnIdx = 0;
				data.getVolumeDiscountList().add(new VolumeDiscount(less, perQty, uom, channelId, date));
				discountTable.setTopIndex(discountTable.getItemCount());
				discountTableItem = new TableItem(discountTable, SWT.NONE, ++rowIdx);
				discountTableItem.setText(columnIdx++, String.valueOf(rowIdx + 1));
				setDiscountInput();
				return true;
			}
		};
	}

	// Item price input
	private void setPriceInput() {
		txtPrice = new TableTextInput(priceTableItem, columnIdx, BigDecimal.ZERO).getText();
		txtPrice.setTouchEnabled(true);
		txtPrice.setFocus();
		new DataInputter(txtPrice, txtPrice) {

			@Override
			protected Boolean isBlankNot() {
				boolean isRefMeatAndAtSoldToSupermarketColumn = isRefMeat
				        && (columnIdx == SUPERMKT_COLUMN || columnIdx == SUPERSRP_COLUMN);
				if (isBoughtAndAtPurchaseColumn()
				        || (isSoldUomEntered && (isAtDealerPriceColumn() || isRefMeatAndAtSoldToSupermarketColumn))) {
					return false;
				}
				if (columnIdx == PURCHASE_COLUMN && rowIdx > 0 && isBoughtUomEntered && isReportedUomEntered) {
					txtPrice.dispose();
					setNext(postButton);
				}
				return null;
			}

			@Override
			protected Boolean isPositive() {
				BigDecimal price = BigDecimal.ZERO;
				priceTableItem.setText(columnIdx, textInput);
				txtPrice.dispose();
				if (!price.equals(BigDecimal.ZERO))
					switch (columnIdx) {
					case PURCHASE_COLUMN:
						data.setPurchasePrice(price);
						break;
					case DEALER_COLUMN:
						data.setDealerPrice(price);
						break;
					case RETAIL_COLUMN:
						data.setRetailPrice(price);
						break;
					case SUPERMKT_COLUMN:
						data.setSupermarketPrice(price);
						break;
					case SUPERSRP_COLUMN:
						data.setSupermarketSRPrice(price);
						break;
					}
				if (columnIdx == SUPERSRP_COLUMN || !isSoldUomEntered || (columnIdx == RETAIL_COLUMN && !isRefMeat)) {
					columnIdx = START_DATE_COLUMN;
					setPriceDateInput();
					setNext(priceStartDateInput);
				} else {
					columnIdx++;
					setPriceInput();
				}
				return true;
			}

			private boolean isAtDealerPriceColumn() {
				return columnIdx == DEALER_COLUMN || columnIdx == RETAIL_COLUMN;
			}

			private boolean isBoughtAndAtPurchaseColumn() {
				return columnIdx == PURCHASE_COLUMN && isPurchased;
			}
		};
	}

	private void setPriceDateInput() {
		priceStartDateInput = new TableTextInput(priceTableItem, columnIdx, DIS.TOMORROW).getText();
		priceStartDateInput.setTouchEnabled(true);
		priceStartDateInput.setFocus();
		new DataInputter(priceStartDateInput, postButton) {
			@Override
			protected Boolean isNonBlank() {
				date = DIS.parseDate(textInput);
				priceTableItem.setText(columnIdx++, textInput);
				priceStartDateInput.dispose();
				data.setPriceStartDate(date);
				return true;
			}
		};
	}

	@Override
	protected void setFocus() {
		if (id == 0)
			nameInput.setFocus();
	}

	public Text getTxtShortId() {
		return nameInput;
	}

	public Combo getCmbType() {
		return classCombo;
	}

	public void setCmbType(Combo cmbType) {
		this.classCombo = cmbType;
	}

	public Text getTxtName() {
		return descriptionInput;
	}

	public Text getTxtUnspscId() {
		return unspscInput;
	}

	public Button getNotDiscountedOrIsCheckBox() {
		return notDiscountedCheckBox;
	}

	public Combo getCmbProductLine() {
		return typeCombo;
	}

	public Table getTblUom() {
		return uomTable;
	}

	public Table getTblPrice() {
		return priceTable;
	}

	public Table getTblDiscount() {
		return discountTable;
	}

	@Override
	public Posting getPosting() {
		return new ItemPosting(data);
	}
}

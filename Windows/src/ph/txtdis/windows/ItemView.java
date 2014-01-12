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

public class ItemView extends OrderView {
	private int rowIdx, columnIdx, childId;
	private boolean isPurchased, isReturnable, isBoughtUomEntered, isSoldUomEntered, isReportedUomEntered,
	        isNoUomChecked, isRefMeat;
	private ArrayList<BOM> bomList;
	private ArrayList<Integer> childIdList;
	private ArrayList<QtyPerUOM> qtyPerUomList;
	private ItemHelper helper;
	private ItemMaster item;
	private Button saveButton, itemIdButton, notDiscountedOrIsCheckBox, uomCheckBox;
	private Combo typeCombo, productLineCombo, uomCombo, childUomCombo, discountUomCombo, channelCombo;
	private ComboBox typeComboBox, productLineComboBox;
	private Text shortIdInput, nameInput, unspscInput, childIdInput, childQtyInput, qtyPerUomInput, txtPrice,
	        priceStartDateInput, txtDiscount, txtVolume, discountStartDateInput;
	private Table bomTable, uomTable, priceTable, discountTable;
	private TableItem bomTableItem, uomTableItem, priceTableItem, discountTableItem;
	private String type, discountUom;
	private ArrayList<String> usedUoms;

	private BigDecimal less;
	private int perQty, uomId, channelId;

	final private static int PURCHASE_COLUMN = 1;
	final private static int DEALER_COLUMN = 2;
	final private static int RETAIL_COLUMN = 3;
	final private static int SUPERMKT_COLUMN = 4;
	final private static int SUPERSRP_COLUMN = 5;
	final private static int START_DATE_COLUMN = 6;

	final private static int BOUGHT_UOM_COLUMN = 3;
	final private static int SOLD_UOM_COLUMN = 4;
	final private static int REPORTED_UOM_COLUMN = 5;

	public ItemView(int id) {
		super();
		this.id = id;
		isNoUomChecked = true;
		helper = new ItemHelper();
		usedUoms = new ArrayList<>();
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
		order = item = new ItemMaster(id);
	}

	@Override
	protected void setTitleBar() {
		saveButton = new MasterTitleBar(this, order) {
			@Override
			protected void insertButtons() {
				String group = Login.getGroup();
				if (id != 0 && ((group.contains("super") || group.equals("sys_admin")))) {
					new ImageButton(buttons, module, "Tag", "Update prices") {
						@Override
						protected void doWhenSelected() {
							columnIdx = 0;
							rowIdx = item.getPriceData().length;
							isSoldUomEntered = helper.isSold(id);
							isRefMeat = helper.isRefMeat(productLineCombo.getText());
							priceTableItem = new TableItem(priceTable, SWT.NONE, rowIdx);
							priceTable.setTopIndex(priceTable.getItemCount() - 1);
							priceTableItem.setText(columnIdx++, String.valueOf(rowIdx + 1));
							getButton().setEnabled(false);
							setPriceInput();
						}
					};
					setBtnPost(new PostButton(buttons, order).getButton());
				}
			}
		}.getSaveButton();
	}

	@Override
	protected void setHeader() {
		Group header = new Grp(shell, 8, "DETAILS", SWT.CENTER, SWT.BEGINNING, true, false, 2, 1).getGroup();

		new TextDisplayBox(header, "ITEM ID", item.getId()).getText();
		shortIdInput = new TextInputBox(header, "NAME", item.getItemName(), 1, 16).getText();
		nameInput = new TextInputBox(header, "DESCRIPTION", item.getName(), 3, 52).getText();

		productLineComboBox = new ComboBox(header, item.getProductLines(), "LINE");
		productLineCombo = productLineComboBox.getCombo();

		typeComboBox = new ComboBox(header, item.getTypes(), "TYPE");
		typeCombo = typeComboBox.getCombo();
		type = item.getItemType();

		unspscInput = new TextInputBox(header, "VENDOR ID #", item.getUnspscId()).getText();

		notDiscountedOrIsCheckBox = new CheckButton(header, "NOT DISCOUNTED", item.isNotDiscounted()).getButton();
		notDiscountedOrIsCheckBox.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
	}

	@Override
	public Table getTable() {
		Composite table = new Compo(shell, 2).getComposite();

		Group bom = new Grp(table, 1, "BILL OF MATERIALS", SWT.CENTER, SWT.BEGINNING, true, false, 1, 1).getGroup();
		bomTable = new ReportTable(bom, item.getBomData(), item.getBomHeaders(), "", 50, true).getTable();
		bomTable.setTopIndex(bomTable.getItemCount());

		Group discount = new Grp(table, 1, "VOLUME DISCOUNT", SWT.CENTER, SWT.BEGINNING, true, false, 1, 1).getGroup();
		discountTable = new ReportTable(discount, item.getDiscountData(), item.getDiscountHeaders(), "", 70, true)
		        .getTable();
		discountTable.setTopIndex(discountTable.getItemCount());

		Group uom = new Grp(table, 1, "QUANTITY PER UOM RELATIVE TO \"PK\"", SWT.CENTER, SWT.BEGINNING, true, false, 1,
		        1).getGroup();
		uomTable = new ReportTable(uom, item.getUomData(), item.getUomHeaders(), "", 90, true).getTable();
		uomTable.setTopIndex(uomTable.getItemCount());

		Group price = new Grp(table, 1, "PRICE PER PK", SWT.CENTER, SWT.BEGINNING, true, false, 1, 1).getGroup();
		priceTable = new ReportTable(price, item.getPriceData(), item.getPriceHeaders(), "", 90, true).getTable();
		priceTable.setTopIndex(priceTable.getItemCount());
		return null;
	}

	@Override
	protected void setListener() {
		new TextInputter(shortIdInput, nameInput) {
			@Override
			protected boolean isTheDataInputValid() {
				if (helper.getId(textInput) != 0) {
					new ErrorDialog(textInput + " has been used;\ntry another.");
					return false;
				}
				item.setItemName(textInput);
				return true;
			}
		};

		new TextInputter(nameInput, productLineCombo) {
			@Override
			protected boolean isTheDataInputValid() {
				item.setName(textInput);
				return true;
			}
		};

		new ComboSelector(productLineComboBox, typeCombo) {
			@Override
			protected void doAfterSelection() {
				item.setProductLine(selection);
				isRefMeat = helper.isRefMeat(selection);
				productLineCombo.setEnabled(false);
			}
		};

		new ComboSelector(typeComboBox, unspscInput) {
			@Override
			protected void doAfterSelection() {
				type = selection;
				isPurchased = helper.isPurchased(type);
				isReturnable = type.equals("RETURNABLE");
				item.setBundled(type.equals("BUNDLED"));
				item.setPromo(type.equals("PROMO"));
				item.setFreebie(type.equals("FREEBIE"));
				item.setItemType(type);
				if (helper.isWithBOM(type))
					setNext(notDiscountedOrIsCheckBox);
			}
		};

		new TextInputter(unspscInput, notDiscountedOrIsCheckBox) {
			@Override
			protected boolean isABlankInputNotValid() {
				if (isPurchased && DIS.VENDOR_ITEM_ID_MINIMUM_LENGTH != null) {
					new ErrorDialog("Purchased items must have\ncorresponding UNSPSC numbers");
					return true;
				}
				shouldReturn = true;
				return false;
			}

			@Override
			protected boolean isThePositiveNumberValid() {
				long unspscId = numericInput.longValue();
				int unspscLength = DIS.VENDOR_ITEM_ID_MINIMUM_LENGTH == null ? 13 : DIS.VENDOR_ITEM_ID_MINIMUM_LENGTH;
				if (textInput.length() != unspscLength) {
					new ErrorDialog("UNSPSC # must be 13 digits long;\ntry again.");
					return false;
				} else if (helper.getId(unspscId) != 0) {
					new ErrorDialog(textInput + " has been used;\ntry another.");
					return false;
				}
				item.setUnspscId(unspscId);
				return true;
			}
		};

		new CheckBoxSelector(notDiscountedOrIsCheckBox, uomCheckBox) {
			@Override
			protected void doAfterSelection() {
				item.setNotDiscounted(checkBox.getSelection());
				if (helper.isWithBOM(type)) {
					bomList = item.getBomList();
					childIdList = item.getChildIdList();
					setChildIdInput();
				} else
					setUomTableData();
			}
		};
	}

	private void setUomTableData() {
		uomTableItem = new TableItem(uomTable, SWT.NONE, rowIdx);
		uomTableItem.setText(0, "1");
		uomTableItem.setText(1, "1.0000");
		uomTableItem.setText(2, "PK");
		usedUoms.add("PK");

		item.setQty(BigDecimal.ONE);
		item.setUomId(0);

		columnIdx = isPurchased ? BOUGHT_UOM_COLUMN : SOLD_UOM_COLUMN;
		qtyPerUomList = item.getQtyPerUOMList();
		setBuyOrSellOrReportCheckBoxes();
	}

	private void setChildIdInput() {
		bomTableItem = new TableItem(bomTable, SWT.NONE, rowIdx);
		bomTable.setTopIndex(bomTable.getItemCount() - 1);
		bomTableItem.setText(0, String.valueOf(rowIdx + 1));
		itemIdButton = new TableButton(bomTableItem, rowIdx, 0, "Item List").getButton();
		childIdInput = new TableTextInput(bomTableItem, rowIdx, 1, 0).getText();
		new TextInputter(childIdInput, uomCombo) {

			@Override
			protected boolean isABlankInputNotValid() {
				if (rowIdx == 0)
					return true;
				childIdInput.dispose();
				itemIdButton.dispose();
				bomTableItem.dispose();
				rowIdx = 0;
				setUomTableData();
				shouldReturn = true;
				return false;
			}

			@Override
			protected boolean isThePositiveNumberValid() {
				childId = numericInput.intValue();
				if (childIdList.contains(childId)) {
					new ErrorDialog("Item ID " + childId + "\nis already on the list");
					return false;
				}
				String name = helper.getShortId(childId);
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
		childUomCombo = new TableCombo(bomTableItem, 3, new UOM().getUoms(childId)).getCombo();
		new ComboSelector(childUomCombo, qtyPerUomInput) {
			@Override
			protected void doAfterSelection() {
				uomId = new UOM(selection).getId();
				bomTableItem.setText(3, selection);
				childUomCombo.dispose();
				setChildQtyInput();
			}
		};
		childUomCombo.setFocus();
	}

	private void setChildQtyInput() {
		childQtyInput = new TableTextInput(bomTableItem, rowIdx, 4, BigDecimal.ZERO).getText();
		new TextInputter(childQtyInput, childIdInput) {
			@Override
			protected boolean isThePositiveNumberValid() {
				bomTableItem.setText(4, textInput);
				childQtyInput.dispose();
				childIdList.add(childId);
				bomList.add(new BOM(childId, uomId, numericInput));
				item.setBomList(bomList);
				rowIdx++;
				setChildIdInput();
				return true;
			}
		};
		childQtyInput.setFocus();
	}

	private void setBuyOrSellOrReportCheckBoxes() {
		uomCheckBox = new TableCheckButton(uomTableItem, rowIdx, columnIdx).getButton();
		uomCheckBox.setBackground(UI.YELLOW);
		uomCheckBox.setFocus();
		new CheckBoxSelector(uomCheckBox, qtyPerUomInput) {
			@Override
			protected void doAfterSelection() {
				String ok = "";
				if (checkBox.getSelection()) {
					if (!((columnIdx == REPORTED_UOM_COLUMN && isReportedUomEntered) 
							|| (columnIdx == BOUGHT_UOM_COLUMN && isBoughtUomEntered)))
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
				}
				uomTableItem.setText(columnIdx, ok);
				uomCheckBox.dispose();
				// At EOL, create new, else next control
				boolean isAtSoldColumnAndHasReportUom = ((columnIdx == SOLD_UOM_COLUMN) && isReportedUomEntered);
				boolean isAtReportColumn = (columnIdx == REPORTED_UOM_COLUMN);
				if (isAtSoldColumnAndHasReportUom || isAtReportColumn) {
					columnIdx = 0;
					if (rowIdx != 0 && isNoUomChecked) {
						usedUoms.remove(uomTableItem.getText(2));
						uomTableItem.dispose();
						rowIdx--;
					}
					isNoUomChecked = true;
					uomTable.setTopIndex(uomTable.getItemCount() - 1);
					uomTableItem = new TableItem(uomTable, SWT.NONE, ++rowIdx);
					uomTableItem.setText(columnIdx, String.valueOf(rowIdx + 1));
					setQtyPerUomInput();
					setNext(qtyPerUomInput);
					qtyPerUomList.add(new QtyPerUOM(item.getQty(), item.getUomId(), isBoughtUomEntered,
					        isSoldUomEntered, isReportedUomEntered));
					item.setQtyPerUomList(qtyPerUomList);
					return;
				}
				++columnIdx;
				setBuyOrSellOrReportCheckBoxes();
			}
		};
	}

	private void setQtyPerUomInput() {
		qtyPerUomInput = new TableTextInput(uomTableItem, rowIdx, ++columnIdx, BigDecimal.ZERO).getText();
		qtyPerUomInput.setTouchEnabled(true);
		qtyPerUomInput.setFocus();
		new TextInputter(qtyPerUomInput, uomCombo) {
			@Override
			protected boolean isInputValid() {
				if (textInput.isEmpty()) {
					if (rowIdx > 0
					        && ((!isPurchased || isBoughtUomEntered) && isReportedUomEntered && isSoldUomEntered)
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
							setNext(saveButton);
						}
						return true;
					}
					return false;
				}
				BigDecimal qty = new BigDecimal(textInput);
				if (qty.compareTo(BigDecimal.ZERO) <= 0)
					return false;
				uomTableItem.setText(columnIdx++, DIS.FOUR_PLACE_DECIMAL.format(qty));
				item.setQty(qty);
				qtyPerUomInput.dispose();
				setUomCombo();
				return true;
			}
		};
	}

	private void setUomCombo() {
		String[] uoms = new UOM().getUoms(usedUoms);
		if (uoms.length == 0) {
			if (isPurchased && !isBoughtUomEntered) {
				new ErrorDialog("Purchased item must have a buying UOM;\ntry again from the top.");
				shell.dispose();
				new ItemView(0);
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
			protected void doAfterSelection() {
				uomTableItem.setText(columnIdx, selection);
				usedUoms.add(selection);
				uomCombo.dispose();
				item.setUomId(new UOM(selection).getId());
				columnIdx = isPurchased ? BOUGHT_UOM_COLUMN : SOLD_UOM_COLUMN;
				setBuyOrSellOrReportCheckBoxes();
			}
		};
	}

	// Item volume discount amount input
	private void setDiscountInput() {
		txtDiscount = new TableTextInput(discountTableItem, rowIdx, columnIdx, BigDecimal.ZERO).getText();
		txtDiscount.setTouchEnabled(true);
		txtDiscount.setFocus();
		new TextInputter(txtDiscount, txtVolume) {
			@Override
			protected boolean isInputValid() {
				less = BigDecimal.ZERO;
				if (textInput.isEmpty()) {
					txtDiscount.dispose();
					discountTableItem.dispose();
					rowIdx = 0;
					columnIdx = 0;
					priceTableItem = new TableItem(priceTable, SWT.NONE, rowIdx);
					priceTableItem.setText(columnIdx++, String.valueOf(rowIdx + 1));
					setPriceInput();
					setNext(txtPrice);
					return true;
				} else {
					less = new BigDecimal(textInput);
					if (less.compareTo(BigDecimal.ZERO) < 1)
						return false;
					textInput = DIS.NO_COMMA_DECIMAL.format(less);
				}
				discountTableItem.setText(columnIdx++, textInput);
				txtDiscount.dispose();
				setVolumeInput();
				return true;
			}
		};
	}

	// Item volume discount quantity cut-off
	private void setVolumeInput() {
		txtVolume = new TableTextInput(discountTableItem, rowIdx, columnIdx, 0).getText();
		txtVolume.setTouchEnabled(true);
		txtVolume.setFocus();
		new TextInputter(txtVolume, discountUomCombo) {
			@Override
			protected boolean isInputValid() {
				if (textInput.isEmpty())
					return false;
				perQty = Integer.parseInt(textInput);
				if (perQty < 1)
					return false;
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
		String[] uoms = new UOM().getUoms();
		discountUomCombo = new TableCombo(discountTableItem, columnIdx, uoms).getCombo();
		discountUomCombo.setEnabled(true);
		discountUomCombo.setFocus();
		new ComboSelector(discountUomCombo, channelCombo) {
			@Override
			protected void doAfterSelection() {
				discountTableItem.setText(columnIdx++, selection);
				uomId = new UOM(selection).getId();
				discountUomCombo.dispose();
				setChannelCombo();
			}
		};
	}

	// Item volume discount applicable channel
	private void setChannelCombo() {
		channelCombo = new TableCombo(discountTableItem, columnIdx, new Channel().getChannels()).getCombo();
		channelCombo.setEnabled(true);
		channelCombo.setFocus();
		new ComboSelector(channelCombo, discountStartDateInput) {
			@Override
			protected void doAfterSelection() {
				discountTableItem.setText(columnIdx++, selection);
				channelId = new Channel(selection).getId();
				channelCombo.dispose();
				setDiscountStartDateInput();
			}
		};
	}

	// Item volume discount start date input
	private void setDiscountStartDateInput() {
		discountStartDateInput = new TableTextInput(discountTableItem, rowIdx, columnIdx, DIS.TOMORROW).getText();
		discountStartDateInput.setTouchEnabled(true);
		discountStartDateInput.setFocus();
		new DateInputter(discountStartDateInput, txtDiscount) {
			@Override
			protected boolean isInputValid() {
				discountTableItem.setText(columnIdx++, textInput);
				discountStartDateInput.dispose();
				columnIdx = 0;
				item.getVolumeDiscountList().add(
				        new VolumeDiscount(less, perQty, uomId, channelId, DIS.parseDate(textInput)));
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
		txtPrice = new TableTextInput(priceTableItem, rowIdx, columnIdx, BigDecimal.ZERO).getText();
		txtPrice.setTouchEnabled(true);
		txtPrice.setFocus();
		new TextInputter(txtPrice, txtPrice) {
			@Override
			protected boolean isInputValid() {
				BigDecimal price = BigDecimal.ZERO;
				if (textInput.isEmpty()) {
					boolean isBoughtAndAtPurchasedColumn = columnIdx == PURCHASE_COLUMN && isPurchased;
					boolean isAtSoldToDealerColumn = columnIdx == DEALER_COLUMN || columnIdx == RETAIL_COLUMN;
					boolean isRefMeatAndAtSoldToSupermarketColumn = isRefMeat
					        && (columnIdx == SUPERMKT_COLUMN || columnIdx == SUPERSRP_COLUMN);
					if (isBoughtAndAtPurchasedColumn
					        || (isSoldUomEntered && (isAtSoldToDealerColumn || isRefMeatAndAtSoldToSupermarketColumn))) {
						return false;
					}
					if (columnIdx == PURCHASE_COLUMN && rowIdx > 0 && isBoughtUomEntered && isReportedUomEntered) {
						txtPrice.dispose();
						setNext(saveButton);
					}
				} else {
					price = new BigDecimal(textInput);
					if (price.compareTo(BigDecimal.ZERO) < 1)
						return false;
					textInput = DIS.NO_COMMA_DECIMAL.format(price);
				}
				priceTableItem.setText(columnIdx, textInput);
				txtPrice.dispose();
				if (!price.equals(BigDecimal.ZERO))
					switch (columnIdx) {
					case PURCHASE_COLUMN:
						item.setPurchasePrice(price);
						break;
					case DEALER_COLUMN:
						item.setDealerPrice(price);
						break;
					case RETAIL_COLUMN:
						item.setRetailPrice(price);
						break;
					case SUPERMKT_COLUMN:
						item.setSupermarketPrice(price);
						break;
					case SUPERSRP_COLUMN:
						item.setSupermarketSRPrice(price);
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
		};
	}

	// Item price start date input
	private void setPriceDateInput() {
		priceStartDateInput = new TableTextInput(priceTableItem, rowIdx, columnIdx, DIS.TOMORROW).getText();
		priceStartDateInput.setTouchEnabled(true);
		priceStartDateInput.setFocus();
		new DateInputter(priceStartDateInput, saveButton) {
			@Override
			protected boolean isTheDataInputValid() {
				priceTableItem.setText(columnIdx++, textInput);
				priceStartDateInput.dispose();
				item.setPriceStartDate(date);
				return true;
			}
		};
	}

	@Override
	protected void setFocus() {
		if (id == 0)
			shortIdInput.setFocus();
	}

	public Text getTxtShortId() {
		return shortIdInput;
	}

	public Combo getCmbType() {
		return typeCombo;
	}

	public void setCmbType(Combo cmbType) {
		this.typeCombo = cmbType;
	}

	public Text getTxtName() {
		return nameInput;
	}

	public Text getTxtUnspscId() {
		return unspscInput;
	}

	public Button getNotDiscountedOrIsCheckBox() {
		return notDiscountedOrIsCheckBox;
	}

	public Combo getCmbProductLine() {
		return productLineCombo;
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

	// Main method
	public static void main(String[] args) {
		Database.getInstance().getConnection("sheryl", "10-8-91", "mgdc_smis");
		new ItemView(0);
		Database.getInstance().closeConnection();
	}
}

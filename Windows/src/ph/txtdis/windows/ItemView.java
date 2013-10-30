package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ItemView extends OrderView {
	private int rowIdx, columnIdx;
	private boolean isPurchased, isTraded, isBought, isSold, isReported, wereNoDataEntered, isRefMeat;
	private ArrayList<QtyPerUOM> qtyPerUOMList;
	private ItemHelper helper;
	private ItemMaster item;
	private Button saveButton, bomButton, notDiscountedOrIsCheckBox, uomCheckBox;
	private Combo typeCombo, cmbProductLine, cmbUom, cmbDiscountUom, cmbChannel;
	private Text txtShortId, nameInput, txtUnspscId, txtQty, txtPrice, priceStartDateInput, txtDiscount, txtVolume,
	        discountStartDateInput;
	private Table uomTable, priceTable, discountTable;
	private TableItem uomTableItem, priceTableItem, discountTableItem;
	private String type, discountUom;
	private ArrayList<String> usedUoms;

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
		wereNoDataEntered = true;
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
				if (id != 0 && (group.equals("super_supply") || group.equals("system_user"))) {
					new ImageButton(buttons, module, "Tag", "Update prices") {
						@Override
						protected void doWhenSelected() {
							columnIdx = 0;
							rowIdx = item.getPriceData().length;
							isSold = helper.isSold(id);
							isRefMeat = helper.isRefMeat(cmbProductLine.getText());
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
		Group header = new Grp(shell, 7, "DETAILS", SWT.CENTER, SWT.BEGINNING, true, false, 2, 1).getGroup();

		new TextDisplayBox(header, "ITEM CODE #", item.getId()).getText();
		txtShortId = new TextInputBox(header, "NAME", item.getShortId(), 1, 16).getText();
		typeCombo = new ComboBox(header, item.getTypes(), "TYPE").getCombo();
		
		bomButton = new BomButton(header, item).getButton();
		bomButton.setEnabled(type == null ? false : helper.isWithBOM(type));
		
		nameInput = new TextInputBox(header, "DESCRIPTION", item.getName(), 6, 52).getText();
		txtUnspscId = new TextInputBox(header, "UNSPSC ID #", item.getUnspscId()).getText();

		notDiscountedOrIsCheckBox = new CheckButton(header, "NOT DISCOUNTED", item.isNotDiscounted()).getButton();
		notDiscountedOrIsCheckBox.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));

		cmbProductLine = new ComboBox(header, item.getProductLines(), "LINE").getCombo();
	}

	@Override
	public Table getTable() {
		Group uom = new Grp(shell, 1, "QUANTITY PER UOM RELATIVE TO \"PK\"", SWT.CENTER, SWT.BEGINNING, true, false, 1,
		        1).getGroup();
		uomTable = new ReportTable(uom, item.getUomData(), item.getUomHeaders(), "", 90, true) {
			@Override
			protected void doubleClickListener() {
				// disabled
			}
		}.getTable();
		uomTable.setTopIndex(3);

		Group discount = new Grp(shell, 1, "VOLUME DISCOUNT", SWT.CENTER, SWT.BEGINNING, true, false, 1, 1).getGroup();
		discountTable = new ReportTable(discount, item.getDiscountData(), item.getDiscountHeaders(), "", 50, true) {
			@Override
			protected void doubleClickListener() {
				// disabled
			}
		}.getTable();
		discountTable.setTopIndex(discountTable.getItemCount() - 1);

		Group price = new Grp(shell, 1, "PRICE PER PK", SWT.CENTER, SWT.BEGINNING, true, false, 1, 1).getGroup();
		priceTable = new ReportTable(price, item.getPriceData(), item.getPriceHeaders(), "", 50, true) {
			@Override
			protected void doubleClickListener() {
				// disabled
			}
		}.getTable();
		priceTable.setTopIndex(priceTable.getItemCount() - 1);
		return null;
	}

	@Override
	protected void setListener() {
		new TextInputter(txtShortId, typeCombo) {
			@Override
			protected boolean isTheDataInputValid() {
				if (helper.getId(textInput) != 0) {
					new ErrorDialog(textInput + " has been used;\ntry another.");
					return false;
				}
				item.setShortId(textInput);
				return true;
			}
		};

		new ComboSelector(typeCombo, nameInput) {
			@Override
			protected void doAfterSelection() {
				type = selection;
				isPurchased = helper.isPurchased(type);
				isTraded = helper.isTraded(type);
				if (helper.isWithBOM(type))
					setNext(bomButton);
				item.setItemType(type);
			}
		};

		new TextInputter(nameInput, txtUnspscId) {
			@Override
			protected boolean isTheDataInputValid() {
				item.setName(textInput);
				return true;
			}
		};

		new TextInputter(txtUnspscId, notDiscountedOrIsCheckBox) {
			@Override
			protected boolean isABlankInputNotValid() {
				if (isPurchased) {
					new ErrorDialog("Purchased items must have\ncorresponding UNSPSC numbers");
					return true;
				}
				return false;
			}

			@Override
			protected boolean isThePositiveNumberValid() {
				long unspscId = numericInput.longValue();
				if (textInput.length() != 13) {
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

		new CheckBoxSelector(notDiscountedOrIsCheckBox, cmbProductLine) {
			@Override
			protected void doAfterSelection() {
				item.setNotDiscounted(checkBox.getSelection());
			}
		};

		new ComboSelector(cmbProductLine, uomCheckBox) {
			@Override
			protected void doAfterSelection() {
				item.setProductLine(selection);
				isRefMeat = helper.isRefMeat(selection);
				cmbProductLine.setEnabled(false);
				uomTableItem = new TableItem(uomTable, SWT.NONE, rowIdx);
				uomTableItem.setText(0, "1");
				uomTableItem.setText(1, "1.0000");
				uomTableItem.setText(2, "PK");
				usedUoms.add("PK");

				item.setQty(BigDecimal.ONE);
				item.setUomId(0);

				columnIdx = isPurchased ? BOUGHT_UOM_COLUMN : SOLD_UOM_COLUMN;
				qtyPerUOMList = item.getQtyPerUOMList();
				setBuyOrSellOrReportCheckBoxes();
			}
		};
	}

	private void setBuyOrSellOrReportCheckBoxes() {
		uomCheckBox = new TableCheckButton(uomTableItem, rowIdx, columnIdx).getButton();
		uomCheckBox.setBackground(DIS.YELLOW);
		uomCheckBox.setFocus();
		new CheckBoxSelector(uomCheckBox, txtQty) {
			@Override
			protected void doAfterSelection() {
				String ok = "";
				if (uomCheckBox.getSelection()) {
					if (!((columnIdx == REPORTED_UOM_COLUMN && isReported) || (columnIdx == BOUGHT_UOM_COLUMN && isBought)))
						ok = "OK";
					switch (columnIdx) {
						case BOUGHT_UOM_COLUMN:
							isBought = true;
							break;
						case SOLD_UOM_COLUMN:
							isSold = true;
							break;
						case REPORTED_UOM_COLUMN:
							isReported = true;
							break;
					}
					wereNoDataEntered = false;
				}
				uomTableItem.setText(columnIdx, ok);
				uomCheckBox.dispose();
				// At EOL, create new, else next control
				boolean isAtSoldColumnButHasReportSet = ((columnIdx == SOLD_UOM_COLUMN) && isReported);
				boolean isAtReportedSlashLastColumn = (columnIdx == REPORTED_UOM_COLUMN);
				if (isAtSoldColumnButHasReportSet || isAtReportedSlashLastColumn) {
					columnIdx = 0;
					if (rowIdx != 0 && wereNoDataEntered) {
						usedUoms.remove(uomTableItem.getText(2));
						uomTableItem.dispose();
						rowIdx--;
					}
					wereNoDataEntered = true;
					uomTableItem = new TableItem(uomTable, SWT.NONE, ++rowIdx);
					uomTableItem.setText(columnIdx, String.valueOf(rowIdx + 1));
					setQtyPerUOMInput();
					setNext(txtQty);
					qtyPerUOMList.add(new QtyPerUOM(item.getQty(), item.getUomId(), isBought, isSold, isReported));
					return;
				}
				++columnIdx;
				setBuyOrSellOrReportCheckBoxes();
			}
		};
	}

	private void setQtyPerUOMInput() {
		txtQty = new TableTextInput(uomTableItem, rowIdx, ++columnIdx, BigDecimal.ZERO).getText();
		txtQty.setTouchEnabled(true);
		txtQty.setFocus();
		new TextInputter(txtQty, cmbUom) {
			@Override
			protected boolean isInputValid() {
				if (textInput.isEmpty()) {
					if (rowIdx > 0 && (isPurchased && isBought) && (isTraded && isReported && isSold)) {
						txtQty.dispose();
						uomTableItem.dispose();
						columnIdx = 0;
						rowIdx = 0;
						if (isSold) {
							discountTableItem = new TableItem(discountTable, SWT.NONE, rowIdx);
							discountTableItem.setText(columnIdx++, String.valueOf(rowIdx + 1));
							setDiscountInput();
							setNext(txtDiscount);
						} else {
							if (isBought) {
								priceTableItem = new TableItem(priceTable, SWT.NONE, rowIdx);
								priceTableItem.setText(columnIdx++, String.valueOf(rowIdx + 1));
								setPriceInput();
								setNext(txtPrice);
							} else {
								setNext(saveButton);
							}
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
				txtQty.dispose();
				setUomCombo();
				return true;
			}
		};
	}

	// Item UOM selector
	private void setUomCombo() {
		String[] uoms = new UOM().getUoms(usedUoms);
		if (uoms.length == 0) {
			if (isPurchased && !isBought) {
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
		cmbUom = new TableCombo(uomTableItem, columnIdx, uoms).getCombo();
		cmbUom.setEnabled(true);
		cmbUom.setFocus();
		new ComboSelector(cmbUom, uomCheckBox) {
			@Override
			protected void doAfterSelection() {
				uomTableItem.setText(columnIdx, selection);
				usedUoms.add(selection);
				cmbUom.dispose();
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
				BigDecimal discount = BigDecimal.ZERO;
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
					discount = new BigDecimal(textInput);
					if (discount.compareTo(BigDecimal.ZERO) < 1)
						return false;
					textInput = DIS.NO_COMMA_DECIMAL.format(discount);
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
		new TextInputter(txtVolume, cmbDiscountUom) {
			@Override
			protected boolean isInputValid() {
				if (textInput.isEmpty())
					return false;
				if (Integer.parseInt(textInput) < 1)
					return false;
				discountTableItem.setText(columnIdx++, textInput);
				txtVolume.dispose();
				if (discountUom == null) {
					setDiscountUomCombo();
				} else {
					discountTableItem.setText(columnIdx++, discountUom);
					setChannelCombo();
					setNext(cmbChannel);
				}
				return true;
			}
		};
	}

	// Item volume discount UOM selector
	private void setDiscountUomCombo() {
		String[] uoms = new UOM().getUoms();
		cmbDiscountUom = new TableCombo(discountTableItem, columnIdx, uoms).getCombo();
		cmbDiscountUom.setEnabled(true);
		cmbDiscountUom.setFocus();
		new ComboSelector(cmbDiscountUom, cmbChannel) {
			@Override
			protected void doAfterSelection() {
				discountTableItem.setText(columnIdx++, selection);
				cmbDiscountUom.dispose();
				setChannelCombo();
			}
		};
	}

	// Item volume discount applicable channel
	private void setChannelCombo() {
		cmbChannel = new TableCombo(discountTableItem, columnIdx, new Channel().getChannels()).getCombo();
		cmbChannel.setEnabled(true);
		cmbChannel.setFocus();
		new ComboSelector(cmbChannel, discountStartDateInput) {
			@Override
			protected void doAfterSelection() {
				discountTableItem.setText(columnIdx++, selection);
				cmbChannel.dispose();
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
					        || (isSold && (isAtSoldToDealerColumn || isRefMeatAndAtSoldToSupermarketColumn))) {
						return false;
					}
					if (columnIdx == PURCHASE_COLUMN && rowIdx > 0 && isBought && isReported) {
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
				if (columnIdx == SUPERSRP_COLUMN || !isSold || (columnIdx == RETAIL_COLUMN && !isRefMeat)) {
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
			txtShortId.setFocus();
	}

	public Text getTxtShortId() {
		return txtShortId;
	}

	public Combo getCmbType() {
		return typeCombo;
	}

	public void setCmbType(Combo cmbType) {
		this.typeCombo = cmbType;
	}

	public Button getBtnBom() {
		return bomButton;
	}

	public Text getTxtName() {
		return nameInput;
	}

	public Text getTxtUnspscId() {
		return txtUnspscId;
	}

	public Button getNotDiscountedOrIsCheckBox() {
		return notDiscountedOrIsCheckBox;
	}

	public Combo getCmbProductLine() {
		return cmbProductLine;
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
		// Database.getInstance().getConnection("irene","ayin","localhost");
		
		Database.getInstance().getConnection("sheryl", "10-8-91", "localhost");
		//Database.getInstance().getConnection("sheryl", "10-8-91", "192.168.1.100");
		Login.setGroup("super_supply");
		new ItemView(328);
		Database.getInstance().closeConnection();
	}
}

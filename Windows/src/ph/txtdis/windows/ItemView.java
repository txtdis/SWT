package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ItemView extends ReportView {
	private int id, rowIdx, columnIdx, priceSize;
	private boolean isPurchased, isTraded, isWithBOM, isBought, isSold, isReported, wereNoDataEntered, isRefMeat;
	private ItemHelper helper;
	private ItemMaster item;
	private Button btnPost, btnBom, btnDiscount, btnUom;
	private Combo cmbType, cmbProductLine, cmbUom, cmbDiscountUom, cmbChannel;
	private Text txtId, txtShortId, txtName, txtUnspscId, txtQty, txtPrice, txtPriceDate;
	private Text txtDiscount, txtVolume, txtDiscountDate;
	private Table tblUom, tblPrice, tblDiscount;
	private TableItem itmUom, itmPrice, itmDiscount;
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
		report = item = new ItemMaster(id);
	}

	@Override
	protected void setTitleBar() {
		MasterTitleBar mtb = new MasterTitleBar(this, report) {
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
							itmPrice = new TableItem(tblPrice, SWT.NONE, rowIdx);
							itmPrice.setText(columnIdx++, String.valueOf(rowIdx + 1));
							getButton().setEnabled(false);
							tblPrice.setTopIndex(priceSize);
							setPriceInput();
						}
					};
					setBtnPost(new PostButton(buttons, reportView, report).getButton());
				}
			}
		};
		btnPost = mtb.getBtnPost();
	}

	@Override
	protected void setHeader() {
		Group header = new Group(shell, SWT.NONE);
		header.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, false, 2, 1));
		header.setText("DETAILS");
		header.setLayout(new GridLayout(7, false));

		int id = item.getId();
		long unspscId = item.getUnspscId();
		String smsId = item.getShortId();
		String name = item.getName();
		// item ID display
		txtId = new DataDisplay(header, "ITEM CODE #", id).getText();
		// 16 alphanumeric short item name input
		txtShortId = new DataEntry(header, "NAME", smsId, 1, 16).getText();
		// Item type selector
		cmbType = new DataSelection(header, item.getTypes(), "TYPE", item.getType()).getCombo();
		// BOM dialog launcher button
		btnBom = new BomButton(header, item).getButton();
		isWithBOM = type == null ? false : helper.isWithBOM(type);
		if (!isWithBOM) {
			btnBom.setEnabled(false);
		}
		// 52 alphanumeric item description input
		txtName = new DataEntry(header, "DESCRIPTION", name, 6, 52).getText();
		// UN SPSC input
		txtUnspscId = new DataEntry(header, "UNSPSC ID #", unspscId).getText();
		// Discounted or not toggle button
		btnDiscount = new Button(header, SWT.CHECK | SWT.RIGHT_TO_LEFT);
		btnDiscount.setText("NOT DISCOUNTED");
		btnDiscount.setFont(DIS.MONO);
		btnDiscount.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
		btnDiscount.setSelection(item.isNotDiscounted());
		// Product line selector
		cmbProductLine = new DataSelection(header, item.getProductLines(), "LINE", item.getProductLine()).getCombo();
	}

	@Override
	public Table getTable() {
		// UOM
		Group grpUom = new Group(shell, SWT.NONE);
		grpUom.setText("QUANTITY PER UOM RELATIVE TO \"PK\"");
		grpUom.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, false, 1, 1));
		grpUom.setLayout(new GridLayout(1, false));
		Object[][] uomData = item.getUomData();
		String[][] uomHeaders = item.getUomHeaders();
		tblUom = new ReportTable(grpUom, uomData, uomHeaders, "", 90, true) {
			@Override
			protected void doubleClickListener() {
				// disabled
			}
		}.getTable();
		tblUom.setTopIndex(3);

		// DISCOUNT
		Group grpDiscount = new Group(shell, SWT.NONE);
		grpDiscount.setText("VOLUME DISCOUNT");
		grpDiscount.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, false, 1, 1));
		grpDiscount.setLayout(new GridLayout(1, false));
		String[][] discountHeaders = item.getDiscountHeaders();
		Object[][] discounts = item.getDiscountData();
		tblDiscount = new ReportTable(grpDiscount, discounts, discountHeaders, "", 50, true) {
			@Override
			protected void doubleClickListener() {
				// disabled
			}
		}.getTable();
		int discountSize = discounts == null ? 0 : discounts.length;
		tblDiscount.setTopIndex(discountSize - 1);

		// PRICE
		Group grpPrice = new Group(shell, SWT.NONE);
		grpPrice.setText("PRICE PER PK");
		grpPrice.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, false, 1, 1));
		grpPrice.setLayout(new GridLayout(1, false));
		String[][] priceHeaders = item.getPriceHeaders();
		Object[][] prices = item.getPriceData();
		tblPrice = new ReportTable(grpPrice, prices, priceHeaders, "", 50, true) {
			@Override
			protected void doubleClickListener() {
				// disabled
			}
		}.getTable();
		priceSize = prices == null ? 0 : prices.length;
		tblPrice.setTopIndex(priceSize - 1);
		return null;
	}

	@Override
	protected void setListener() {
		// Short name input
		new DataInput(txtShortId, cmbType) {
			@Override
			protected boolean isInputValid() {
				String shortId = txtShortId.getText().trim();
				if (shortId.isEmpty())
					return false;
				if (helper.getId(shortId) != 0) {
					new ErrorDialog(shortId + " has been used;\ntry another.");
					return false;
				}
				return true;
			}
		};

		// Item type selection
		new DataSelector(cmbType, txtName) {
			@Override
			protected void doWhenSelected() {
				type = cmbType.getText();
				isWithBOM = helper.isWithBOM(type);
				isPurchased = helper.isPurchased(type);
				isTraded = helper.isTraded(type);
				if (isWithBOM) {
					btnBom.setEnabled(true);
					setNext(btnBom);
				}
			}
		};

		// Item name input
		new DataInput(txtName, txtUnspscId) {
			@Override
			protected boolean isDataInputValid() {
				if (helper.getShortId(string) != null) {
					new ErrorDialog(string + " has been used;\ntry another.");
					return false;
				} else {
					return true;
				}
			}
		};

		// Item UNSPSC ID input
		new DataInput(txtUnspscId, btnDiscount) {
			@Override
			protected boolean isBlankInputNotValid() {
				if (isPurchased) {
					new ErrorDialog("Purchased items must have\ncorresponding UNSPSC numbers");
					return false;
				} else {
					return true;
				}
			}

			@Override
			protected boolean isDataInputValid() {
				String unspsc = string;
				if (unspsc.length() != 13) {
					new ErrorDialog("UNSPSC # must be 13 digits long;\ntry again.");
					return false;
				} else if (helper.getId(Long.parseLong(unspsc)) != 0) {
					new ErrorDialog(unspsc + " has been used;\ntry another.");
					return false;
				} else {
					return true;
				}
			}
		};

		// Item discount exemption selection
		new DataSwitcher(btnDiscount, cmbProductLine);

		// Item product line selection
		new DataSelector(cmbProductLine, btnUom) {
			@Override
			protected void doWhenSelected() {
				String productLine = cmbProductLine.getText();
				isRefMeat = helper.isRefMeat(productLine);
				cmbProductLine.setEnabled(false);
				itmUom = new TableItem(tblUom, SWT.NONE, rowIdx);
				itmUom.setText(0, "1");
				itmUom.setText(1, "1.0000");
				itmUom.setText(2, "PK");
				usedUoms.add("PK");
				columnIdx = isPurchased ? BOUGHT_UOM_COLUMN : SOLD_UOM_COLUMN;
				setUomButton();
				setNext(btnUom);
			}
		};
	}

	// Item UOM buy/sell/report toggle selector
	private void setUomButton() {
		btnUom = new TableCheckButton(itmUom, rowIdx, columnIdx).getButton();
		btnUom.setBackground(DIS.YELLOW);
		btnUom.setFocus();
		new DataSwitcher(btnUom, txtQty) {
			@Override
			protected void doWhenSelected() {
				boolean isSelected = btnUom.getSelection();
				String ok = "";
				if (isSelected) {
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
				itmUom.setText(columnIdx, ok);
				btnUom.dispose();
				// At EOL, create new, else next control
				boolean isAtSoldColumnButHasReportSet = ((columnIdx == SOLD_UOM_COLUMN) && isReported);
				boolean isAtReportedSlashLastColumn = (columnIdx == REPORTED_UOM_COLUMN);
				if (isAtSoldColumnButHasReportSet || isAtReportedSlashLastColumn) {
					columnIdx = 0;
					if (rowIdx != 0 && wereNoDataEntered) {
						usedUoms.remove(itmUom.getText(2));
						itmUom.dispose();
						rowIdx--;
					}
					wereNoDataEntered = true;
					itmUom = new TableItem(tblUom, SWT.NONE, ++rowIdx);
					itmUom.setText(columnIdx, String.valueOf(rowIdx + 1));
					setQtyInput();
					setNext(txtQty);
					return;
				}
				++columnIdx;
				setUomButton();
				setNext(btnUom);
			}
		};
	}

	// Item quantity per UOM input listener
	private void setQtyInput() {
		txtQty = new TableInput(itmUom, rowIdx, ++columnIdx, BigDecimal.ZERO).getText();
		txtQty.setTouchEnabled(true);
		txtQty.setFocus();
		new DataInput(txtQty, cmbUom) {
			@Override
			protected boolean isInputValid() {
				String strQty = txtQty.getText().trim();
				if (strQty.isEmpty()) {
					if (rowIdx > 0 && (isPurchased && isBought) && (isTraded && isReported && isSold)) {
						txtQty.dispose();
						itmUom.dispose();
						columnIdx = 0;
						rowIdx = 0;
						if (isSold) {
							itmDiscount = new TableItem(tblDiscount, SWT.NONE, rowIdx);
							itmDiscount.setText(columnIdx++, String.valueOf(rowIdx + 1));
							setDiscountInput();
							setNext(txtDiscount);
						} else {
							if (isBought) {
								itmPrice = new TableItem(tblPrice, SWT.NONE, rowIdx);
								itmPrice.setText(columnIdx++, String.valueOf(rowIdx + 1));
								setPriceInput();
								setNext(txtPrice);
							} else {
								setNext(btnPost);
							}
						}
						return true;
					}
					return false;
				}
				BigDecimal qty = new BigDecimal(strQty);
				if (qty.compareTo(BigDecimal.ZERO) <= 0)
					return false;
				itmUom.setText(columnIdx++, DIS.FOUR_PLACE_DECIMAL.format(qty));
				txtQty.dispose();
				setUomCombo();
				setNext(cmbUom);
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
			itmUom.dispose();
			rowIdx = 0;
			columnIdx = 0;
			itmPrice = new TableItem(tblPrice, SWT.NONE, rowIdx);
			itmPrice.setText(columnIdx++, String.valueOf(rowIdx + 1));
			setPriceInput();
			txtPrice.setTouchEnabled(true);
			txtPrice.setFocus();
			return;
		}
		cmbUom = new TableSelection(itmUom, rowIdx, columnIdx, uoms, null).getCombo();
		cmbUom.setEnabled(true);
		cmbUom.setFocus();
		new DataSelector(cmbUom, btnUom) {
			@Override
			protected void doWhenSelected() {
				String uom = cmbUom.getText();
				itmUom.setText(columnIdx, uom);
				usedUoms.add(uom);
				cmbUom.dispose();
				columnIdx = isPurchased ? BOUGHT_UOM_COLUMN : SOLD_UOM_COLUMN;
				setUomButton();
				setNext(btnUom);
			}
		};
	}

	// Item volume discount amount input
	private void setDiscountInput() {
		txtDiscount = new TableInput(itmDiscount, rowIdx, columnIdx, BigDecimal.ZERO).getText();
		txtDiscount.setTouchEnabled(true);
		txtDiscount.setFocus();
		new DataInput(txtDiscount, txtVolume) {
			@Override
			protected boolean isInputValid() {
				String strDiscount = txtDiscount.getText().trim();
				BigDecimal discount = BigDecimal.ZERO;
				if (strDiscount.isEmpty()) {
					txtDiscount.dispose();
					itmDiscount.dispose();
					rowIdx = 0;
					columnIdx = 0;
					itmPrice = new TableItem(tblPrice, SWT.NONE, rowIdx);
					itmPrice.setText(columnIdx++, String.valueOf(rowIdx + 1));
					setPriceInput();
					setNext(txtPrice);
					return true;
				} else {
					discount = new BigDecimal(strDiscount);
					if (discount.compareTo(BigDecimal.ZERO) < 1)
						return false;
					strDiscount = DIS.NO_COMMA_DECIMAL.format(discount);
				}
				itmDiscount.setText(columnIdx++, strDiscount);
				txtDiscount.dispose();
				setVolumeInput();
				setNext(txtVolume);
				return true;
			}
		};
	}

	// Item volume discount quantity cut-off
	private void setVolumeInput() {
		txtVolume = new TableInput(itmDiscount, rowIdx, columnIdx, 0).getText();
		txtVolume.setTouchEnabled(true);
		txtVolume.setFocus();
		new DataInput(txtVolume, cmbDiscountUom) {
			@Override
			protected boolean isInputValid() {
				String strVolume = txtVolume.getText().trim();
				if (strVolume.isEmpty())
					return false;
				if (Integer.parseInt(strVolume) < 1)
					return false;
				itmDiscount.setText(columnIdx++, strVolume);
				txtVolume.dispose();
				if (discountUom == null) {
					setDiscountUomCombo();
					setNext(cmbDiscountUom);
				} else {
					itmDiscount.setText(columnIdx++, discountUom);
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
		cmbDiscountUom = new TableSelection(itmDiscount, rowIdx, columnIdx, uoms, null).getCombo();
		cmbDiscountUom.setEnabled(true);
		cmbDiscountUom.setFocus();
		new DataSelector(cmbDiscountUom, cmbChannel) {
			@Override
			protected void doWhenSelected() {
				discountUom = cmbDiscountUom.getText();
				itmDiscount.setText(columnIdx++, discountUom);
				cmbDiscountUom.dispose();
				setChannelCombo();
				setNext(cmbChannel);
			}
		};
	}

	// Item volume discount applicable channel
	private void setChannelCombo() {
		cmbChannel = new TableSelection(itmDiscount, rowIdx, columnIdx, new Channel().getChannels(), null).getCombo();
		cmbChannel.setEnabled(true);
		cmbChannel.setFocus();
		new DataSelector(cmbChannel, txtDiscountDate) {
			@Override
			protected void doWhenSelected() {
				String channel = cmbChannel.getText();
				itmDiscount.setText(columnIdx++, channel);
				cmbChannel.dispose();
				setDiscountDateInput();
				setNext(txtDiscountDate);
			}
		};
	}

	// Item volume discount start date input
	private void setDiscountDateInput() {
		final DateAdder today = new DateAdder();
		txtDiscountDate = new TableInput(itmDiscount, rowIdx, columnIdx, today.plus(1)).getText();
		txtDiscountDate.setTouchEnabled(true);
		txtDiscountDate.setFocus();
		new DataInput(txtDiscountDate, txtDiscount, today.add(1)) {
			@Override
			protected boolean isInputValid() {
				String strDate = txtDiscountDate.getText();
				itmDiscount.setText(columnIdx++, strDate);
				txtDiscountDate.dispose();
				columnIdx = 0;
				itmDiscount = new TableItem(tblDiscount, SWT.NONE, ++rowIdx);
				itmDiscount.setText(columnIdx++, String.valueOf(rowIdx + 1));
				setDiscountInput();
				setNext(txtDiscount);
				return true;
			}
		};
	}

	// Item price input
	private void setPriceInput() {
		txtPrice = new TableInput(itmPrice, rowIdx, columnIdx, BigDecimal.ZERO).getText();
		txtPrice.setTouchEnabled(true);
		txtPrice.setFocus();
		new DataInput(txtPrice, txtPrice) {
			@Override
			protected boolean isInputValid() {
				String strPrice = txtPrice.getText().trim();
				BigDecimal price = BigDecimal.ZERO;
				if (strPrice.isEmpty()) {
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
						setNext(btnPost);
					}
				} else {
					price = new BigDecimal(strPrice);
					if (price.compareTo(BigDecimal.ZERO) < 1)
						return false;
					strPrice = DIS.NO_COMMA_DECIMAL.format(price);
				}
				itmPrice.setText(columnIdx, strPrice);
				txtPrice.dispose();
				if (columnIdx == SUPERSRP_COLUMN || !isSold || (columnIdx == RETAIL_COLUMN && !isRefMeat)) {
					columnIdx = START_DATE_COLUMN;
					setPriceDateInput();
					setNext(txtPriceDate);
				} else {
					columnIdx++;
					setPriceInput();
					setNext(txtPrice);
				}
				return true;
			}
		};
	}

	// Item price start date input
	private void setPriceDateInput() {
		final DateAdder today = new DateAdder();
		txtPriceDate = new TableInput(itmPrice, rowIdx, columnIdx, today.plus(1)).getText();
		txtPriceDate.setTouchEnabled(true);
		txtPriceDate.setFocus();
		new DataInput(txtPriceDate, btnPost, today.add(1)) {
			@Override
			protected boolean isInputValid() {
				String strDate = txtPriceDate.getText();
				itmPrice.setText(columnIdx++, strDate);
				txtPriceDate.dispose();
				setNext(btnPost);
				return true;
			}
		};
	}

	@Override
	protected void setFocus() {
		if (id == 0) {
			txtShortId.setTouchEnabled(true);
			txtShortId.setFocus();
		}
	}

	public Button getBtnPost() {
		return btnPost;
	}

	public void setBtnPost(Button btnPost) {
		this.btnPost = btnPost;
	}

	public Text getTxtId() {
		return txtId;
	}

	public void setTxtId(Text txtId) {
		this.txtId = txtId;
	}

	public Text getTxtShortId() {
		return txtShortId;
	}

	public void setTxtShortId(Text txtSmsId) {
		this.txtShortId = txtSmsId;
	}

	public Combo getCmbType() {
		return cmbType;
	}

	public void setCmbType(Combo cmbType) {
		this.cmbType = cmbType;
	}

	public Button getBtnBom() {
		return btnBom;
	}

	public void setBtnBom(Button btnBom) {
		this.btnBom = btnBom;
	}

	public Text getTxtName() {
		return txtName;
	}

	public void setTxtName(Text txtName) {
		this.txtName = txtName;
	}

	public Text getTxtUnspscId() {
		return txtUnspscId;
	}

	public void setTxtUnspscId(Text unspscId) {
		this.txtUnspscId = unspscId;
	}

	public Button getBtnDiscount() {
		return btnDiscount;
	}

	public void setBtnDiscount(Button btnDiscount) {
		this.btnDiscount = btnDiscount;
	}

	public Combo getCmbProductLine() {
		return cmbProductLine;
	}

	public void setCmbProductLine(Combo cmbProductLine) {
		this.cmbProductLine = cmbProductLine;
	}

	public Table getTblUom() {
		return tblUom;
	}

	public Table getTblPrice() {
		return tblPrice;
	}

	public Table getTblDiscount() {
		return tblDiscount;
	}

	// Main method
	public static void main(String[] args) {
		// Database.getInstance().getConnection("irene", "ayin");
		Database.getInstance().getConnection("sheryl", "10-8-91");
		Login.setGroup("super_supply");
		new ItemView(328);
		Database.getInstance().closeConnection();
	}
}

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
	private int id, rowIdx, colIdx;
	private boolean bought, sold, reported, isNoEntry;
	private final String BUY = "PURCHASED";
	private ItemHelper helper;
	private ItemMaster im;
	private Button btnPost, btnBom, btnDiscount, btnUom;
	private Combo cmbType, cmbProductLine, cmbUom, cmbDiscountUom, cmbChannel;
	private Text txtId, txtShortId, txtName, txtUnspscId, txtQty, txtPrice, txtPriceDate;
	private Text txtDiscount, txtVolume, txtDiscountDate;
	private Table tblUom, tblPrice, tblDiscount;
	private TableItem tblItmUom, tblItmPrice, tblItmDiscount;
	private String type, discountUom;
	private ArrayList<String> usedUoms; 

	public ItemView(int id) {
		super();
		this.id = id;
		bought = false;
		sold = false;
		reported = false;
		isNoEntry = true;
		usedUoms = new ArrayList<>();
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
		report = im = new ItemMaster(id);
	}
	@Override
	protected void setTitleBar() {
		btnPost = new MasterTitleBar(this, report).getBtnPost();
	}
	@Override
	protected void setHeader() {
		Group header = new Group(shell, SWT.NONE);
		header.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, false,2, 1));
		header.setText("DETAILS");
		header.setLayout(new GridLayout(7, false));

		int id = im.getId();
		long unspscId = im.getUnspscId();
		String smsId = im.getShortId();
		String name = im.getName();
		// item ID display
		txtId = new DataDisplay(header, "ITEM CODE #", id).getText();
		// 16 alphanumeric short item name input
		txtShortId = new DataEntry(header, "NAME", smsId, 1, 16).getText();
		// Item type selector
		cmbType = new DataSelection(header, im.getTypes(), "TYPE", im.getType()).getCombo();
		// BOM dialog launcher button
		btnBom = new BomButton(header, im).getButton();
		btnBom.setEnabled(type != null ? (type.equals("MADE") ? true : false) : false);
		// 52 alphanumeric item description input
		txtName = new DataEntry(header, "DESCRIPTION", name, 6, 52).getText();
		// UN SPSC input
		txtUnspscId = new DataEntry(header, "UNSPSC ID #", unspscId).getText();
		// Discounted or not toggle
		btnDiscount = new Button(header, SWT.CHECK);
		btnDiscount.setText("DISCOUNT EXEMPT");
		btnDiscount.setFont(View.monoFont());
		btnDiscount.setLayoutData(
				new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
		btnDiscount.setSelection(im.isNotDiscounted());
		// Product line selector
		cmbProductLine = new DataSelection(
				header, 
				im.getProductLines(), 
				"LINE", 
				im.getProductLine()
				).getCombo();
	}
	@Override
	protected void setTableBar() {
		// UOM
		Group grpUom = new Group(shell, SWT.NONE);
		grpUom.setText("QTY PER UOM RELATIVE TO PK");
		grpUom.setLayoutData(
				new GridData(SWT.CENTER, SWT.BEGINNING, true, false, 1, 1));
		grpUom.setLayout(new GridLayout(1, false));
		Object[][] uomData = im.getUomData();
		String[][] uomHeaders = im.getUomHeaders();
		tblUom = new ReportTable(grpUom, uomData, uomHeaders, "", 80, true){
			@Override
			protected void doubleClickListener() {
				// Disable
			}
		}.getTable();

		// DISCOUNT
		Group grpDiscount = new Group(shell, SWT.NONE);
		grpDiscount.setText("VOLUME DISCOUNT");
		grpDiscount.setLayoutData(
				new GridData(SWT.CENTER, SWT.BEGINNING, true, false, 1, 1));
		grpDiscount.setLayout(new GridLayout(1, false));
		String[][] discountHeaders = im.getDiscountHeaders();
		Object[][] discounts = im.getDiscountData();
		tblDiscount = new ReportTable(
				grpDiscount, discounts, discountHeaders, "", 60, true) {
			@Override
			protected void doubleClickListener() {
				// Disable
			}
		}.getTable();

		// PRICE
		Group grpPrice = new Group(shell, SWT.NONE);
		grpPrice.setText("PRICE PER PK");
		grpPrice.setLayoutData(
				new GridData(SWT.CENTER, SWT.BEGINNING, true, false, 1, 1));
		grpPrice.setLayout(new GridLayout(1, false));
		String[][] priceHeaders = im.getPriceHeaders();
		Object[][] prices = im.getPriceData();
		tblPrice = new ReportTable(grpPrice, prices, priceHeaders, "", 40, true){
			@Override
			protected void doubleClickListener() {
				// Disable
			}			
		}.getTable();
	}
	@Override
	protected void setListener() {
		helper = new ItemHelper();

		// Short name input
		new DataInput(txtShortId, cmbType) {
			@Override
			protected boolean act() {
				String shortId = txtShortId.getText().trim();
				if(shortId.isEmpty())
					return false;
				if (helper.getId(shortId) != 0) {
					new ErrorDialog(
							shortId + " has been used;\n" +
							"try another.");
					return false;
				} 
				return true;
			}
		};

		// Item type selection
		new DataSelector(cmbType, txtName) {
			@Override
			protected void act() {
				type = cmbType.getText();
				switch (type) {
					case "REPACKED":
					case "BUNDLED":
					case "MADE":
						ArrayList<BOM> bomList = new BomView(0).getBomList();
						if(bomList.isEmpty()) break;
						im.setBomList(bomList);
					default:
						break;
				}
			}
		};

		// Item name input
		new DataInput(txtName, txtUnspscId) {
			@Override
			protected boolean act() {
				String name = txtName.getText().trim();
				if(name.isEmpty())
					return false;
				if (helper.getShortId(name) !=  null) {
					new ErrorDialog(
							name + " has been used;\n" +
							"try another.");
					return false;					
				}
				return true;
			}			
		};

		// Item UNSPSC ID input
		new DataInput(txtUnspscId, btnDiscount) {
			@Override
			protected boolean act() {
				String unspsc = txtUnspscId.getText().trim();

				if(unspsc.isEmpty()) {
					if (type.equals(BUY)) {
						new ErrorDialog("" +
								"Purchased items must have\n" +
								"corresponding UNSPSC numbers");
						return false;
					}
				} else {
					if (unspsc.length() != 13) {
						new ErrorDialog("" +
								"UNSPSC must be 13 digits long;\n" +
								"try again.");
						return false;					
					}
					if (helper.getId(Long.parseLong(unspsc)) != 0) {
						new ErrorDialog("" +
								unspsc + " has been used;\n" +
								"try another.");
						return false;					
					}
				}
				btnDiscount.setBackground(View.yellow());
				return true;
			}						
		};

		// Item discount exemption selection
		new DataSwitcher(btnDiscount, cmbProductLine);

		// Item product line selection
		new DataSelector(cmbProductLine, btnUom) {
			@Override
			protected void act() {
				cmbProductLine.setEnabled(false);
				tblItmUom = new TableItem(tblUom, SWT.NONE, rowIdx);
				tblItmUom.setText(0, "1");
				tblItmUom.setText(1, "1.0000");
				tblItmUom.setText(2, "PK");
				usedUoms.add("PK");
				colIdx = type.equals(BUY) ? 3 : 4;
				setUomButton();
				setNext(btnUom);
			}
		};
	}
	// Item UOM buy/sell/report toggle selector
	private void setUomButton() {
		btnUom = new TableCheckButton(tblItmUom, rowIdx, colIdx).getButton();
		btnUom.setBackground(View.yellow());
		btnUom.setFocus();
		new DataSwitcher(btnUom, txtQty) {
			@Override
			protected void act() {
				boolean bool = btnUom.getSelection(); 
				String ok = "";
				if(bool) {
					if (!(colIdx == 5 && reported || colIdx == 3 && bought)) 
						ok = "OK";
					if (colIdx == 3) bought = true;
					if (colIdx == 4) sold = true;
					if (colIdx == 5) reported = true;
					isNoEntry = false;
				}
				tblItmUom.setText(colIdx, ok);
				btnUom.dispose();
				// At EOL, create new, else next control
				if ((colIdx == 4 && reported) || colIdx == 5) {
					colIdx = 0;
					if(rowIdx != 0 && isNoEntry) {
						usedUoms.remove(tblItmUom.getText(2));
						tblItmUom.dispose();
						rowIdx--;
					}
					isNoEntry = true;
					tblItmUom = new TableItem(tblUom, SWT.NONE, ++rowIdx);
					tblItmUom.setText(colIdx, "" + (rowIdx + 1));
					if(rowIdx > 2) tblUom.setTopIndex(rowIdx - 2);
					setQtyInput();
					setNext(txtQty);
					return;
				} 
				++colIdx;
				setUomButton();
				setNext(btnUom);
			}
		};
	}
	// Item quantity per UOM input listener
	private void setQtyInput() {
		txtQty = new TableInput(tblItmUom, rowIdx, ++colIdx, BigDecimal.ZERO).getText();
		txtQty.setTouchEnabled(true);
		txtQty.setFocus();
		new DataInput(txtQty, cmbUom){
			@Override
			protected boolean act() {
				String strQty = txtQty.getText().trim();
				if(strQty.isEmpty()) {
					if(rowIdx > 0 && 
							(!type.equals(BUY) || (type.equals(BUY) && bought))) {
						txtQty.dispose();
						tblItmUom.dispose();
						colIdx = 0;
						rowIdx = 0;
						if(sold) {
							tblItmDiscount = new TableItem(
									tblDiscount, SWT.NONE, rowIdx);
							tblItmDiscount.setText(colIdx++, "" + (rowIdx + 1));
							setDiscountInput();
							setNext(txtDiscount);
						} else {
							if(bought) {
								tblItmPrice = new TableItem(tblPrice, SWT.NONE, rowIdx);
								tblItmPrice.setText(colIdx++, "" + (rowIdx + 1));
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
				if(qty.compareTo(BigDecimal.ZERO) <= 0) 
					return false;
				tblItmUom.setText(colIdx++, DIS.XNF.format(qty));
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
		if(uoms.length == 0) {
			if(type.equals(BUY) && !bought) {
				new ErrorDialog("" +
						"Purchased item must have a buying UOM;\n" +
						"try again from the top.");
				shell.dispose();
				new ItemView(0);
				return;
			}
			tblItmUom.dispose();
			rowIdx = 0;
			colIdx = 0;
			tblItmPrice = new TableItem(tblPrice, SWT.NONE, rowIdx);
			tblItmPrice.setText(colIdx++, "" + (rowIdx + 1));
			setPriceInput();
			txtPrice.setTouchEnabled(true);
			txtPrice.setFocus();
			return;
		}
		cmbUom = new TableSelection(tblItmUom, rowIdx, colIdx, uoms, null).getCombo();
		cmbUom.setEnabled(true);
		cmbUom.setFocus();
		new DataSelector(cmbUom, btnUom) {
			@Override
			protected void act() {
				String uom = cmbUom.getText();
				tblItmUom.setText(colIdx, uom);
				usedUoms.add(uom);
				cmbUom.dispose();
				colIdx = type.equals(BUY) ? 3 : 4;
				setUomButton();
				setNext(btnUom);
			}
		};
	}
	// Item volume discount amount input
	private void setDiscountInput() {
		txtDiscount = new TableInput(
				tblItmDiscount, rowIdx, colIdx, BigDecimal.ZERO).getText();
		txtDiscount.setTouchEnabled(true);
		txtDiscount.setFocus();
		new DataInput(txtDiscount, txtVolume){
			@Override
			protected boolean act() {
				String strDiscount = txtDiscount.getText().trim();
				BigDecimal discount = BigDecimal.ZERO;
				if(strDiscount.isEmpty()) {
					txtDiscount.dispose();
					tblItmDiscount.dispose();
					rowIdx = 0;
					colIdx = 0;
					tblItmPrice = new TableItem(tblPrice, SWT.NONE, rowIdx);
					tblItmPrice.setText(colIdx++, "" + (rowIdx + 1));
					setPriceInput();
					setNext(txtPrice);
					return true;
				} else {
					discount = new BigDecimal(strDiscount);
					if(discount.compareTo(BigDecimal.ZERO) <= 0)
						return false;
					strDiscount = DIS.SNF.format(discount);
				}
				tblItmDiscount.setText(colIdx++, strDiscount);
				txtDiscount.dispose();
				setVolumeInput();
				setNext(txtVolume);
				return true;
			}
		};
	}
	// Item volume discount quantity cut-off
	private void setVolumeInput() {
		txtVolume = new TableInput(tblItmDiscount, rowIdx, colIdx, 0).getText();
		txtVolume.setTouchEnabled(true);
		txtVolume.setFocus();
		new DataInput(txtVolume, cmbDiscountUom){
			@Override
			protected boolean act() {
				String strVolume = txtVolume.getText().trim();
				if(strVolume.isEmpty()) return false;	
				if(Integer.parseInt(strVolume) <= 0) return false;
				tblItmDiscount.setText(colIdx++, strVolume);
				txtVolume.dispose();
				if(discountUom == null) {
					setDiscountUomCombo();
					setNext(cmbDiscountUom);
				} else {
					tblItmDiscount.setText(colIdx++, discountUom);
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
		cmbDiscountUom = new TableSelection(tblItmDiscount, rowIdx, colIdx, uoms, null).getCombo();
		cmbDiscountUom.setEnabled(true);
		cmbDiscountUom.setFocus();
		new DataSelector(cmbDiscountUom, cmbChannel) {
			@Override
			protected void act() {
				discountUom = cmbDiscountUom.getText();
				tblItmDiscount.setText(colIdx++, discountUom);
				cmbDiscountUom.dispose();
				setChannelCombo();
				setNext(cmbChannel);
			}
		};
	}
	// Item volume discount applicable channel
	private void setChannelCombo() {
		cmbChannel = new TableSelection(
				tblItmDiscount, rowIdx, colIdx, new Channel().getChannels(), null).getCombo();
		cmbChannel.setEnabled(true);
		cmbChannel.setFocus();
		new DataSelector(cmbChannel, txtDiscountDate) {
			@Override
			protected void act() {
				String channel = cmbChannel.getText();
				tblItmDiscount.setText(colIdx++, channel);
				cmbChannel.dispose();
				setDiscountDateInput();
				setNext(txtDiscountDate);
			}
		};
	}
	// Item volume discount start date input
	private void setDiscountDateInput() {
		final DateAdder today = new DateAdder();
		txtDiscountDate = new TableInput(
				tblItmDiscount, rowIdx, colIdx, today.plus(1)).getText();
		txtDiscountDate.setTouchEnabled(true);
		txtDiscountDate.setFocus();
		new DataInput(txtDiscountDate, txtDiscount, today.add(1)){
			@Override
			protected boolean act() {
				String strDate = txtDiscountDate.getText();
				tblItmDiscount.setText(colIdx++, strDate);
				txtDiscountDate.dispose();
				colIdx = 0;
				tblItmDiscount = new TableItem(tblDiscount, SWT.NONE, ++rowIdx);
				if(rowIdx > 1) tblDiscount.setTopIndex(rowIdx - 1);
				tblItmDiscount.setText(colIdx++, "" + (rowIdx + 1));
				setDiscountInput();
				setNext(txtDiscount);
				return true;
			}
		};
	}
	// Item price input
	private void setPriceInput() {
		txtPrice = new TableInput(
				tblItmPrice, rowIdx, colIdx, BigDecimal.ZERO).getText();
		txtPrice.setTouchEnabled(true);
		txtPrice.setFocus();
		new DataInput(txtPrice, txtPrice){
			@Override
			protected boolean act() {
				String strPrice = txtPrice.getText().trim();
				BigDecimal price = BigDecimal.ZERO;
				if(strPrice.isEmpty()) {
					if((colIdx == 1 && type.equals(BUY)) || (colIdx == 2 && sold)) {
						return false;						
					}
					if(colIdx == 1 && rowIdx > 0 && bought && reported) {
						txtPrice.dispose();
						setNext(btnPost);
					}
				} else {
					price = new BigDecimal(strPrice);
					if(price.compareTo(BigDecimal.ZERO) <= 0)
						return false;
					strPrice = DIS.SNF.format(price);
				}
				tblItmPrice.setText(colIdx, strPrice);
				txtPrice.dispose();
				if(colIdx == 5 || !sold) {
					colIdx = 6;
					setPriceDateInput();
					setNext(txtPriceDate);			
				} else {
					colIdx++;
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
		txtPriceDate = new TableInput(
				tblItmPrice, rowIdx, colIdx, today.plus(1)).getText();
		txtPriceDate.setTouchEnabled(true);
		txtPriceDate.setFocus();
		new DataInput(txtPriceDate, btnPost, today.add(1)){
			@Override
			protected boolean act() {
				String strDate = txtPriceDate.getText();
				tblItmPrice.setText(colIdx++, strDate);
				txtPriceDate.dispose();
				setNext(btnPost);
				return true;
			}
		};
	}
	// Initial focused widget
	@Override
	protected void setFocus() {
		if (id == 0) {
			txtShortId.setTouchEnabled(true);
			txtShortId.setFocus();
		} 
	}
	// Getters and setters
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
		Database.getInstance().getConnection("irene","ayin");
		new ItemView(328);
		Database.getInstance().closeConnection();
	}
}

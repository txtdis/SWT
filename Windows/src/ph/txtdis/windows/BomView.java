package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class BomView extends ReportView {
	private int itemId, rowIdx, childId, uomId;
	private ArrayList<BOM> bomList;
	private ArrayList<Integer> childIdList;
	private Text txtChildId, txtQty;
	private Button btnItemId, btnReturn;
	private Combo cmbUom;
	private TableItem tableItem;
	private ItemMaster im;
	
	public BomView(ItemMaster im) {
		super();
		this.im = im;
		itemId = im.getId();
		bomList = im.getBomList();
		childIdList = new ArrayList<>();
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
		report = new BomList(im);
	}

	@Override
	protected void setTitleBar() {
		new ListTitleBar(this, report) {
			@Override
			protected void layButtons() {
				btnReturn = new ReportButton(buttons, report, "Back", "Return to Item Data") {

					@Override
					protected void doWhenSelected() {
						TableItem[] tableItems = table.getItems();
						for (int i = 0; i < tableItems.length - 1; i++) {
							// [1]item_id, [3]uom, [4]qty
							TableItem tableItem = tableItems[i];
							int itemId = Integer.parseInt(tableItem.getText(1));
							int uom = new UOM(tableItem.getText(3)).getId();
							BigDecimal qty = new BigDecimal(tableItem.getText(4));
							bomList.add(new BOM(itemId, uom, qty));
						}
						parent.getShell().dispose();
					}
				}.getButton();
				btnReturn.setEnabled(false);
			}
		};
	}

	@Override
	protected void setHeader() {
		new ReportHeaderBar(shell, report);
	}

	@Override
	protected void setFocus() {
		if (itemId == 0)
			setChildId();
	}
	
	private void setChildId() {
		tableItem = getTableItem(rowIdx);
		tableItem.setText(0, String.valueOf(rowIdx + 1));
		btnItemId = new TableButton(tableItem, rowIdx, 0, "Item List").getButton();
		txtChildId = new TableTextInput(tableItem, rowIdx, 1, 0).getText();		
		new TextInputter(txtChildId, cmbUom) {
			
			@Override
            protected boolean isABlankInputNotValid() {
	            return super.isABlankInputNotValid();
            }

			@Override
			protected boolean isThePositiveNumberValid() {
				childId = numericInput.intValue();
				if (childIdList.contains(childId)) {
					new ErrorDialog("Item ID " + childId + "\nis already on the list");
					return false;
				}
				String name = new ItemHelper().getName(childId);
				if (name == null) {
					new ErrorDialog("Item ID " + childId + "\nis not in our system");
					return false;
				}
				tableItem.setText(1, textInput);
				tableItem.setText(2, name);
				btnItemId.dispose();
				txtChildId.dispose();
				setUomCombo();
				return true;
			}
		};
		txtChildId.setFocus();
	}

	private void setUomCombo() {
		cmbUom = new TableCombo(tableItem, 3, new UOM().getUoms(childId)).getCombo();
		new ComboSelector(cmbUom, txtQty) {
			@Override
			protected void doAfterSelection() {
				uomId = new UOM(selection).getId();
				tableItem.setText(3, selection);
				cmbUom.dispose();
				setQtyListener();
			}
		};
		cmbUom.setFocus();
	}

	private void setQtyListener() {
		txtQty = new TableTextInput(tableItem, rowIdx, 4, BigDecimal.ZERO).getText();
		new TextInputter(txtQty, txtChildId) {
			@Override
			protected boolean isThePositiveNumberValid() {
				tableItem.setText(4, textInput);
				txtQty.dispose();
				childIdList.add(childId);
				bomList.add(new BOM(childId, uomId, numericInput));
				
				if(++rowIdx >= 2)
					btnReturn.setEnabled(true);
				setChildId();
				return true;
			}
		};
		txtQty.setFocus();
	}

	public ArrayList<BOM> getBomList() {
		return bomList;
	}
}

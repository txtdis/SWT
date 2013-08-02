package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class BomView extends ReportView {
	private int itemId, rowIdx, childId;
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
		if (itemId == 0) {
			setChildId();
			txtChildId.setTouchEnabled(true);
			txtChildId.setFocus();
			setChildIdListener();
		}
	}
	
	private void setChildId() {
		tableItem = getTableItem(rowIdx);
		tableItem.setText(0, String.valueOf(rowIdx + 1));
		btnItemId = new TableButton(tableItem, rowIdx, 0, "Item List").getButton();
		txtChildId = new TableInput(tableItem, rowIdx, 1, 0).getText();		
	}

	private void setChildIdListener() {
		new DataInput(txtChildId, cmbUom) {
			@Override
			protected boolean isInputValid() {
				String strChildId = txtChildId.getText().trim();
				if (strChildId.isEmpty())
					if (rowIdx > 0) {
						txtChildId.dispose();
						setNext(btnReturn);
						return true;
					} else {
						return false;
					}
				childId = Integer.parseInt(strChildId);
				if (childId <= 0)
					return false;
				
				if (childIdList.contains(childId)) {
					new ErrorDialog("Item ID " + childId + "\nis already on the list");
					return false;
				}
				String name = new ItemHelper().getName(childId);
				if (name == null) {
					new ErrorDialog("Item ID " + childId + "\nis not in our system");
					return false;
				}
				
				tableItem.setText(1, strChildId);
				tableItem.setText(2, name);
				txtChildId.dispose();
				btnItemId.dispose();
				String[] uoms = new UOM().getUoms(childId);
				cmbUom = new TableSelection(tableItem, rowIdx, 3, uoms, null).getCombo();
				setNext(cmbUom);
				setUomListener();
				return true;
			}
		};
	}

	private void setUomListener() {
		// Child item UOM selection
		new DataSelector(cmbUom, txtQty) {
			@Override
			protected void doWhenSelected() {
				tableItem.setText(3, cmbUom.getText());
				cmbUom.dispose();
				txtQty = new TableInput(tableItem, rowIdx, 4, BigDecimal.ZERO).getText();
				setNext(txtQty);
				setQtyListener();
			}
		};
	}

	private void setQtyListener() {
		// Child item quantity input listener
		new DataInput(txtQty, txtChildId) {
			@Override
			protected boolean isInputValid() {
				String strQty = txtQty.getText().trim();
				if (strQty.isEmpty())
					return false;
				BigDecimal qty = new BigDecimal(strQty);
				if (qty.compareTo(BigDecimal.ZERO) <= 0)
					return false;
				tableItem.setText(4, strQty);
				txtQty.dispose();
				childIdList.add(childId);
				++rowIdx;
				setChildId();
				setNext(txtChildId);
				setChildIdListener();
				return true;
			}
		};
	}

	public ArrayList<BOM> getBomList() {
		return bomList;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene", "ayin");
		new BomView(new ItemMaster(0));
		Database.getInstance().closeConnection();
	}
}

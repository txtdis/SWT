package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class BomView extends ReportView {
	private int itemId, rowIdx;
	private ArrayList<BOM> bomList;
	private ArrayList<Integer> childIdList;
	private Text txtChildId, txtQty;
	private Button btnItemId, btnReturn;
	private Combo cmbUom;
	private TableItem tableItem;

	public BomView(int itemId) {
		this.itemId = itemId;
		bomList = new ArrayList<>();
		childIdList = new ArrayList<>();
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
		report = new BomList(itemId);
	}

	@Override
	protected void setTitleBar() {
		new ListTitleBar(this, report){
			@Override
			protected void layButtons() {
				btnReturn = new ReportButton(buttons, report, "Back", "Return to Item Data"){

					@Override
					protected void open(){
						TableItem[] tableItem = table.getItems(); 
						for (int i = 0; i < tableItem.length-1; i++) {							
							//[1]item_id, [3]uom, [4]qty
							int itemId = Integer.parseInt(tableItem[i].getText(1));
							int uom = new UOM(tableItem[i].getText(3)).getId();
							BigDecimal qty = new BigDecimal(tableItem[i].getText(4));
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
	protected void setTableBar() {
		super.setTableBar();
		setTableItem(0);
	}
	@Override
	protected void setFocus() {
		if (itemId != 0) return;
		txtChildId.setTouchEnabled(true);
		txtChildId.setFocus();
	}

	private void setTableItem(int rowIdx) {
		tableItem = new TableItem(table, SWT.NONE);
		tableItem.setText(0, "" + (rowIdx + 1));
		btnItemId = new TableButton(tableItem, rowIdx, 0, "Item List").getButton();
		txtChildId = new TableInput(tableItem, rowIdx, 1, 0).getText();
		setChildIdListener();
	}
	
	private void setChildIdListener() {
		// Child item ID input listener
		new DataInput(txtChildId, cmbUom){
			@Override
			protected boolean act() {
				String strChildId = txtChildId.getText().trim();
				if(strChildId.isEmpty()) 
					if(rowIdx > 0) {
						txtChildId.dispose();
						setNext(btnReturn);
						return true;
					} else {
						return false;
					}
				int childId = Integer.parseInt(strChildId);
				if(childId <= 0) 
					return false;
				if(childIdList.contains(childId)) {
					new ErrorDialog("Item ID " + childId + "\nis already on the list");
					return false;					
				}
				String name = new ItemHelper().getName(childId);
				if(name == null) {
					new ErrorDialog("Item ID " + childId + "\nis not in our system");
					return false;
				}
				childIdList.add(childId);
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
		new DataSelector(cmbUom, txtQty){
			@Override
			protected void act() {
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
		new DataInput(txtQty, txtChildId){
			@Override
			protected boolean act() {
				String strQty = txtQty.getText().trim();
				if(strQty.isEmpty()) 
					return false;
				BigDecimal qty = new BigDecimal(strQty);
				if(qty.compareTo(BigDecimal.ZERO) <= 0) 
					return false;
				tableItem.setText(4, strQty);
				txtQty.dispose();
				setTableItem(++rowIdx);
				setNext(txtChildId);
				return true;
			}
		};		
	}

	public ArrayList<BOM> getBomList() {
		return bomList;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		ArrayList<BOM> bm = new BomView(0).getBomList();
		for (BOM bom : bm) {
			System.out.println(bom.getItemId());
			System.out.println(bom.getUom());
			System.out.println(bom.getQty());			
		}
		Database.getInstance().closeConnection();
	}
}

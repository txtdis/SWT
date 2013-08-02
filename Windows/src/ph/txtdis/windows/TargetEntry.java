package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class TargetEntry extends TableDataInput {
	private TableItem tableItem;
	private Button btnPost, btnOutletId;
	private Program program;

	public TargetEntry(Text txtOutlet, int colIdx, int rowIdx, Text txtTarget,
			Table tblTarget, Button btnPost, Table tblRebate, Program program) {
		super(txtOutlet, colIdx, rowIdx, txtTarget, tblTarget, btnPost, tblRebate, 
				program);
		tableItem = table.getItem(rowIdx);
		tableItem.setText(0, "" + (rowIdx + 1));
		btnOutletId = new TableButton(
				tableItem, rowIdx, 0, "Customer List").getButton();
		this.btnPost = btnPost;
		this.program = program;
	}

	@Override
	protected boolean isInputValid() {
		String string = text.getText().trim();
		int outletId;
		if(string.isEmpty()) { 
			if(rowIdx > 0) {
				text.dispose();
				setNext(btnPost);
				return true;
			} 
			return false;
		}
		try {
			outletId = Integer.parseInt(string);
		} catch (NumberFormatException e) {
			new ErrorDialog(e);
			return false;
		}
		if(outletId <= 0)
			return false;
		String name = new CustomerHelper(outletId).getName();
		if(name == null) {
			new ErrorDialog("" +
					"Customer ID" + outletId + "\n" +
					"is not in our system."
					);
			return false;
		}
		ArrayList<Integer> outletList = program.getOutletList();
		if(outletList.contains(outletId)) {
			new ErrorDialog("" +
					"Customer ID" + outletId + "\n" +
					"is already on the list."
					);
			return false;			
		}
		outletList.add(outletId);
		tableItem.setText(1, "" + outletId);
		tableItem.setText(2, name);
		btnOutletId.dispose();
		text.dispose();
		for (int i = 3; i < table.getColumnCount(); i++) {
			if(!nxtTbl.getItem(0).getText(i).isEmpty()) {
				colIdx = i;
				break;
			}
		}
		Text txtTarget = new TableInput(
				tableItem, rowIdx, colIdx, BigDecimal.ZERO).getText();
		setNext(txtTarget);
		new TableDataInput(txtTarget, colIdx, rowIdx, txtTarget, table, option, 
				nxtTbl, program);
		return true;
	}
}

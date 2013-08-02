package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;


public class TableDataInput extends DataInput {
	protected Text text;
	protected Control newNext, oldNext, option;
	protected int colIdx, rowIdx;
	protected Table table, nxtTbl;
	protected Report report;
	
	private int colCnt;
	private String tip;
	private TableItem tableItem;
	
	public TableDataInput(Text text, int colIdx, int rowIdx, Control next, Table table, 
			Control option, Table nextTable, Report report) {
		super(text, next);
		this.text = text;
		this.colIdx = colIdx;
		this.rowIdx = rowIdx;
		newNext = next;
		oldNext = next;
		this.table = table;
		this.option = option;
		this.nxtTbl = nextTable;
		this.report = report;
	}

	@Override
	protected boolean isInputValid() {
		String string = ((Text) text).getText().trim();
		tableItem = table.getItem(rowIdx);
		tableItem.setText(colIdx, string);
		text.dispose();
		colCnt = table.getColumnCount() - 1;
		tip = table.getToolTipText();
		if (rowIdx > 8) table.setTopIndex(rowIdx - 9);
		if (colIdx == colCnt) {
			return setNextLine();
		} else {
			if(tip.contains("target")){
				for (int i = colIdx+1; i < colCnt+1; i++) {
					if(!nxtTbl.getItem(0).getText(i).isEmpty()) {
						colIdx = i;
						break;
					} 
					if (i == colCnt) {
						return setNextLine();
					}
				}
			} else {
				++colIdx;
			}
			text = new TableInput(tableItem, rowIdx, colIdx, BigDecimal.ZERO).getText();
			newNext = text;
			setNext(newNext);
			new TableDataInput(text, colIdx, rowIdx, oldNext, table, option, 
					nxtTbl, report);
		}
		return true;
	}
	
	private boolean setNextLine() {
		boolean hasDatum = false;
		for (int i = 3; i < (colCnt + 1); i++) {
			if(!tableItem.getText(i).isEmpty()){
				hasDatum = true;
				break;
			}
		}
		if(!hasDatum) {
			new ErrorDialog("" +
					"At least one column must have data\n" +
					"Restart from the top");
			table.getShell().dispose();
			new ProgramView(0);
			return false;
		}
		if(tip.contains("rebate")) {
			rowIdx = 0;
			tableItem = nxtTbl.getItem(rowIdx);
			Table temp = table;
			table = nxtTbl;
			nxtTbl = temp;
		} else {
			tableItem = new TableItem(table, SWT.NONE, ++rowIdx);
		}
		Text txtOutlet = new TableInput(tableItem, rowIdx, 1, 0).getText();
		setNext(txtOutlet);
		new TargetEntry(txtOutlet, 1, rowIdx, (Text) text, table, 
				(Button) option, nxtTbl, (Program) report);
		return true;
	}
}

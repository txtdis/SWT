package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public abstract class InputView extends PostView implements Openable {
	
	protected Button listButton;
	protected InputData data;
	protected Text idDisplay;
	protected int id;

	public InputView() {
    }

	public InputView(InputData data) {
		super(data);
		this.data = data;
		id = data.getId();
	}

	protected void display() {
		super.display();
		if (id == 0)
			addListener();
		setFocus();
    }

	@Override
	public void open() {
		new OpenDialog(type, shell);
	}

	protected abstract void setFocus();
	protected abstract void addListener();
	protected abstract void addSubheader();

	public void createTableItem() {
		tableItem = new TableItem(table, SWT.NONE);
		rowIdx = table.indexOf(tableItem);
		tableItem.setBackground(rowIdx % 2 == 0 ? UI.WHITE : UI.GRAY);
	}

	public TableItem getTableItem(int rowIdx) {
		int itemCount = table.getItemCount();
		if(itemCount < rowIdx) {
			rowIdx = itemCount + 1;
			this.rowIdx = rowIdx;
			createTableItem();
		}
		return table.getItem(rowIdx);
	}

	public Button getListButton() {
		return listButton;
	}

	public void setListButton(Button listButton) {
		this.listButton = listButton;
	}
}

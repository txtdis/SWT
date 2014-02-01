package ph.txtdis.windows;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TableItem;

public abstract class PostView extends ReportView implements Postable {
	protected Button postButton;
	protected TableItem tableItem;
	protected int rowIdx;

	public PostView() {
	}

	public PostView(Data data) {
		super(data);
		this.data = data;
	}
	
	@Override
	public Button getPostButton() {
		return postButton;
	}

	@Override
    public void post() {
		tableItem.dispose();
		Posting posting = getPosting();
		posting.save();
		if (posting.isOK())
			updateView(posting.getId());
		else
			new ErrorDialog("Posting Failed\nTry Again");
	}

	@Override
    public void updateView(int id) {
		shell.close();
		new InfoDialog("Posting Successful");
		DIS.instantiateClass(this, new Object[] {id}, new Class<?>[] {int.class});
    }

	public int getRowIdx() {
		return rowIdx;
	}

	public void setRowIdx(int rowIdx) {
		this.rowIdx = rowIdx;
	}

	public TableItem getTableItem() {
		return tableItem;
	}
	
}

package ph.txtdis.windows;

import org.eclipse.swt.widgets.Button;

public class MasterTitleBar extends ListTitleBar {
	private Button btnNew, btnPost;

	public MasterTitleBar(ReportView view, Order report) {
		super(view, report);
	}

	@Override
	protected void layButtons() {
		btnNew = new NewButton(buttons, module).getButton();
		new OpenButton(buttons, report);
		insertButtons();
		Order order = (Order) report;
		if (report.getId() == 0 || order.isEditable())
			btnPost = new PostButton(buttons, order).getButton();
	}

	protected void insertButtons() {

	}

	public Button getBtnNew() {
		return btnNew;
	}

	public Button getSaveButton() {
		return btnPost;
	}

	protected void setBtnPost(Button btnPost) {
		this.btnPost = btnPost;
	}
}

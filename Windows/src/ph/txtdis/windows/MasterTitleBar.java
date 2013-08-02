package ph.txtdis.windows;

import org.eclipse.swt.widgets.Button;

public class MasterTitleBar extends ListTitleBar {
	private Button btnNew, btnPost;

	public MasterTitleBar(ReportView view, Report report) {
		super(view, report);
	}

	@Override
	protected void layButtons() {
		btnNew = new NewButton(buttons, module).getButton();
		new RetrieveButton(buttons, report);
		insertButtons();
		if (report.getId() == 0)
			btnPost = new PostButton(buttons, reportView, report).getButton();
		new ExitButton(buttons, module);
	}
	
	protected void insertButtons() {
		
	}

	public Button getBtnNew() {
		return btnNew;
	}

	public Button getBtnPost() {
		return btnPost;
	}

	protected void setBtnPost(Button btnPost) {
		this.btnPost = btnPost;
	}
}

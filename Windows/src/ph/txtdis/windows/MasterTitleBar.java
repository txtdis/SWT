package ph.txtdis.windows;

import org.eclipse.swt.widgets.Button;

public class MasterTitleBar extends ListTitleBar {
	private Button btnPost;

	public MasterTitleBar(ReportView view, Report report) {
		super(view, report);
	}

	@Override
	protected void layButtons() {
		new NewButton(buttons, module).getButton();
		new RetrieveButton(buttons, report).getButton();
		if (report.getId() == 0)
			btnPost = new PostButton(buttons, reportView, report).getButton();
		new ExitButton(buttons, module).getButton();
	}

	public Button getBtnPost() {
		return btnPost;
	}

}

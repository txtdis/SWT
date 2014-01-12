package ph.txtdis.windows;

import org.eclipse.swt.widgets.Button;

public class ListTitleBar extends ReportTitleBar {
	private Button addButton;
	
	public ListTitleBar(ReportView view, Report report) {
		super(view, report);
	}
	
	@Override
	protected void layButtons() {
		new SearchButton(buttons, module);
		addButton = new AddButton(buttons, module).getButton();
		new ExcelButton(buttons, report);
	}

	public Button getAddButton() {
		return addButton;
	}
}

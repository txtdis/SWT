package ph.txtdis.windows;

import org.eclipse.swt.widgets.Button;

public abstract class ListView extends ReportView {
	protected String string;
	protected Button addButton;

	public ListView(String string) {
		super();
		this.string = string;
		setProgress();
		setTitleBar();
		setHeader();
		getTable();
		setFooter();
		setListener();
		setFocus();
		showReport();
	}
	@Override
	protected void setTitleBar() {
		addButton = new ListTitleBar(this, report).getAddButton();
	}
}

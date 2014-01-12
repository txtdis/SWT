package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class CustomerEditButton extends ReportButton {

	public CustomerEditButton(Composite parent, Report report) {
		super(parent, report, "Write", "Edit Customer Data");
	}

	@Override
	protected void doWhenSelected() {
		parent.getShell().dispose();
		new CustomerView(report.getId(), true);
	}
}

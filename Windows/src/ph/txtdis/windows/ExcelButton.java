package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class ExcelButton extends ReportButton {
	public ExcelButton(Composite parent, Report report) {
		super(parent, report, "Table", "Save data to Excel");
	}

	@Override
	protected void doWithProgressMonitorWhenSelected() {
		new ExcelWriter(report);
	}
}
package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public abstract class ReportButton extends ImageButton {

	protected Report report;

	public ReportButton(Composite parent, Report report, String icon, String tooltip) {
		super(parent, report.getModule(), icon, tooltip);
		this.report = report;
	}

	@Override
	protected void doWhenSelected() {
		new ProgressDialog() {
			@Override
			public void proceed() {
				doWithProgressMonitorWhenSelected();
			}
		};
	}

	protected void doWithProgressMonitorWhenSelected() {
	};
}
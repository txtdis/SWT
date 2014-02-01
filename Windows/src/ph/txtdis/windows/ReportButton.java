package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public abstract class ReportButton extends ImageButton {

	protected Data data;

	public ReportButton(Composite parent, Data report, String icon, String tooltip) {
		super(parent, report.getType().getName(), icon, tooltip);
		this.data = report;
	}

	@Override
	protected void proceed() {
	}
}
package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class OpenButton extends ReportButton {

	public OpenButton(Composite parent, Report report) {
		super(parent, report, "Open", "Open " + report.getModule() + "#");
	}

	@Override
	protected void doWhenSelected(){
		new OpenDialog(module);
	}
}

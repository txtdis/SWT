package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class RetrieveButton extends ReportButton {

	public RetrieveButton(Composite parent, Report report) {
		super(parent, report, "Retrieve", "Retrieve a Saved " + report.getModule() + 
				(report.getModule().equals("Stock Take") ? " Tag" : ""));
	}

	@Override
	protected void open(){
		new RetrieveDialog(module);
	}
}

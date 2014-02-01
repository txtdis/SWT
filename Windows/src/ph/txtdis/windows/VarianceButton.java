package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.Composite;

public class VarianceButton extends ReportButton {
	private static final int READ_ONLY = 1;
	
	public VarianceButton(Composite parent, Data report) {
		super(parent, report, "Percent", "Correct variance");
	}

	@Override
	protected void proceed() {
		parent.getShell().close();
        Date latest = Count.getLatestDate();
        if(!Count.isClosed(latest)) {
        	new ErrorDialog("Tag " + latest + " as closed\nbefore making any comparisons.");
        	new CountReportView(latest);
        	return;
        }
        new CountVarianceView();
	}
}

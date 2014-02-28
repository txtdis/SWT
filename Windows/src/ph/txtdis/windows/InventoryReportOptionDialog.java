package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;

public class InventoryReportOptionDialog extends DialogView {
	private Button weekly, monthly;
	private Date begin, end = Count.getLatestReconciledDate();;

	public InventoryReportOptionDialog() {
		super(Type.OPTION, "Select\ntype.");
		display();
	}

	@Override
	public void setRightPane() {
		super.setRightPane();
		setTypeCombo();
	}

	private void setTypeCombo() {
		Group group = new Grp(shell, 2, "").getGroup();
		weekly = new Button(group, SWT.RADIO);
		weekly.setText("WEEK-END");
		weekly.setSelection(true);
		monthly = new Button(group, SWT.RADIO);
		monthly.setText("MONTH-END");
	}

	@Override
	protected void setOkButtonAction() {
		if (weekly.getSelection())
			createEndOfWeekReport();
		else
			createEndOfMonthReport();
	}

	private void createEndOfWeekReport() {
	}

	private void createEndOfMonthReport() {

	}
}

package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class RouteOptionDialog extends DialogView {
	private Combo cmbGrp;
	private Label lblGrp;
	private RouteReport report;
	
	public RouteOptionDialog(RouteReport report) {
		super();
		this.report = report;
		setName("Options");
		open();
	}
	
	@Override
	public void setRightPane() {
		Composite cmp = new Composite(header, SWT.NONE);
		cmp.setLayout(new RowLayout(SWT.VERTICAL));
		lblGrp = new Label(cmp, SWT.NONE);
		lblGrp.setText("Grouping");
		cmbGrp = new Combo(cmp, SWT.READ_ONLY);
		cmbGrp.setItems(new RouteHelper().getRoutes());
		cmbGrp.select(0);		
	}

	@Override
	protected void setButton() {
		final Button btnOK = new Button(getFooter(), SWT.PUSH);
		btnOK.setText("OK");
		btnOK.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int grp = cmbGrp.getSelectionIndex();
				for (Shell sh : shell.getDisplay().getShells()) {
					sh.dispose();
				}
				new RouteView(report.getDates(), grp);
			}
		});

		final Button btnCancel = new Button(getFooter(), SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				shell.close();
			}
		});
	}
}

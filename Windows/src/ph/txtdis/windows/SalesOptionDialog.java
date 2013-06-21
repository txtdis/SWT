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

public class SalesOptionDialog extends DialogView {
	private Combo cmbMetric, cmbCat, cmbGrp;
	private Label lblMetric, lblCat, lblGrp;
	private SalesReport report;
	private ItemHelper itemHelper;
	
	public SalesOptionDialog(SalesReport report) {
		super();
		this.report = report;
		setName("Options");
		open();
	}
	
	@Override
	public void setRightPane() {
		Composite cmp = new Composite(header, SWT.NONE);
		cmp.setLayout(new RowLayout(SWT.VERTICAL));
		itemHelper = new ItemHelper();
		lblMetric = new Label(cmp, SWT.NONE);
		lblMetric.setText("Metric");
		cmbMetric = new Combo(cmp, SWT.READ_ONLY);
		cmbMetric.setItems(new String[] {"SALES TO TRADE", "PRODUCTIVE CALLS"});
		cmbMetric.select(0);
		lblCat = new Label(cmp, SWT.NONE);
		lblCat.setText("Product Line");
		cmbCat = new Combo(cmp, SWT.READ_ONLY);
		cmbCat.setItems(itemHelper.getFamilies(2));
		cmbCat.select(0);
		lblGrp = new Label(cmp, SWT.NONE);
		lblGrp.setText("Grouping");
		cmbGrp = new Combo(cmp, SWT.READ_ONLY);
		cmbGrp.setItems(new String[] {"ROUTE", "OUTLET"});
		cmbGrp.select(0);
		
		cmbMetric.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (cmbMetric.getSelectionIndex() == 0) {
					cmbGrp.setEnabled(true);
				} else {
					cmbGrp.select(0);
					cmbGrp.setEnabled(false);
				}
			}
			
		});
	}

	@Override
	protected void setButton() {
		final Button btnOK = new Button(getFooter(), SWT.PUSH);
		btnOK.setText("OK");
		btnOK.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				String metric = cmbMetric.getText();
				int cat = itemHelper.getFamilyId(cmbCat.getText());
				int grp = cmbGrp.getSelectionIndex();
				for (Shell sh : shell.getDisplay().getShells()) {
					sh.dispose();
				}
				new SalesReportView(report.getDates(), metric, cat, grp);
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

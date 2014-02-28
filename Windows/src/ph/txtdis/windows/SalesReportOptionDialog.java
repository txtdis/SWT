package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class SalesReportOptionDialog extends DialogView {
	private Combo metricInput, categoryCombo, groupingCombo;
	private SalesReport report;

	public SalesReportOptionDialog(SalesReport report) {
		super(Type.OPTION, "");
		this.report = report;
		display();
	}

	@Override
	public void setRightPane() {
		Composite option = new Compo(right, 2).getComposite();
		final int CATEGORY = 2;
		metricInput = new ComboBox(option, new String[] {
		        "SALES TO TRADE", "PRODUCTIVE CALLS" }, "METRIC").getCombo();
		categoryCombo = new ComboBox(option, Item.getFamilies(CATEGORY), "PRODUCT LINE").getCombo();
		groupingCombo = new ComboBox(option, new String[] {
		        "ROUTE", "OUTLET" }, "GROUPING").getCombo();

		metricInput.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (metricInput.getSelectionIndex() == 0) {
					groupingCombo.setEnabled(true);
				} else {
					groupingCombo.select(0);
					groupingCombo.setEnabled(false);
				}
			}
		});
	}

	@Override
	protected void setOkButtonAction() {
		String metric = metricInput.getText();
		int categoryId = Item.getFamilyId(categoryCombo.getText());
		boolean isPerRoute = groupingCombo.getText().equals("ROUTE");
		shell.close();
		new SalesReportView(report.getDates(), metric, categoryId, isPerRoute);
	}
}

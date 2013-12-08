package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PurchaseTargetDialog extends DialogView {
	private Text txtUomOrDayCount;
	private Combo cmbBizUnit, uomOrDayBasedCombo;
	private PurchaseOrder order;

	public PurchaseTargetDialog(PurchaseOrder order) {
		super();
		this.order = order;
		setName("Target");
		open();
	}

	@Override
	public void setRightPane() {
		Label historicalDayCount = new Label(right, SWT.CENTER);
		historicalDayCount.setText("First, select business unit;\n"
				+ "then, choose basis for order quantity;\n"
				+ "finally, enter its value.\n");

		ItemHelper itemHelper = new ItemHelper();
		String[] bizUnits = itemHelper.getFamilies(1);
		cmbBizUnit = new Combo(right, SWT.READ_ONLY);
		cmbBizUnit.setItems(bizUnits);
		cmbBizUnit.select(0);

		uomOrDayBasedCombo = new ComboBox(right, new String[] { "NUMBER OF INVENTORY DAYS",
				"NUMBER OF EQUIVALENT UOM" }).getCombo();

		int uomOrDayCount = itemHelper.getMaxStockDays(itemHelper
				.getFamilyId(cmbBizUnit.getText()));
		txtUomOrDayCount = new Text(right, SWT.BORDER | SWT.RIGHT);
		txtUomOrDayCount.setFont(DIS.MONO);
		txtUomOrDayCount.setText(StringUtils.leftPad("" + uomOrDayCount, 6));
		new PositiveIntegerVerifier(txtUomOrDayCount);
	}

	@Override
	protected void setOkButtonAction() {
		if (order.getId() == 0) {
			String bizUnit = cmbBizUnit.getText();
			boolean isDayBased = uomOrDayBasedCombo.getSelectionIndex() == 0 ? true
					: false;
			final int poId = 0;
			int uomOrDayCount = Integer.parseInt(txtUomOrDayCount.getText()
					.trim());
			for (Shell sh : shell.getDisplay().getShells()) {
				sh.dispose();
			}
			new PurchaseOrderView(poId, bizUnit, isDayBased, uomOrDayCount);
		} else {
			shell.dispose();
		}
	}

	@Override
	protected void setListener() {
		cmbBizUnit.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				uomOrDayBasedCombo.setFocus();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				uomOrDayBasedCombo.setFocus();
			}
		});

		uomOrDayBasedCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				txtUomOrDayCount.setFocus();
				txtUomOrDayCount.selectAll();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				txtUomOrDayCount.setFocus();
				txtUomOrDayCount.selectAll();
			}
		});

		txtUomOrDayCount.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				setOkButtonAction();
			}
		});
	}

	@Override
	protected void setFocus() {
		cmbBizUnit.setFocus();
	}
}

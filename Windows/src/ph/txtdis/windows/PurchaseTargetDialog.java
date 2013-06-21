package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PurchaseTargetDialog extends DialogView {
	private Text txtUomOrDayCount;
	private Combo cmbBizUnit, cmbUomOrDayBased;
	private PurchaseOrder order;

	public PurchaseTargetDialog(PurchaseOrder order) {
		super();
		this.order = order;
		setName("Target");
		open();
	}

	@Override
	public void setRightPane() {
		Composite cmp = new Composite(header, SWT.NONE);
		cmp.setLayout(new RowLayout(SWT.VERTICAL));

		Label lblHistoricalDayCount = new Label(cmp, SWT.CENTER);
		lblHistoricalDayCount.setText("" +
				"First, select business unit;\n" +
				"then, choose basis for order quantity;\n" +
				"finally, enter its value.\n");

		ItemHelper itemHelper = new ItemHelper();
		String[] bizUnits = itemHelper.getFamilies(1);
		cmbBizUnit = new Combo(cmp, SWT.READ_ONLY);
		cmbBizUnit.setItems(bizUnits);
		cmbBizUnit.select(0);

		cmbUomOrDayBased = new Combo(cmp, SWT.READ_ONLY);
		cmbUomOrDayBased.setItems(new String[] {
				"NUMBER OF INVENTORY DAYS", 
				"NUMBER OF EQUIVALENT UOM"
		}
				);
		cmbUomOrDayBased.select(0);

		int uomOrDayCount = itemHelper.getMaxStockDays(
				itemHelper.getFamilyId(cmbBizUnit.getText()));
		txtUomOrDayCount = new Text(cmp, SWT.BORDER | SWT.RIGHT);
		txtUomOrDayCount.setFont(View.monoFont());
		txtUomOrDayCount.setText(StringUtils.leftPad("" + uomOrDayCount, 6));
		new PositiveIntegerVerifier(txtUomOrDayCount);
	}

	@Override
	protected void setButton() {
		super.setButton();
		setCancelButton();
	}

	@Override
	protected void setOkButtonAction() {
		if(order.getId() == 0) {
			String bizUnit = cmbBizUnit.getText();
			boolean isDayBased = cmbUomOrDayBased.getSelectionIndex() == 0 ? true : false;
			final int poId = 0;
			int uomOrDayCount = 
					Integer.parseInt(txtUomOrDayCount.getText().trim());
			for (Shell sh : shell.getDisplay().getShells()) {
				sh.dispose();
			}
			new PurchaseOrderView(
					poId,
					bizUnit,
					isDayBased,
					uomOrDayCount
					);
		} else {
			shell.dispose();
		}
	}

	@Override
	protected void setListener() {
		cmbBizUnit.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cmbUomOrDayBased.setFocus();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				cmbUomOrDayBased.setFocus();
			}
		});

		cmbUomOrDayBased.addSelectionListener(new SelectionListener() {
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


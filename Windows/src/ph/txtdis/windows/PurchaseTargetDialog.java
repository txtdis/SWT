package ph.txtdis.windows;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class PurchaseTargetDialog extends DialogView {
	private Text targetInput;
	private Combo bizUnitCombo, targetCombo;

	public PurchaseTargetDialog() {
		super(Type.TARGET, "");
		proceed();
	}

	@Override
	public void setRightPane() {
		
		Composite compo = new Compo(right, 2).getComposite();
		bizUnitCombo = new ComboBox(compo,Item.getFamilies(1), "BIZ UNIT").getCombo();
		targetCombo = new ComboBox(compo, new String[] { "INVENTORY DAYS", "EQUIVALENT UOM" }, "BASIS").getCombo();

		int target = Item.getMaxStockDays(Item.getFamilyId(bizUnitCombo.getText()));
		targetInput = new TextInputBox(compo, "TARGET", target).getText();
	}

	@Override
	protected void setOkButtonAction() {
		String bizUnit = bizUnitCombo.getText();
		int target = Integer.parseInt(targetInput.getText().trim());
		shell.close();
		PurchaseData purchase = new PurchaseData(bizUnit, target);
		if (targetCombo.getSelectionIndex() == 0)
			purchase.computeBasedOnDaysLevel();
		else
			purchase.computeBasedOnVolume();
		new PurchaseView(purchase);
	}

	@Override
	protected void setListener() {
		bizUnitCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				targetCombo.setFocus();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				targetCombo.setFocus();
			}
		});

		targetCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				targetInput.setFocus();
				targetInput.selectAll();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				targetInput.setFocus();
				targetInput.selectAll();
			}
		});

		targetInput.addSelectionListener(new SelectionListener() {
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
		bizUnitCombo.setFocus();
	}
}

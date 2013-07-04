package ph.txtdis.windows;

public class ShippedMaterialBalanceBar extends ListTitleBar {

	public ShippedMaterialBalanceBar(ShippedMaterialBalanceView view, ShippedMaterialBalance report) {
		super(view, report);
		new ExcelButton(buttons, report);
		new ExitButton(buttons, module);
	}
}

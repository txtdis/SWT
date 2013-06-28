package ph.txtdis.windows;

public class ShippingMaterialBalanceBar extends ListTitleBar {

	public ShippingMaterialBalanceBar(ShippingMaterialBalanceView view, ShippingMaterialBalance report) {
		super(view, report);
		new ExcelButton(buttons, report);
		new ExitButton(buttons, module);
	}
}

package ph.txtdis.windows;

public class LoadedMaterialBalanceBar extends ListTitleBar {

	public LoadedMaterialBalanceBar(LoadedMaterialBalanceView view, LoadedMaterialBalance report) {
		super(view, report);
		new ExcelButton(buttons, report);
		new ExitButton(buttons, module);
	}
}

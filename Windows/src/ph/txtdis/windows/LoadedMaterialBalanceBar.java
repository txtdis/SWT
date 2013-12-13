package ph.txtdis.windows;

public class LoadedMaterialBalanceBar extends ListTitleBar {

	public LoadedMaterialBalanceBar(LoadSettlementView view, LoadSettlement report) {
		super(view, report);
		new ExcelButton(buttons, report);
		new ExitButton(buttons, module);
	}
}

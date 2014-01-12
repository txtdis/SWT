package ph.txtdis.windows;

public class LoadSettlementBar extends ListTitleBar {

	public LoadSettlementBar(SettlementView view, LoadSettlement report) {
		super(view, report);
		new ExcelButton(buttons, report);
	}
}

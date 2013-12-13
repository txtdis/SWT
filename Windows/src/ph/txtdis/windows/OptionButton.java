package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class OptionButton extends ReportButton {

	public OptionButton(Composite parent, Report report) {
		super(parent, report, "Options", "Choose Options");
	}
	
	@Override
	public void doWhenSelected(){
		switch (module) {
		case "Sales Report":
			new SalesReportOptionDialog((SalesReport) report);
			break;
		case "Load-In/Out Settlement":
			new SettlementOptionDialog((LoadSettlement) report);
		case "Cash Settlement":
			new SettlementOptionDialog((CashSettlement) report);
			break;
		default:
			new ErrorDialog("No Option for\n" + module + "\nin Option Button");
			break;
		}
	}
}


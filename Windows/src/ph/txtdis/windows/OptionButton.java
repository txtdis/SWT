package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class OptionButton extends ReportButton {

	public OptionButton(Composite parent, Data report) {
		super(parent, report, "Options", "Choose Options");
	}
	
	@Override
	public void proceed(){
		switch (module) {
		case "Sales Report":
			new SalesReportOptionDialog((SalesReport) data);
			break;
		case "Load Settlement":
			new SettlementOptionDialog((LoadSettlement) data);
			break;
		case "Cash Settlement":
			new SettlementOptionDialog((CashSettlement) data);
			break;
		case "Remittance Settlement":
			new SettlementOptionDialog((RemitSettlement) data);
			break;
		default:
			new ErrorDialog("No Option for\n" + module + "\nin Option Button");
			break;
		}
	}
}


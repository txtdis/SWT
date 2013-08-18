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
		case "Loaded Material Balance":
			new LoadedMaterialOptionDialog((LoadedMaterialBalance) report);
			break;
		default:
			new ErrorDialog("No Option for\n" + module + "\nin Option Button");
			break;
		}
	}
}


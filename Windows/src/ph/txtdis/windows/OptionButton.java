package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class OptionButton extends ReportButton {

	public OptionButton(Composite parent, Report report) {
		super(parent, report, "Options", "Choose Options");
	}
	
	@Override
	public void open(){
		switch (module) {
		case "Sales Report":
			new SalesOptionDialog((SalesReport) report);
			break;
		case "Shipped Material Balance":
			new ShippedMaterialOptionDialog((ShippedMaterialBalance) report);
			break;
		default:
			new ErrorDialog("No Option for\n" + module + "\nin Option Button");
			break;
		}
	}
}


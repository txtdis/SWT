package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.widgets.Composite;

public class PrintingButton extends ReportButton {

	public PrintingButton(Composite parent, Data report, boolean enabled) {
		super(parent, report, "Printer", "Print");
		button.setEnabled(enabled);
	}

	@Override
	public void proceed(){
		boolean wasPrinted;
		if(((SalesData) data).getComputedTotal().compareTo(BigDecimal.ZERO) > 0) {
			wasPrinted = new SalesOrderPrinting(data).isPrinted();
		} else {
			wasPrinted = new ReturnedMaterialPrinting(data).isPrinted();
		}
		if(wasPrinted) button.setEnabled(false);
	}
}


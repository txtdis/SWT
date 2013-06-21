package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.widgets.Composite;

public class PrintingButton extends ReportButton {

	public PrintingButton(Composite parent, Report report, boolean enabled) {
		super(parent, report, "Printer", "Print");
		button.setEnabled(enabled);
	}

	@Override
	public void open(){
		boolean wasPrinted;
		if(((SalesOrder) report).getSumTotal().compareTo(BigDecimal.ZERO) > 0) {
			wasPrinted = new SalesOrderPrinting(report).isPrinted();
		} else {
			wasPrinted = new ReturnedMaterialPrinting(report).isPrinted();
		}
		if(wasPrinted) button.setEnabled(false);
	}
}


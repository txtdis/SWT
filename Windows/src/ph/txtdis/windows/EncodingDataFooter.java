package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class EncodingDataFooter {

	public EncodingDataFooter(Composite parent, View view, Data report) {
		OrderData order = (OrderData) report;
		Composite composite = new Compo(parent, 6, SWT.END, SWT.BEGINNING, true, false, 10, 1).getComposite();
		new TextDisplayBox(composite, "ENCODER", order.getInputter(), 1).getText();
		new TextDisplayBox(composite, "DATE", order.getInputDate()).getText();
		new TextDisplayBox(composite, "TIME", order.getInputTime()).getText();
	}
}

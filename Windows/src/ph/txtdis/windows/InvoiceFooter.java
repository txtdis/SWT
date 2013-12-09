package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class InvoiceFooter {
	
	public InvoiceFooter(OrderView view, Order order) {
		Composite data = new Compo(view.getShell(), 10, GridData.HORIZONTAL_ALIGN_CENTER).getComposite();

		String firstLevelDiscount = DIS.TWO_PLACE_DECIMAL.format(order.getFirstLevelDiscountRate());
		TextDisplayBox firstLevelDiscountBox = new TextDisplayBox(data, firstLevelDiscount + "%",
		        order.getFirstLevelDiscountTotal());
		view.setFirstLevelDiscountBox(firstLevelDiscountBox);

		String secondLevelDiscount = DIS.TWO_PLACE_DECIMAL.format(order.getSecondLevelDiscountRate());
		TextDisplayBox secondLevelDicountBox = new TextDisplayBox(data, secondLevelDiscount + "%", order.getSecondLevelDiscountTotal());
		view.setSecondLevelDiscountBox(secondLevelDicountBox);

		view.setTxtTotalVatable(new TextDisplayBox(data, "VATABLE", order.getTotalVatable()).getText());
		view.setTxtTotalVat(new TextDisplayBox(data, "VAT", order.getTotalVat()).getText());
		view.setComputedTotalDisplay(new TextDisplayBox(data, "TOTAL", order.getComputedTotal()).getText());

		Composite cmpEncode = new Compo(data, 6, SWT.END, SWT.BEGINNING, true, false, 10, 1).getComposite();
		view.setInputterDisplay(new TextDisplayBox(cmpEncode, "ENCODER", order.getInputter(), 1).getText());
		view.setInputDateDisplay(new TextDisplayBox(cmpEncode, "DATE", order.getInputDate()).getText());
		view.setInputTimeDisplay(new TextDisplayBox(cmpEncode, "TIME", order.getInputTime()).getText());
	}
}

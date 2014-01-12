package ph.txtdis.windows;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class InvoiceFooter {
	
	public InvoiceFooter(OrderView view, Order order) {
		Composite data = new Compo(view.getShell(), 10, GridData.HORIZONTAL_ALIGN_CENTER).getComposite();

		String firstLevelDiscount = DIS.formatTo2Places(order.getDiscount1Percent());
		TextDisplayBox firstLevelDiscountBox = new TextDisplayBox(data, firstLevelDiscount + "%",
		        order.getDiscount1Total());
		view.setFirstLevelDiscountBox(firstLevelDiscountBox);

		String secondLevelDiscount = DIS.formatTo2Places(order.getDiscount2Percent());
		TextDisplayBox secondLevelDicountBox = new TextDisplayBox(data, secondLevelDiscount + "%", order.getDiscount2Total());
		view.setSecondLevelDiscountBox(secondLevelDicountBox);

		view.setTxtTotalVatable(new TextDisplayBox(data, "VATABLE", order.getTotalVatable()).getText());
		view.setTxtTotalVat(new TextDisplayBox(data, "VAT", order.getTotalVat()).getText());
		view.setComputedTotalDisplay(new TextDisplayBox(data, "TOTAL", order.getComputedTotal()).getText());
	}
}

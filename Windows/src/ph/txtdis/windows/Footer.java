package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class Footer {
	
	public Footer(OrderView view, OrderData data) {
		Composite footer = new Compo(view.getShell(), 6, GridData.HORIZONTAL_ALIGN_CENTER).getComposite();

		BigDecimal discount1 = data.getDiscount1Total();
		String discount1Text = DIS.formatTo2Places(data.getDiscount1Percent());
		TextDisplayBox discount1Box = new TextDisplayBox(footer, discount1Text + "%", discount1);
		view.setDiscount1Box(discount1Box);
		

		BigDecimal discount2 = data.getDiscount2Total();
		String discount2Text = DIS.formatTo2Places(data.getDiscount2Percent());
		TextDisplayBox discount2Box = new TextDisplayBox(footer, discount2Text + "%", discount2);
		view.setDiscount2Box(discount2Box);
		
		BigDecimal discount = discount1.add(discount2);
		view.setTotalDiscountDisplay(new TextDisplayBox(footer, "DISCOUNT", discount).getText());

		view.setTotalVatableDisplay(new TextDisplayBox(footer, "VATABLE", data.getTotalVatable()).getText());
		view.setTotalVatableDisplay(new TextDisplayBox(footer, "VAT", data.getTotalVat()).getText());
		view.setComputedTotalDisplay(new TextDisplayBox(footer, "TOTAL VAT", data.getComputedTotal()).getText());
	}
}

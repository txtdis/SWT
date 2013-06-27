package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;


public class InvoiceFooter {
	private Text txtTotalDiscount, txtTotalVatable, txtTotalVat, txtSumTotal;
	
	public InvoiceFooter(OrderView view, Order order) {
		GridData gdData = new GridData();
		gdData.horizontalAlignment = GridData.CENTER;
		gdData.grabExcessHorizontalSpace = true;	

		Composite cmpData = new Composite(view.getShell(), SWT.NO_TRIM);
		cmpData.setLayout(new GridLayout(10, false));
		cmpData.setLayoutData(gdData);
		
		String strDiscount1 = DIS.LNF.format(order.getDiscountRate1());
		DataDisplay totalDiscount1 = 
				new DataDisplay(cmpData, strDiscount1 + "%", order.getTotalDiscount1());
		view.setDiscount1(totalDiscount1);
		
		String strDiscount2 = DIS.LNF.format(order.getDiscountRate2());
		DataDisplay totalDiscount2 = 
				new DataDisplay(cmpData, strDiscount2 + "%", order.getTotalDiscount2());
		view.setDiscount2(totalDiscount2);
		
		view.setTxtTotalVatable(
				new DataDisplay(cmpData, "VATABLE", order.getTotalVatable()).getText());
		view.setTxtTotalVat(new DataDisplay(cmpData, "VAT", order.getTotalVat()).getText());
		view.setTxtSumTotal(new DataDisplay(cmpData, "TOTAL", order.getSumTotal()).getText());

		GridData gdEncode = new GridData();
		gdEncode.horizontalSpan = 10;
		gdEncode.horizontalAlignment = GridData.END;
		gdEncode.grabExcessHorizontalSpace = true;	
		Group cmpEncode = new Group(cmpData, SWT.NONE);
		cmpEncode.setLayout(new GridLayout(9, false));
		cmpEncode.setLayoutData(gdEncode);
		
		view.setTxtEncoder(new DataDisplay(cmpEncode, "ENCODER", order.getEncoder(),1).getText());
		view.setTxtEncDate(new DataDisplay(cmpEncode, "DATE", order.getEncDate()).getText());
		view.setTxtEncTime(new DataDisplay(cmpEncode, "TIME", order.getEncTime()).getText());
	}

	public Text getTxtTotalDiscount() {
		return txtTotalDiscount;
	}

	public Text getTxtTotalVatable() {
		return txtTotalVatable;
	}

	public Text getTxtTotalVat() {
		return txtTotalVat;
	}

	public Text getTxtSumTotal() {
		return txtSumTotal;
	}

}

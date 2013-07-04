package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.swt.widgets.Button;

public class SalesOrderView extends OrderView {
	protected SalesOrder salesOrder;
	private Button btnPrinter;

	public SalesOrderView(int orderId) {
		super(orderId);
	}

	@Override
	protected void setTitleBar() {
		new ReportTitleBar(this, salesOrder) {
			@Override
			protected void layButtons() {
				boolean wasPrinted = new SalesOrderPrintOut(orderId).wasPrinted();
				Calendar cal = DateUtils.truncate(Calendar.getInstance(), Calendar.DATE);
				Date today = new Date(cal.getTime().getTime());
				Date soDate = salesOrder.getPostDate();
				if(!Login.group.contains("_supply")) {
					new NewButton(buttons, module);
				} else { //if(!soDate.before(today)) {
					Button btnCancel = new CancelButton(buttons, salesOrder).getButton();
					btnCancel.setEnabled(wasPrinted);
				}
				new RetrieveButton(buttons, report);
				if(salesOrder.getId() == 0)
					btnPost = new PostButton(buttons, reportView, report).getButton();
				if(!wasPrinted && !soDate.before(today)) 
					btnPrinter = new PrintingButton(buttons, salesOrder, false).getButton();
				new ExitButton(buttons, module);
			}
		};
	}

	@Override
	protected void runClass() {
		report = order = salesOrder = new SalesOrder(orderId);
	}

	@Override
	protected void setFocus() {
		if (orderId == 0) {
			btnList.setEnabled(true);
			txtPartnerId.setTouchEnabled(true);
			txtPartnerId.setFocus();
		} else if (btnPrinter != null && !new SalesOrderPrintOut(orderId).wasPrinted()){
			btnPrinter.setEnabled(true);
			btnPrinter.setFocus();
		}
	}

	public Button getBtnPrinter() {
		return btnPrinter;
	}

	public void setBtnPrinter(Button btnPrinter) {
		this.btnPrinter = btnPrinter;
	}

	public static void main(String[] args) {
//		Database.getInstance().getConnection("sheryl", "10-8-91");
		Database.getInstance().getConnection("irene", "ayin");
		Login.user = "irene";
		new SalesOrderView(0);
		Database.getInstance().closeConnection();
	}

}

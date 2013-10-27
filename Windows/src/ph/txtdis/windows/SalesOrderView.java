package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.swt.widgets.Button;

public class SalesOrderView extends OrderView {
	protected SalesOrder salesOrder;
	private Button printerButton;

	public SalesOrderView(int orderId) {
		super(orderId);
	}

	@Override
	protected void setTitleBar() {
		new ReportTitleBar(this, salesOrder) {
			@Override
			protected void layButtons() {
				boolean wasPrinted = new SalesOrderPrintOut(id).wasPrinted();
				Calendar cal = DateUtils.truncate(Calendar.getInstance(), Calendar.DATE);
				Date today = new Date(cal.getTime().getTime());
				Date soDate = salesOrder.getDate();
				if(!Login.getGroup().contains("_supply")) {
					new NewButton(buttons, module);
				} else { //if(!soDate.before(today)) {
					Button btnCancel = new CancelButton(buttons, salesOrder).getButton();
					btnCancel.setEnabled(wasPrinted);
				}
				new RetrieveButton(buttons, report);
				if(salesOrder.getId() == 0)
					((OrderView) view).setPostButton(new PostButton(buttons, order).getButton());
				if(!wasPrinted && !soDate.before(today)) 
					printerButton = new PrintingButton(buttons, salesOrder, false).getButton();
				new ExitButton(buttons, module);
			}
		};
	}

	@Override
	protected void runClass() {
		report = order = salesOrder = new SalesOrder(id);
	}

	@Override
	protected void setFocus() {
		if (id == 0) {
			listButton.setEnabled(true);
			partnerIdInput.setTouchEnabled(true);
			partnerIdInput.setFocus();
		} else if (printerButton != null && !new SalesOrderPrintOut(id).wasPrinted()){
			printerButton.setEnabled(true);
			printerButton.setFocus();
		}
	}

	public Button getPrinterButton() {
		return printerButton;
	}

	public void setPrinterButton(Button printerButton) {
		this.printerButton = printerButton;
	}

	public static void main(String[] args) {
//		Database.getInstance().getConnection("sheryl", "10-8-91");
//		Database.getInstance().getConnection("irene","ayin","localhost");
		Database.getInstance().getConnection("irene","ayin","192.168.1.100");
		Login.setUser("irene");
		new SalesOrderView(0);
		Database.getInstance().closeConnection();
	}

}

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
				Date soDate = salesOrder.getDate();
				if(!Login.getGroup().contains("_support")) {
					new NewButton(buttons, module);
				} else if(!soDate.before(DIS.TODAY)) {
					Button btnCancel = new CancelButton(buttons, salesOrder).getButton();
					btnCancel.setEnabled(wasPrinted);
				}
				new RetrieveButton(buttons, report);
				if(salesOrder.getId() == 0)
					((OrderView) view).setPostButton(new PostButton(buttons, order).getButton());
				if(!wasPrinted && DateUtils.truncatedCompareTo(soDate, DIS.TODAY, Calendar.DATE) >= 0) 
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
//		Database.getInstance().getConnection("sheryl", "10-8-91", "localhost");
//		Database.getInstance().getConnection("maricel","maricel","localhost");
		Database.getInstance().getConnection("badette","013094","192.168.1.100");
		new SalesOrderView(0);
		Database.getInstance().closeConnection();
	}

}

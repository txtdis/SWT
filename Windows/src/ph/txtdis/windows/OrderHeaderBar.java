package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class OrderHeaderBar {
	public OrderHeaderBar(OrderView view, Order order) {

		String module = order.getModule();

		Composite cmpInfo = new Compo(view.getShell(), 3, GridData.FILL_HORIZONTAL).getComposite();

		// / INVOICE GROUP
		Group grpInvoice = new Group(cmpInfo, SWT.NONE);
		grpInvoice.setLayout(new GridLayout(2, false));
		grpInvoice.setText("ORDER INFO");
		GridData gdInvoice = new GridData();
		gdInvoice.verticalSpan = 3;
		gdInvoice.verticalAlignment = GridData.BEGINNING;
		grpInvoice.setLayoutData(gdInvoice);
		switch (module) {
			case "Invoice":
				view.setTxtSoId(new DataEntry(grpInvoice, "S/O(P/O)#", order.getSoId()).getText());
				view.setTxtSeries(new DataEntry(grpInvoice, "SERIES", order.getSeries(), 1).getText());
				view.setTxtOrderId(new DataEntry(grpInvoice, "INVOICE #", order.getId()).getText());
				view.setTxtEnteredTotal(new DataEntry(grpInvoice, "S/I AMOUNT", order.getEnteredTotal()).getText());
				break;
			case "Delivery Report":
				view.setTxtSoId(new DataEntry(grpInvoice, "S/O #", order.getSoId()).getText());
				view.setTxtOrderId(new DataDisplay(grpInvoice, "D/R #", order.getId()).getText());
				view.setTxtEnteredTotal(new DataEntry(grpInvoice, "D/R AMT", order.getEnteredTotal()).getText());
				break;
			case "Sales Order":
				view.setTxtSoId(new DataDisplay(grpInvoice, "S/O #", order.getSoId()).getText());
				view.setTxtEnteredTotal(new DataDisplay(grpInvoice, "LIMIT", order.getEnteredTotal()).getText());
				break;
			case "Purchase Order":
				view.setTxtSoId(new DataDisplay(grpInvoice, "P/O #", order.getId()).getText());
				view.setTxtEnteredTotal(new DataDisplay(grpInvoice, "LIMIT", order.getEnteredTotal()).getText());
				break;
			default:
				new ErrorDialog("No Invoice Header Bar option\nfor " + module);
				break;
		}
		// / RECIPIENT GROUP
		Group grpPartner = new Grp(cmpInfo, 3, "CUSTOMER INFO", GridData.FILL_HORIZONTAL).getGroup();

		int partnerId = order.getPartnerId();
		CustomerHelper cust = new CustomerHelper(partnerId);
		view.setTxtPartnerId(new DataEntry(grpPartner, "ID #", partnerId).getText());
		view.setBtnList(new ListButton(grpPartner, "Customer List").getButton());
		view.setTxtPartnerName(new DataDisplay(grpPartner, "NAME", cust.getName(), 2).getText());
		view.getBtnList().setEnabled(false);

		// / DATE SUBGROUP
		Group grpDate = new Grp(cmpInfo, 2, "DATE", GridData.FILL_VERTICAL).getGroup();
		Date invoicePostDate = order.getPostDate();
		view.setTxtPostDate(new DataEntry(grpDate, "POST", invoicePostDate).getText());
		view.setTxtDueDate(new DataDisplay(grpDate, "DUE", new DateAdder(invoicePostDate).plus(order.getLeadTime()))
		        .getText());

		// / ADDRESS SUBGROUP
		Composite cmpAddress = new Composite(cmpInfo, SWT.NO_TRIM);
		cmpAddress.setLayout(new GridLayout(4, false));
		GridData gdAddress = new GridData();
		gdAddress.horizontalSpan = 2;
		gdAddress.horizontalAlignment = GridData.FILL;
		cmpAddress.setLayoutData(gdAddress);

		view.setTxtAddress(new DataDisplay(cmpAddress, "ADDRESS", order.getAddress(), 2).getText());
	}
}

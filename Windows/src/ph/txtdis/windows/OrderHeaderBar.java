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
				view.setReferenceIdInput(new TextInputBox(grpInvoice, "S/O(P/O)#", order.getReferenceId()).getText());
				view.setTxtSeries(new TextInputBox(grpInvoice, "SERIES", order.getSeries(), 1).getText());
				view.setIdInput(new TextInputBox(grpInvoice, "INVOICE #", order.getId()).getText());
				view.setTxtEnteredTotal(new TextInputBox(grpInvoice, "S/I AMOUNT", order.getEnteredTotal()).getText());
				break;
			case "Delivery Report":
				view.setReferenceIdInput(new TextInputBox(grpInvoice, "S/O #", order.getReferenceId()).getText());
				view.setIdInput(new TextDisplayBox(grpInvoice, "D/R #", order.getId()).getText());
				view.setTxtEnteredTotal(new TextInputBox(grpInvoice, "D/R AMT", order.getEnteredTotal()).getText());
				break;
			case "Sales Order":
				view.setReferenceIdInput(new TextDisplayBox(grpInvoice, "S/O #", order.getReferenceId()).getText());
				view.setTxtEnteredTotal(new TextDisplayBox(grpInvoice, "LIMIT", order.getEnteredTotal()).getText());
				break;
			case "Purchase Order":
				view.setReferenceIdInput(new TextDisplayBox(grpInvoice, "P/O #", order.getId()).getText());
				view.setTxtEnteredTotal(new TextDisplayBox(grpInvoice, "LIMIT", order.getEnteredTotal()).getText());
				break;
			default:
				new ErrorDialog("No Invoice Header Bar option\nfor " + module);
				break;
		}
		// / RECIPIENT GROUP
		Group grpPartner = new Grp(cmpInfo, 3, "CUSTOMER INFO", GridData.FILL_HORIZONTAL).getGroup();

		int partnerId = order.getPartnerId();
		view.setTxtPartnerId(new TextInputBox(grpPartner, "ID #", partnerId).getText());
		view.setListButton(new ListButton(grpPartner, "Customer List").getButton());
		view.setTxtPartnerName(new TextDisplayBox(grpPartner, "NAME", new Customer().getName(partnerId), 2).getText());
		view.getListButton().setEnabled(false);

		// / DATE SUBGROUP
		Group grpDate = new Grp(cmpInfo, 2, "DATE", GridData.FILL_VERTICAL).getGroup();
		Date invoicePostDate = order.getDate();
		view.setTxtPostDate(new TextInputBox(grpDate, "POST", invoicePostDate).getText());
		view.setTxtDueDate(new TextDisplayBox(grpDate, "DUE", new DateAdder(invoicePostDate).plus(order.getLeadTime()))
		        .getText());

		// / ADDRESS SUBGROUP
		Composite address = new Compo(cmpInfo, 4, SWT.FILL, SWT.BEGINNING, true, false, 2, 1).getComposite();
		view.setAddressDisplay(new TextDisplayBox(address, "ADDRESS", order.getAddress(), 2).getText());
	}
}

package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public abstract class Subheader {
	public Subheader(OrderView view, OrderData data) {
		Composite subheader = new Compo(view.getShell(), 3, GridData.FILL_HORIZONTAL).getComposite();
		
		Group order = new Grp(subheader, 2, "ORDER INFO", SWT.FILL, SWT.BEGINNING, false, true, 1, 2).getGroup();
		setOrderGroup(view, data, order);

		Group partner = new Grp(subheader, 3, "CUSTOMER INFO", GridData.FILL_HORIZONTAL).getGroup();
		int partnerId = data.getPartnerId();
		String partnerName = Customer.getName(partnerId);
		view.setPartnerIdInput(new TextInputBox(partner, "ID #", partnerId).getText());
		setListButton(view, partner);
		view.setPartnerDisplay(new TextDisplayBox(partner, "NAME", partnerName, 2).getText());
		
		Date post = data.getDate();
		Date due = DIS.addDays(post,data.getLeadTime());
		Group date = new Grp(subheader, 2, "DATE", GridData.FILL_VERTICAL).getGroup();
		view.setDateInput(new TextInputBox(date, "POST", post).getText());
		view.setDueDisplay(new TextDisplayBox(date, "DUE", due).getText());

		Composite address = new Compo(subheader, 6, SWT.FILL, SWT.BEGINNING, true, false, 4, 1).getComposite();
		view.setAddressDisplay(new TextDisplayBox(address, "ADDRESS", data.getAddress(), 2).getText());
	}

	private void setListButton(OrderView view, Group partner) {
	    Button listButton = new ImgButton(partner, Type.SEARCH16, Type.CUSTOMER_LIST).getButton();
		view.setListButton(listButton);
		listButton.setEnabled(false);
    }

	protected abstract void setOrderGroup(OrderView view, OrderData data, Group grpInvoice);
}

package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

public class ReceivingHeaderBar {

	public ReceivingHeaderBar(ReceivingView view, Receiving receiving) {

		Shell shell = view.getShell();
		Composite cmpInfo = new Composite(shell, SWT.NO_TRIM);
		cmpInfo.setLayout(new GridLayout(2, false));
		cmpInfo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		/// PARTNER GROUP
		Group grpPartner = new Group(cmpInfo, SWT.NONE);
		grpPartner.setLayout(new GridLayout(3, false));
		grpPartner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpPartner.setText("PARTNER INFO");		
		int partnerId = receiving.getPartnerId();
		CustomerHelper cust = new CustomerHelper(partnerId);
		view.setTxtPartnerId(new DataEntry(grpPartner, "ID", partnerId).getText());
		view.setBtnList(new ListButton(grpPartner, "Partner List").getButton());
		view.setTxtPartnerName(new DataDisplay(grpPartner, "NAME", cust.getName(), 2).getText());
		view.setTxtAddress(new DataDisplay(grpPartner, "ADDRESS", 
				new Address(partnerId).getAddress(), 2).getText());
		view.getBtnList().setEnabled(false);

		/// DETAIL SUBGROUP
		Group grpDetail = new Group(cmpInfo, SWT.NONE);
		grpDetail.setLayout(new GridLayout(2, false));
		grpDetail.setText("DETAILS");
		grpDetail.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		view.setTxtDate(new DataEntry(grpDetail, "DATE", receiving.getDate()).getText());
		view.setTxtOrderId(new DataEntry(grpDetail, "RR ID", receiving.getRrId()).getText());
		view.setTxtRefId(new DataEntry(grpDetail, "REF ID", receiving.getRefId()).getText());
	}
}

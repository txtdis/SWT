package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class OrderPartnerIdEntry{
	private Text txtAddress, txtPartnerId, txtPartnerName, txtPostDate, txtDueDate;
	private Date postDate;
	private int partnerId, creditTerm;
	private String strPartnerId, module;
	private Button btnList;
	private BigDecimal actual;

	public OrderPartnerIdEntry(OrderView view, final Order order) {
		txtPartnerId = view.getTxtPartnerId();
		txtPartnerName = view.getTxtPartnerName();
		txtPostDate = view.getTxtPostDate();
		txtDueDate = view.getTxtDueDate();
		txtAddress = view.getTxtAddress();
		btnList = view.getBtnList();
		module = order.getModule();

		new IntegerVerifier(txtPartnerId);
		txtPartnerId.addListener (SWT.DefaultSelection, new Listener () {
			@Override
			public void handleEvent (Event ev) {
				postDate = order.getPostDate();
				actual = order.getActual();
				strPartnerId = txtPartnerId.getText().trim();
				if (StringUtils.isBlank(strPartnerId)) return;
				// retrieve name from id input
				partnerId = Integer.parseInt(strPartnerId);
				final String name = new CustomerHelper(partnerId).getName();
				if (name == null)  {
					new ErrorDialog("" +
							"Sorry, Customer ID " + partnerId + "\n" +
							"is not in our system.");
					clearInput();
					return;
				}
				// Ensure only internal and other channels are not payment-tracked
				if(module.equals("Delivery Report") 
						&& actual.equals(BigDecimal.ZERO) 
						&& !new CustomerHelper().isInternalOrOthers(partnerId)) {
					new ErrorDialog("" +
							"Sorry, only internal and other customers" + 
							"\ndo not involve payment.");
					clearInput();
					return;					
				}
				if (module.equals("Sales Order")) { 
					// Check for aging A/R
					if (!new Overdue(partnerId, DIS.OVERDUE_CUTOFF).getBalance().equals(BigDecimal.ZERO)) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								new OverdueView(partnerId, DIS.OVERDUE_CUTOFF);
							}
						});
						new InfoDialog("" +
								"Click the PHONE icon to request\n" +
								"approval to deliver to\n" +
								name + "\n" +
								"today and/or tomorrow;\n" +
								"You may click the PRINTER button\n" +
								"if you want copy of the outlet's A/R" +
								"");
						clearInput();
						return;
					}
					// Check if route report is completely balanced
					if (!new RouteHelper().isBalanced(partnerId, postDate)) {
						new InfoDialog("" +
								"Complete and balance all Route Reports\n" +
								"starting " + DIS.LDF.format(DIS.BALANCE_CUTOFF) + "\n" +
								"before making a new Sales Order.\n" +
								"");
						clearInput();
						txtPartnerId.getShell().dispose();
						new RemittanceView(0);
						return;	
					}
				}
				
				// save partner id
				order.setPartnerId(partnerId);
				// show name
				txtPartnerName.setText(name);
				// show credit term on date due
				creditTerm = new Credit().getTerm(partnerId, postDate);
				order.setLeadTime(creditTerm);
				txtDueDate.setText(new DateAdder(txtPostDate.getText()).add(order.getLeadTime()));
				// show address
				txtAddress.setText(new Address(partnerId).getAddress());
				// disable partner ID input
				txtPartnerId.setTouchEnabled(false);
				btnList.setEnabled(false);
				// go to invoice date
				txtPostDate.setTouchEnabled(true);
				txtPostDate.setFocus(); 
			}
		});
	}

	private void clearInput() {
		txtPartnerId.setText("");
		txtPartnerId.setFocus();	
		txtPartnerId.setEditable(true);
		txtPartnerId.setBackground(View.yellow());
		txtPartnerId.selectAll();		
	}
}

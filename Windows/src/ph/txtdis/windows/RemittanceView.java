package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class RemittanceView extends ReportView {
	private int remitId, bankId, refId, rowIdx, orderId;
	private String series;
	private Remittance remit;
	private Date date;
	private Time time;
	private Text txtBankId, txtBankName, txtDate, txtTime;
	private Text txtOrderId, txtSeries, txtBalance, txtRefId, txtTotalPayment;
	private Text txtRemitId, txtOrId;
	private Button btnBouncedCheck, btnNewRemittance, btnNewOrder, btnPost,
			btnList;
	private TableItem tableItem;

	public RemittanceView(int remitId) {
		this.remitId = remitId;
		setProgress();
		setTitleBar();
		setHeader();
		setTableBar();
		setFooter();
		setListener();
		setFocus();
		showReport();
	}

	@Override
	protected void runClass() {
		report = remit = new Remittance(remitId);
	}

	@Override
	protected void setTitleBar() {
		new ListTitleBar(this, remit) {
			@Override
			protected void layButtons() {
				if (remitId > 0) {
					if (Login.group.contains("_finance")) {
						btnBouncedCheck = new ImageButton(buttons, module,
								module, module) {

						}.getButton();
					} else {
						btnNewRemittance = new NewButton(buttons, module)
								.getButton();
					}
				}
				new RetrieveButton(buttons, report);
				if (remitId == 0)
					btnPost = new PostButton(buttons, reportView, report)
							.getButton();
				new ExitButton(buttons, module);
			}
		};
	}

	@Override
	protected void setHeader() {
		Composite cmpInfo = new Composite(shell, SWT.NO_TRIM);
		cmpInfo.setLayout(new GridLayout(3, false));
		cmpInfo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// / BANK GROUP
		Group grpPartner = new Group(cmpInfo, SWT.NONE);
		grpPartner.setLayout(new GridLayout(3, false));
		grpPartner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpPartner.setText("BANK INFORMATION");

		int bankId = remit.getPartnerId();
		CustomerHelper cust = new CustomerHelper(bankId);
		txtBankId = new DataEntry(grpPartner, "ID #", bankId).getText();
		btnList = new ListButton(grpPartner, "Bank List").getButton();
		txtBankName = new DataDisplay(grpPartner, "NAME", cust.getName(), 2)
				.getText();
		btnList.setEnabled(false);

		// / TIMESTAMP SUBGROUP
		Date postDate = remit.getDate();
		Time time = remit.getTime();

		Group grpDate = new Group(cmpInfo, SWT.NONE);
		grpDate.setLayout(new GridLayout(2, false));
		grpDate.setText("TIMESTAMP");
		grpDate.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		txtTime = new DataEntry(grpDate, "TIME", time).getText();
		txtDate = new DataEntry(grpDate, "DATE", postDate).getText();

		// / DETAIL GROUP
		Group grpInvoice = new Group(cmpInfo, SWT.NONE);
		grpInvoice.setLayout(new GridLayout(4, false));
		grpInvoice.setText("DETAILS");
		GridData gd = new GridData();
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalAlignment = GridData.END;
		grpInvoice.setLayoutData(gd);
		txtRefId = new DataEntry(grpInvoice, "REF ID", remit.getRefId())
				.getText();
		txtRemitId = new DataDisplay(grpInvoice, "REMIT ID", remit.getRemitId())
				.getText();
		txtTotalPayment = new DataEntry(grpInvoice, "AMOUNT",
				remit.getTotalPayment()).getText();
		txtOrId = new DataEntry(grpInvoice, "OR ID", remit.getOrId()).getText();
	}

	@Override
	protected void setFooter() {
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.END;
		gd.grabExcessHorizontalSpace = true;

		Composite cmp = new Composite(shell, SWT.NO_TRIM);
		cmp.setLayout(new RowLayout());
		cmp.setLayoutData(gd);
		txtBalance = new DataDisplay(cmp, "VARIANCE", remit.getBalance())
				.getText();
	}

	@Override
	protected void setListener() {
		new DataInput(txtBankId, txtTime) {
			@Override
			protected boolean act() {
				String strBankId = txtBankId.getText().trim();
				if (StringUtils.isBlank(strBankId))
					return false;
				bankId = Integer.parseInt(strBankId);
				String name = new CustomerHelper().getBankName(bankId);
				if (name == null) {
					new ErrorDialog("Sorry, Bank ID " + bankId + "\n"
							+ "is not in our system.");
					return false;
				}
				remit.setPartnerId(bankId);
				txtBankName.setText(name);
				btnList.setEnabled(false);
				return true;
			}
		};
		new DataInput(txtTime, txtDate) {
			@Override
			protected boolean act() {
				try {
					String strTime = txtTime.getText();
					time = new Time(DIS.TF.parse(strTime).getTime());
					remit.setTime(time);
					return true;
				} catch (ParseException e) {
					new ErrorDialog(e);
					return false;
				}
			}
		};
		new DataInput(txtDate, txtRefId) {
			@Override
			protected boolean act() {
				try {
					String strDate = txtDate.getText();
					date = new Date(DIS.DF.parse(strDate).getTime());
					remit.setDate(date);
					return true;
				} catch (ParseException e) {
					new ErrorDialog(e);
					return false;
				}
			}
		};
		new DataInput(txtRefId, txtTotalPayment) {
			@Override
			protected boolean ifHasText() {
				refId = Integer.parseInt(string);
				if (refId <= 0)
					return false;
				remitId = new RemittanceHelper().getRemitId(bankId, date, time,
						refId);
				if (remitId != 0) {
					String deposit = "Deposit Slip #";
					try {
						if (time.equals(new Time(DIS.TF.parse("00:00")
								.getTime())))
							deposit = "Check #";
					} catch (ParseException e) {
						e.printStackTrace();
					}
					new ErrorDialog("" + "Sorry, " + deposit + refId
							+ "\nhas been used in Remittance #" + remitId);
					return false;
				}
				remit.setRefId(refId);
				return true;
			}
		};
		new DataInput(txtTotalPayment, txtOrId) {
			@Override
			protected boolean ifHasText() {
				BigDecimal totalPayment = new BigDecimal(string);
				if (totalPayment.compareTo(BigDecimal.ZERO) <= 0)
					return false;
				remit.setTotalPayment(totalPayment);
				return true;
			}
		};
		new DataInput(txtOrId, txtSeries) {
			@Override
			protected boolean act() {
				String strOrId = txtOrId.getText().trim();
				if (!StringUtils.isBlank(strOrId)) {
					int orId = Integer.parseInt(strOrId);
					if (orId <= 0) {
						orId = 0;
						return false;
					}
				}
				tableItem = new TableItem(table, SWT.NO_TRIM, rowIdx);
				setSeries();
				setNext(txtSeries);
				return true;
			}
		};
	}

	private void setSeries() {
		new TableButton(tableItem, rowIdx, 0, report.getModule() + "16")
				.getButton();
		txtSeries = new TableInput(tableItem, rowIdx, 1, "EXT").getText();
		txtSeries.setTouchEnabled(true);
		txtSeries.setFocus();
		new DataInput(txtSeries, txtOrderId) {
			@Override
			protected boolean act() {
				series = txtSeries.getText().trim();
				if (series.isEmpty())
					series = " ";
				if (new OrderHelper().hasSeries(series)) {
					btnNewOrder.dispose();
					txtSeries.dispose();
					tableItem.setText(0, "" + (rowIdx + 1));
					tableItem.setText(1, series);
					setOrderIdInput();
					setNext(txtOrderId);
					return true;
				} else {
					new ErrorDialog("" + "Booklet Series " + series
							+ "\nhas yet to be issued");
					return false;
				}
			}
		};
	}

	// Item Invoice/DR # input listener
	private void setOrderIdInput() {
		txtOrderId = new TableInput(tableItem, rowIdx, 2, BigDecimal.ZERO)
				.getText();
		txtOrderId.setTouchEnabled(true);
		txtOrderId.setFocus();
		new DataInput(txtOrderId, btnPost) {
			@Override
			protected boolean act() {
				String strOrderId = txtOrderId.getText().trim();
				if (StringUtils.isBlank(strOrderId)) {
					if (rowIdx == 0 || !btnPost.isEnabled())
						return false;
					else if (btnPost.isEnabled())
						return true;
				}
				if (strOrderId.equals("-"))
					return false;
				orderId = Integer.parseInt(strOrderId);
				String orderType = "";
				Order order = null;
				if (orderId > 0) {
					orderType = "Invoice";
					order = new Invoice(orderId, series);
				} else if (orderId < 0) {
					orderType = "Delivery Report";
					order = new Delivery(-orderId);
				} else {
					new ErrorDialog("Enter positive integers for S/I,\n"
							+ "negative for D/R, no zeroes(0)");
					return false;
				}
				if (!new OrderHelper(orderId).hasBeenUsed(series)) {
					new ErrorDialog(orderType + " #" + Math.abs(orderId)
							+ "\nis not in our system");
					return false;
				} else if (remit.getOrderIds().contains(orderId)) {
					new ErrorDialog(orderType + " #" + +Math.abs(orderId)
							+ "\nis already on the list");
					return false;
				}
				BigDecimal actualOfThisOrder = order.getActual();
				BigDecimal payment = new RemittanceHelper().getPayment(series,
						orderId);
				BigDecimal totalOfThisOrder = actualOfThisOrder
						.subtract(payment);
				int firstItemId = new OrderHelper(orderId)
						.getFirstLineItemId(series);
				System.out.println(firstItemId);
				String shortId = new ItemHelper().getShortId(firstItemId);
				if (shortId != null && shortId.equals("OR")) {
					if (orderId < 0
							&& actualOfThisOrder.equals(totalOfThisOrder)) {
						totalOfThisOrder = actualOfThisOrder.abs();
					} else if (orderId < 0
							&& payment.compareTo(BigDecimal.ZERO) > 0) {
						totalOfThisOrder = actualOfThisOrder;
					}
				}
				// check if invoice has balance
				if (actualOfThisOrder.equals(BigDecimal.ZERO)) {
					// check if invoice has actual amount inputed
					new ErrorDialog(orderType + " #" + Math.abs(orderId)
							+ "\nhas no actual amount saved.\n"
							+ "Fill the datum in, then continue.");
					return false;
				} else if (totalOfThisOrder.compareTo(BigDecimal.ZERO) == 0) {
					// check if invoice has been used
					String error = orderType + " #" + Math.abs(orderId)
							+ "\nhas been fully paid.";
					if (actualOfThisOrder.compareTo(BigDecimal.ZERO) < 0)
						error = "Negative invoice #" + orderId + "\n"
								+ "has been used.";
					new ErrorDialog(error);
					return false;
				} else {
					int customerId = order.getPartnerId();
					CustomerHelper customerHelper = new CustomerHelper(
							customerId);
					Date postDate = order.getPostDate();
					int term = new Credit().getTerm(customerId, postDate);
					Date dueDate = new DateAdder(postDate).plus(term);
					BigDecimal totalPayment = remit.getTotalPayment();
					BigDecimal runningOrderTotal = remit.getRunningOrderTotal()
							.add(totalOfThisOrder);
					BigDecimal runningPaymentTotal = remit
							.getRunningPaymentTotal();
					BigDecimal paymentForThisOrder;
					if (runningOrderTotal.compareTo(totalPayment) > 0) {
						paymentForThisOrder = totalPayment
								.subtract(runningPaymentTotal);
					} else {
						paymentForThisOrder = totalOfThisOrder;
					}
					runningPaymentTotal = runningPaymentTotal
							.add(paymentForThisOrder);
					tableItem.setText(2, DIS.BIF.format(orderId));
					tableItem.setText(3, "" + customerId);
					tableItem.setText(4, customerHelper.getName());
					tableItem.setText(5, DIS.DF.format(postDate));
					tableItem.setText(6, DIS.DF.format(dueDate));
					tableItem.setText(7, DIS.SNF.format(totalOfThisOrder));
					tableItem.setText(8, DIS.SNF.format(paymentForThisOrder));
					BigDecimal balance = totalPayment
							.subtract(runningOrderTotal);
					txtBalance.setText(DIS.SNF.format(balance));
					txtOrderId.dispose();
					if (balance.abs().compareTo(BigDecimal.ONE) <= 0
							&& btnPost != null) {
						btnPost.setEnabled(true);
						setNext(btnNewRemittance);
						return true;
					}
					if (balance.compareTo(new BigDecimal(-1)) < 0) {
						txtBalance.setForeground(View.red());
						new ErrorDialog(""
								+ "Invoice/Delivery Report's running total\n"
								+ "has exceeded deposited/check amount.\n"
								+ "Balance them first before continuing.");
						remit.getOrderIds().add(rowIdx, orderId);
						remit.getSeriesList().add(rowIdx, series);
						remit.getPayments().add(rowIdx++, paymentForThisOrder);
						remit.setRunningOrderTotal(runningOrderTotal);
						remit.setRunningPaymentTotal(runningPaymentTotal);
						setNext(btnNewRemittance);
						return true;
					}
					remit.getOrderIds().add(rowIdx, orderId);
					remit.getSeriesList().add(rowIdx, series);
					remit.getPayments().add(rowIdx++, paymentForThisOrder);
					remit.setRunningOrderTotal(runningOrderTotal);
					remit.setRunningPaymentTotal(runningPaymentTotal);
					tableItem = new TableItem(table, SWT.NO_TRIM, rowIdx);
					tableItem.setBackground(rowIdx % 2 == 0 ? View.white()
							: View.gray());
					if (rowIdx > 9)
						table.setTopIndex(rowIdx - 9);
					setSeries();
					setNext(txtSeries);
				}
				txtOrderId.dispose();
				return true;
			}
		};
	}

	@Override
	protected void setFocus() {
		txtBankId.setTouchEnabled(true);
		txtBankId.setFocus();
		btnList.setEnabled(true);
	}

	public Text getTxtBalance() {
		return txtBalance;
	}

	public Remittance getRemittance() {
		return remit;
	}

	public int getRemitId() {
		return remitId;
	}

	public Text getTxtPartnerId() {
		return txtBankId;
	}

	public Text getTxtPartnerName() {
		return txtBankName;
	}

	public Text getTxtPostDate() {
		return txtDate;
	}

	public Text getTxtTime() {
		return txtTime;
	}

	public Date getPostDate() {
		return date;
	}

	public Time getTime() {
		return time;
	}

	public Button getBtnList() {
		return btnList;
	}

	public Button getBtnPost() {
		return btnPost;
	}

	public Text getTxtOrderId() {
		return txtOrderId;
	}

	public Text getTxtRefId() {
		return txtRefId;
	}

	public Text getTxtOrId() {
		return txtOrId;
	}

	public Text getTxtTotalPayment() {
		return txtTotalPayment;
	}

	public Text getTxtRemitId() {
		return txtRemitId;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene", "ayin");
		new RemittanceView(0);
		Database.getInstance().closeConnection();
	}
}

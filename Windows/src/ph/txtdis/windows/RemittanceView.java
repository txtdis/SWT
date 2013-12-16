package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class RemittanceView extends OrderView {
	private int orderId;
	private Button btnNewOrder;
	private Customer customer;
	private Date date;
	private Order order;
	private Remittance remit;
	private String series, orderType;
	private TableItem tableItem;
	private Text timeInput, idInput, txtSeries, txtBalance, txtTotalPayment, txtOrId;
	private Time time;

	public RemittanceView(int remitId) {
		this.id = remitId;
		customer = new Customer();
		setProgress();
		setTitleBar();
		setHeader();
		getTable();
		setFooter();
		setListener();
		setFocus();
		showReport();
	}

	@Override
	protected void runClass() {
		report = remit = new Remittance(id);
	}

	@Override
	protected void setTitleBar() {
		new ListTitleBar(this, remit) {
			@Override
			protected void layButtons() {
				if (id > 0) {
					if ((Login.getGroup().contains("_finance") || Login.getGroup().contains("sys_admin"))
					        && remit.isPaymentByCheck(id)) {
						new ImageButton(buttons, module, "Cancel", "Tag check payment\nhas bounced") {

							@Override
							protected void doWhenSelected() {
								new DialogView("Cancel", "You are about to cancel\n"
								        + new Customer().getName(remit.getPartnerId()) + "\n" + "Check #"
								        + remit.getReferenceId()) {

									@Override
									protected void setOkButtonAction() {
										super.setOkButtonAction();
										if (new RemittanceCancellationPosting().set(remit)) {
											new RemittanceView(id);
										}
									}
								};
							}
						}.getButton();
					}
				}
				newButton = new NewButton(buttons, module).getButton();
				new BackwardButton(buttons, report);
				new RetrieveButton(buttons, report);
				new ForwardButton(buttons, report);
				if (id == 0)
					postButton = new PostButton(buttons, remit).getButton();
				new ExitButton(buttons, module);
			}
		};
	}

	@Override
	protected void setHeader() {
		Composite info = new Compo(shell, 3, GridData.HORIZONTAL_ALIGN_FILL).getComposite();

		Group bank = new Grp(info, 3, "BANK INFORMATION", GridData.FILL_HORIZONTAL).getGroup();
		partnerId = remit.getPartnerId();
		partnerIdInput = new TextInputBox(bank, "ID #", partnerId).getText();
		listButton = new ListButton(bank, "Bank List").getButton();
		partnerDisplay = new TextDisplayBox(bank, "NAME", customer.getName(partnerId), 2).getText();
		listButton.setEnabled(false);

		Group dateTime = new Grp(info, 2, "TIMESTAMP", GridData.HORIZONTAL_ALIGN_END).getGroup();
		timeInput = new TextInputBox(dateTime, "TIME", remit.getTime()).getText();
		dateInput = new TextInputBox(dateTime, "DATE", remit.getDate()).getText();

		Group invoice = new Grp(info, 4, "DETAILS", GridData.HORIZONTAL_ALIGN_END).getGroup();
		referenceIdInput = new TextInputBox(invoice, "REF ID", remit.getReferenceId()).getText();
		idDisplay = new TextDisplayBox(invoice, "REMIT ID", remit.getId()).getText();
		txtTotalPayment = new TextInputBox(invoice, "AMOUNT", remit.getEnteredTotal()).getText();
		txtOrId = new TextInputBox(invoice, "OR ID", remit.getReceiptId()).getText();
	}

	@Override
	protected void setFooter() {

		Composite footer = new Compo(shell, 3, GridData.FILL_HORIZONTAL).getComposite();

		Composite status = new Compo(footer, 6, GridData.FILL_HORIZONTAL).getComposite();
		String statusText = remit.getStatus();
		Text statusDisplay = new TextDisplayBox(status, "STATUS", statusText, 1).getText();
		statusDisplay.setForeground(statusText.equals("CANCELLED") ? UI.RED : UI.BLACK);
		new TextDisplayBox(status, "PER", remit.getTagger(), 1).getText();
		new TextDisplayBox(status, "DATE", remit.getStatusDate()).getText();

		Composite input = new Compo(footer, 6, GridData.HORIZONTAL_ALIGN_CENTER).getComposite();
		new TextDisplayBox(input, "ENCODER", remit.getInputter(), 1).getText();
		new TextDisplayBox(input, "DATE", remit.getInputDate()).getText();
		new TextDisplayBox(input, "TIME", remit.getInputTime()).getText();

		Composite variance = new Compo(footer, 2, GridData.HORIZONTAL_ALIGN_END).getComposite();
		txtBalance = new TextDisplayBox(variance, "VARIANCE", remit.getBalance()).getText();
	}

	@Override
	protected void setListener() {
		new TextInputter(partnerIdInput, timeInput) {
			@Override
			protected boolean isThePositiveNumberValid() {
				partnerId = numericInput.intValue();
				String name = new Customer().getBankName(partnerId);
				if (name.isEmpty()) {
					new ErrorDialog("Sorry, Bank ID " + partnerId + "\n" + "is not in our system.");
					return false;
				}
				partnerDisplay.setText(name);
				listButton.setEnabled(false);
				remit.setPartnerId(partnerId);
				return true;
			}
		};
		
		new TextInputter(timeInput, dateInput) {
			@Override
			protected boolean isInputValid() {
				time = DIS.parseTime(textInput);
				remit.setTime(time);
				return true;
			}
		};
		
		new DateInputter(dateInput, referenceIdInput) {
			@Override
			protected boolean isTheDataInputValid() {
				remit.setDate(date);
				return true;
			}
		};

		new TextInputter(referenceIdInput, txtTotalPayment) {
			@Override
			protected boolean isThePositiveNumberValid() {
				referenceId = numericInput.intValue();
				id = remit.getId(partnerId, date, time, referenceId);
				if (id != 0) {
					String deposit = "Deposit Slip #";
					if (time.equals(DIS.ZERO_TIME))
						deposit = "Check #";
					new ErrorDialog("" + "Sorry, " + deposit + referenceId + "\nhas been used in Remittance #" + id);
					return false;
				}
				remit.setReferenceId(referenceId);
				return true;
			}
		};

		new TextInputter(txtTotalPayment, txtOrId) {
			@Override
			protected boolean isThePositiveNumberValid() {
				remit.setEnteredTotal(numericInput);
				return true;
			}
		};
		
		new TextInputter(txtOrId, txtSeries) {
			@Override
			protected boolean isThePositiveNumberValid() {
				remit.setReceiptId(numericInput.intValue());
				tableItem = new TableItem(table, SWT.NO_TRIM, rowIdx);
				setSeries();
				return true;
			}
		};
	}

	private void setSeries() {
		btnNewOrder = new TableButton(tableItem, rowIdx, 0, report.getModule() + "16").getButton();
		txtSeries = new TableTextInput(tableItem, rowIdx, 1, "").getText();
		series = null;
		txtSeries.setTouchEnabled(true);
		txtSeries.setFocus();
		new TextInputter(txtSeries, idInput) {

			@Override
			protected boolean isABlankInputNotValid() {
				series = " ";
				shouldReturn = false;
				return false;
			}

			@Override
			protected boolean isTheDataInputValid() {
				if (series != null)
					series = " ";
				else 
					series = textInput;
				if (new OrderHelper().hasSeries(series)) {
					btnNewOrder.dispose();
					txtSeries.dispose();
					tableItem.setText(0, String.valueOf(rowIdx + 1));
					tableItem.setText(1, series);
					remit.setSeries(series);
					setOrderIdInput();
					return true;
				} else {
					new ErrorDialog("Booklet Series " + series + "\nhas yet to be issued");
					return false;
				}
			}
		};
	}

	// Item Invoice/DR # input listener
	private void setOrderIdInput() {
		idInput = new TableTextInput(tableItem, rowIdx, 2, BigDecimal.ZERO).getText();
		idInput.setTouchEnabled(true);
		idInput.setFocus();
		orderId = 0;
		new TextInputter(idInput, postButton) {
			@Override
			protected boolean isTheNegativeNumberNotValid() {
				orderId = numericInput.intValue();
				orderType = "D/R";
				order = new Delivery(-orderId);
				shouldReturn = false;
				return false;
			}

			@Override
			protected boolean isZeroNotValid() {
				new ErrorDialog("Enter positive integers for S/I,\nnegative for D/R, no zeroes(0)");
				if (orderId < 0)
					shouldReturn = false;
				return false;
			}

			@Override
			protected boolean isThePositiveNumberValid() {
				if (orderId == 0)
					orderId = numericInput.intValue();
				if (orderId > 0) {
					orderType = "S/I";
					order = new Invoice(orderId, remit.getSeries());
				}
				shouldReturn = false;
				return true;
			}

			@Override
			protected boolean isTheSignedNumberValid() {
				if (!new OrderHelper(orderId).isOnFile(series)) {
					new ErrorDialog(orderType + " #" + Math.abs(orderId) + "\nis not in our system");
					return false;
				} else if (remit.getOrderIds().contains(orderId)) {
					new ErrorDialog(orderType + " #" + Math.abs(orderId) + "\nis already on the list");
					return false;
				}

				BigDecimal actualOfThisOrder = order.getEnteredTotal();
				BigDecimal payment = remit.getPayment(series, orderId);
				BigDecimal orderRevenue = actualOfThisOrder.subtract(payment);
				int firstItemId = new OrderHelper(orderId).getFirstLineItemId(series);
				String shortId = new ItemHelper().getShortId(firstItemId);
				if (shortId != null && shortId.equals("OR")) {
					if (orderId < 0 && actualOfThisOrder.equals(orderRevenue)) {
						orderRevenue = actualOfThisOrder.abs();
					} else if (orderId < 0 && payment.signum() == 1) {
						orderRevenue = actualOfThisOrder;
					}
				}

				// check if invoice has balance
				if (actualOfThisOrder.signum() == 0) {
					// check if invoice has actual amount inputed
					new ErrorDialog(orderType + " #" + Math.abs(orderId) + "\nhas no actual amount saved.\n"
					        + "Fill the datum in, then continue.");
					return false;
				} else if (orderRevenue.signum() == 0) {
					// check if invoice has been used
					String error = orderType + " #" + Math.abs(orderId) + "\nhas been fully paid ";
					if (actualOfThisOrder.signum() == -1)
						error = "Negative invoice #" + orderId + "\n" + "has been fully used up";
					String strRemitIdOfPaidOrderId = "";
					Integer[] remitIds = remit.getRemitIds(orderId);
					for (int i = 0; i < remitIds.length; i++) {
						strRemitIdOfPaidOrderId += (remitIds[i] + "\n");
					}
					error += "per Remittance #/s\n" + strRemitIdOfPaidOrderId;
					new ErrorDialog(error);
					return false;
				} else {
					int customerId = order.getPartnerId();
					Date postDate = order.getDate();
					int term = new Credit().getTerm(customerId, postDate);
					Date dueDate = new DateAdder(postDate).plus(term);
					BigDecimal totalPayment = remit.getEnteredTotal();
					BigDecimal revenueSubtotal = remit.getRevenueSubtotal().add(orderRevenue);
					BigDecimal paymentSubtotal = remit.getPaymentSubtotal();
					BigDecimal orderPayment = orderRevenue;
					if (revenueSubtotal.compareTo(totalPayment) > 0)
						orderPayment = totalPayment.subtract(paymentSubtotal);
					paymentSubtotal = paymentSubtotal.add(orderPayment);
					tableItem.setText(2, DIS.NO_COMMA_INTEGER.format(orderId));
					tableItem.setText(3, String.valueOf(customerId));
					tableItem.setText(4, customer.getName(customerId));
					tableItem.setText(5, DIS.POSTGRES_DATE.format(postDate));
					tableItem.setText(6, DIS.POSTGRES_DATE.format(dueDate));
					tableItem.setText(7, DIS.NO_COMMA_DECIMAL.format(orderRevenue));
					tableItem.setText(8, DIS.NO_COMMA_DECIMAL.format(orderPayment));
					BigDecimal balance = totalPayment.subtract(revenueSubtotal);
					txtBalance.setText(DIS.NO_COMMA_DECIMAL.format(balance));
					idInput.dispose();
					if (balance.abs().compareTo(BigDecimal.ONE) <= 0 && postButton != null) {
						postButton.setEnabled(true);
					}
					if (balance.signum() == -1) {
						txtBalance.setForeground(UI.RED);
						new ErrorDialog(""
								+ "Receivables' running total\n"
						        + "exceeded deposited/check amount.\n"
						        + "If this is a partial payment, save;\n"
						        + "else balance them.");
						remit.getOrderIds().add(rowIdx, orderId);
						remit.getSeriesList().add(rowIdx, series);
						remit.getPayments().add(rowIdx++, orderPayment);
						remit.setRevenueSubtotal(revenueSubtotal);
						remit.setPaymentSubtotal(paymentSubtotal);
						setNext(postButton);
						return true;
					}
					remit.getOrderIds().add(rowIdx, orderId);
					remit.getSeriesList().add(rowIdx, series);
					remit.getPayments().add(rowIdx++, orderPayment);
					remit.setRevenueSubtotal(revenueSubtotal);
					remit.setPaymentSubtotal(paymentSubtotal);
					tableItem = new TableItem(table, SWT.NO_TRIM, rowIdx);
					tableItem.setBackground(rowIdx % 2 == 0 ? UI.WHITE : UI.GRAY);
					if (rowIdx > 9)
						table.setTopIndex(rowIdx - 9);
					setSeries();
					setNext(txtSeries);
				}
				idInput.dispose();
				return true;
			}
		};
	}

	@Override
	protected void setFocus() {
		partnerIdInput.setTouchEnabled(true);
		partnerIdInput.setFocus();
		listButton.setEnabled(true);
	}

	public Text getTxtBalance() {
		return txtBalance;
	}

	public Text getTimeInput() {
		return timeInput;
	}

	public Text getTxtOrderId() {
		return idInput;
	}

	public Text getTxtOrId() {
		return txtOrId;
	}

	public Text getTxtTotalPayment() {
		return txtTotalPayment;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("badette", "013094", "192.168.1.100");
		//Database.getInstance().getConnection("badette", "013094", "localhost");
		Login.setUser("badette");
		Login.setGroup("user_sales");
		new RemittanceView(0);
		Database.getInstance().closeConnection();
	}
}

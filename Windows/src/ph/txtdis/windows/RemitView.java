package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class RemitView extends OrderView {
	private boolean isItemARemit, isRemitACheck;
	private int orderId, partnerId;
	private Date date;
	private OrderData order;
	private RemitData remit;
	private String series, bank;
	private TableItem tableItem;
	private Text timeInput, idInput, txtSeries, txtBalance, txtTotalPayment, txtOrId;
	private Time time;
	private Type orderType;

	public RemitView() {
		this(0);
	}

	public RemitView(int id) {
		this(new RemitData(0));
    }

	public RemitView(RemitData remit) {
		super(remit);
		type = Type.REMIT;
		display();
	}

	@Override
	protected void addHeader() {
		new Header(this, remit) {
			@Override
			protected void layButtons() {
				if (User.isFinance()) {
					if (id > 0 && remit.isPaymentByCheck(id))
						new BouncedCheckButton(buttons, remit).getButton();
					else if (partnerId == 0)
						new RemitImportButton(buttons, module).getButton();
					else
						new ImgButton(buttons, Type.EXCEL, view);
				}
				new ImageButton(buttons, module, "Transmittal", "Show latest transmittal") {
					@Override
					protected void proceed() {
						new RemitData(remit.getLatestDate());
					}
				};
				if (module.equals("Remittance"))
					new ImgButton(buttons, Type.NEW, type);
				new BackwardButton(buttons, data);
				new ImgButton(buttons, Type.OPEN, view);
				new ForwardButton(buttons, data);
				if (id == 0)
					postButton = new ImgButton(buttons, Type.SAVE, view).getButton();
			};
		};
	}

	@Override
	protected void addSubheader() {
		Composite info = new Compo(shell, 3, GridData.HORIZONTAL_ALIGN_FILL).getComposite();

		Group bank = new Grp(info, 3, "BANK INFORMATION", GridData.FILL_HORIZONTAL).getGroup();
		partnerId = remit.getPartnerId();
		partnerIdInput = new TextInputBox(bank, "ID #", partnerId).getText();
		listButton = new ListButton(bank, "Bank List").getButton();
		partnerDisplay = new TextDisplayBox(bank, "NAME", Customer.getName(partnerId), 2).getText();
		listButton.setEnabled(false);

		Group dateTime = new Grp(info, 2, "TIMESTAMP", GridData.HORIZONTAL_ALIGN_END).getGroup();
		timeInput = new TextInputBox(dateTime, "TIME", remit.getTime()).getText();
		dateInput = new TextInputBox(dateTime, "DATE", remit.getDate()).getText();

		Group invoice = new Grp(info, 4, "DETAILS", GridData.HORIZONTAL_ALIGN_END).getGroup();
		referenceIdInput = new TextInputBox(invoice, "REF ID", remit.getReferenceId()).getText();
		idInput = new TextDisplayBox(invoice, "REMIT ID", remit.getId()).getText();
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
	protected void addListener() {
		new DataInputter(partnerIdInput, timeInput) {
			@Override
			protected Boolean isPositive() {
				partnerId = number.intValue();
				try {
					bank = Customer.getBankName(partnerId);
				} catch (Exception e) {
					new ErrorDialog("Sorry, Bank ID " + partnerId + "\nis not in our system.");
					return false;
				}

				if (isCashierButNotFinance() || isNotCashierButIsFinance()) {
					new ErrorDialog("Sorry, you're not authorized\n to transact with\n" + bank);
					return false;
				}
				partnerDisplay.setText(bank);
				listButton.setEnabled(false);
				remit.setPartnerId(partnerId);
				return true;
			}
		};

		new DataInputter(timeInput, dateInput) {
			
			@Override
            protected Boolean isNonBlank() {
				time = DIS.parseTime(textInput);
				isRemitACheck = time.equals(DIS.ZERO_TIME);
				if (isRemitACheck && partnerId == DIS.BRANCH_CASHIER)
					return returnFalseOnNonCashDepositedToBranchCashier();
				remit.setTime(time);
				return true;
			}
		};

		new DataInputter(dateInput, referenceIdInput) {
			@Override
			protected Boolean isNonBlank() {
				Date date = DIS.parseDate(textInput);
				remit.setDate(date);
				return true;
			}
		};

		new DataInputter(referenceIdInput, txtTotalPayment) {
			@Override
			protected Boolean isPositive() {
				int referenceId = number.intValue();
				id = remit.getId(partnerId, date, time, referenceId);
				if (id != 0) {
					String document = isRemitACheck ? "Check #" : "Deposit Slip #";
					new ErrorDialog("Sorry, " + document + referenceId + "\nhas been used in Remittance #" + id);
					return false;
				}
				remit.setReferenceId(referenceId);
				return true;
			}
		};

		new DataInputter(txtTotalPayment, txtOrId) {
			@Override
			protected Boolean isPositive() {
				remit.setEnteredTotal(number);
				return true;
			}
		};

		new DataInputter(txtOrId, txtSeries) {
			@Override
			protected Boolean isPositive() {
				remit.setReceiptId(number.intValue());
				tableItem = new TableItem(table, SWT.NO_TRIM, rowIdx);
				setSeries();
				return true;
			}
		};
	}

	private void setSeries() {
		txtSeries = new TableTextInput(tableItem, 1, "").getText();
		series = null;
		txtSeries.setTouchEnabled(true);
		txtSeries.setFocus();
		new DataInputter(txtSeries, idInput) {
			

			@Override
            protected Boolean isBlankNot() {
				series = " ";
	            return null;
            }

			@Override
            protected Boolean isNonBlank() {
				series = textInput;
				isItemARemit = series.equals("R");

				if (isItemARemit && partnerId == DIS.BRANCH_CASHIER)
					return returnFalseOnNonCashDepositedToBranchCashier();

				if (isItemARemit || OrderControl.isOnFile(series)) {
					txtSeries.dispose();
					tableItem.setText(0, String.valueOf(rowIdx + 1));
					tableItem.setText(1, series);
					remit.setSeries(series);
					setOrderIdInput();
					return true;
				}
				new ErrorDialog("Booklet Series " + series + "\nhas yet to be issued");
				return false;
            }
		};
	}

	private boolean returnFalseOnNonCashDepositedToBranchCashier() {
		new ErrorDialog("Sorry, only cash can be remitted to\n" + bank);
		return false;
	}

	// Item Invoice/DR # input listener
	private void setOrderIdInput() {
		idInput = new TableTextInput(tableItem, 2, BigDecimal.ZERO).getText();
		idInput.setTouchEnabled(true);
		idInput.setFocus();
		orderId = 0;
		new DataInputter(idInput, postButton) {
			@Override
            protected Boolean isNegativeNot() {
				if (isItemARemit) {
					new ErrorDialog("Enter positive integers for\nRemittances");
					return false;
				}
				orderId = number.intValue();
				orderType = Type.DELIVERY;
				order = new DeliveryData(-orderId);
				series = null;
				return null;
            }

			@Override
            protected Boolean isPositive() {
				orderId = number.intValue();
				if (isItemARemit) {
					orderType = Type.REMIT;
					order = new RemitData(orderId);
				} else {
					orderType = Type.INVOICE;
					order = new InvoiceData(orderId, series);
				}
				return null;
            }

			@Override
            protected boolean isAnyNonZero() {
				int absoluteOrderId = Math.abs(orderId);
				String orderTypeAndId = orderType.getName() + " #" + absoluteOrderId;
				if (order == null) {
					new ErrorDialog(orderTypeAndId + "\nis not in our system");
					return false;
				} else if (remit.getOrderIds().contains(orderId)) {
					new ErrorDialog(orderTypeAndId + "\nis already on the list");
					return false;
				}

				BigDecimal actualOfThisOrder = order.getEnteredTotal();
				BigDecimal payment = remit.getPayment(series, orderId);
				BigDecimal orderRevenue = actualOfThisOrder.subtract(payment);
				int firstItemId = OrderControl.getFirstLineItemId(orderId, series);
				String shortId = Item.getShortId(firstItemId);
				if (shortId != null && shortId.equals("OR")) {
					if (orderId < 0 && actualOfThisOrder.equals(orderRevenue)) {
						orderRevenue = actualOfThisOrder.abs();
					} else if (orderId < 0 && payment.signum() == 1) {
						orderRevenue = actualOfThisOrder;
					}
				}

				if (isInvoiceHaveActualAmountInputted(actualOfThisOrder)) {
					new ErrorDialog(orderType + " #" + Math.abs(orderId) + "\nhas no actual amount saved.\n"
					        + "Fill the datum in, then continue.");
					return false;
				} else if (isInvoiceHaveActualAmountInputted(orderRevenue)) {
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
					int term = Credit.getTerm(customerId, postDate);
					Date dueDate = DIS.addDays(postDate, term);
					BigDecimal totalPayment = remit.getEnteredTotal();
					BigDecimal revenueSubtotal = remit.getRevenueSubtotal().add(orderRevenue);
					BigDecimal paymentSubtotal = remit.getPaymentSubtotal();
					BigDecimal orderPayment = orderRevenue;
					if (revenueSubtotal.compareTo(totalPayment) > 0)
						orderPayment = totalPayment.subtract(paymentSubtotal);
					paymentSubtotal = paymentSubtotal.add(orderPayment);
					tableItem.setText(2, DIS.NO_COMMA_INTEGER.format(orderId));
					tableItem.setText(3, String.valueOf(customerId));
					tableItem.setText(4, Customer.getName(customerId));
					tableItem.setText(5, postDate.toString());
					tableItem.setText(6, dueDate.toString());
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
						new ErrorDialog("Receivables' running total\nexceeded deposited/check amount.\n"
						        + "If this is a partial payment, save;\nelse balance them.");
						remit.getOrderIds().add(rowIdx, orderId);
						remit.getSeriesList().add(rowIdx, series);
						remit.getPayments().add(rowIdx++, orderPayment);
						remit.setRevenueSubtotal(revenueSubtotal);
						remit.setPaymentSubtotal(paymentSubtotal);//
						setNext(postButton);
						return true;
					}
					remit.getOrderIds().add(rowIdx, orderId);
					remit.getSeriesList().add(rowIdx, series == null ? " " : series);
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

			private boolean isInvoiceHaveActualAmountInputted(BigDecimal actualOfThisOrder) {
				return actualOfThisOrder.signum() == 0;
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

	private boolean isNotCashierButIsFinance() {
		return partnerId != DIS.MAIN_CASHIER && User.isFinance();
	}

	private boolean isCashierButNotFinance() {
		return partnerId == DIS.MAIN_CASHIER && !User.isFinance();
	}

	@Override
    public Posting getPosting() {
	    return new RemitPosting(remit);
    }
}

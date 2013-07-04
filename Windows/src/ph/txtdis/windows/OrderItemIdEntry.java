package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class OrderItemIdEntry {
	private ItemHelper item;
	private OrderHelper orderHlp;
	private CustomerHelper customer;
	private InvoiceLineItem lineItem;
	private Button btnPost;
	private Order order;
	private TableItem tableItem;
	private Text txtItemId;
	private String itemName;
	private Date postDate;
	private int itemId, partnerId, rowIdx;
	private boolean isSO, isDisposal, isRMA, isMonetaryTransaction;
	private boolean isExTruckRoute, isInternalCustomerOrOthers;

	public OrderItemIdEntry(final OrderView view,
			final InvoiceLineItem lineItem, final Order order) {
		this.lineItem = lineItem;
		this.order = order;
		item = new ItemHelper();
		tableItem = lineItem.getTableItem();
		btnPost = view.getBtnPost();
		txtItemId = lineItem.getTxtItemId();
		new IntegerVerifier(txtItemId);
		txtItemId.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event ev) {
				BigDecimal actual = order.getActual();
				String module = order.getModule();
				Text txtLimit = view.getTxtActual();
				boolean isPO = module.equals("Purchase Order");
				boolean isDR = module.equals("Delivery Report");
				boolean isSI = module.equals("Invoice");
				orderHlp = new OrderHelper();
				rowIdx = lineItem.getRow();
				postDate = order.getPostDate();
				partnerId = order.getPartnerId();
				isRMA = false;
				isSO = module.equals("Sales Order");
				isDisposal = view.getTxtPartnerName().getText().trim()
						.equals("BO DISPOSAL");
				customer = new CustomerHelper();
				isExTruckRoute = customer.isExTruck(partnerId);
				isInternalCustomerOrOthers = customer
						.isInternalOrOthers(partnerId);

				if (StringUtils.isBlank(txtItemId.getText())) {
					BigDecimal sumTotal = order.getSumTotal();
					if (rowIdx == 0 && actual.compareTo(BigDecimal.ZERO) > 0)
						return;
					// enable posting if difference between actual & computed <=
					// 1
					if (btnPost != null
							&& (actual.subtract(sumTotal).abs()
									.compareTo(BigDecimal.ONE) < 1
									|| isInternalCustomerOrOthers
									|| isRMA
									|| isSO || isPO)) {
						btnPost.setEnabled(true);
						btnPost.setFocus();
					}
				} else {
					itemId = Integer.parseInt(txtItemId.getText());
					isMonetaryTransaction = item.isMonetaryType(itemId);
					if (!isDR
							&& isMonetaryTransaction
							&& !(isSI && item.getName(itemId).equals(
									"DEALERS' INCENTIVE"))) {
						clearEntry("EWTs, PCVs or O/Rs"
								+ "\nmust be entered only on D/Rs");
						return;
					}
					if (isDR && isMonetaryTransaction
							&& actual.compareTo(BigDecimal.ZERO) >= 1) {
						clearEntry("EWTs, PCVs or O/Rs"
								+ "\n must have negative actuals");
						return;
					}
					if (isDR && !isMonetaryTransaction
							&& actual.compareTo(BigDecimal.ZERO) < 1) {
						clearEntry("Negative actuals are for\n"
								+ "EWTs, PCVs or O/Rs only");
						return;
					}
					if (itemId < 0) {
						isRMA = true;
						// ensure RMA is only done in an S/O
						if (!isSO && !isPO) {
							clearEntry("RMA must be imported\n"
									+ "from an approved S/O or P/O");
							return;
						}
						if (isSO && rowIdx == 0) {
							// check for open RMA
							int openRMA = orderHlp.getOpenRMA(partnerId);
							if (openRMA != 0) {
								new ErrorDialog("" + "S/O #" + openRMA + ""
										+ "\nmust be closed first "
										+ "\nbefore opening a new RMA");
								btnPost.getShell().dispose();
								new SalesOrderView(openRMA);
								return;
							}
							order.setActual(orderHlp
									.getReturnedMaterialBalance(partnerId,
											order.getPostDate()));
							txtLimit.setEnabled(true);
							txtLimit.setText("" + order.getActual());
							new InfoDialog("actual: "
									+ DIS.LNF.format(order.getActual()));
							// ensure RMA done separately per S/O
						} else if (isSO && actual.equals(BigDecimal.ZERO)) {
							clearEntry("RMA must be\ndone separately");
							return;
						}
						lineItem.setReturnedMaterial(true);
						itemId = Math.abs(itemId);
					} else { // transaction is not RMA
						if ((isSO || isPO) && rowIdx != 0
								&& !actual.equals(BigDecimal.ZERO)) {
							clearEntry("RMA must be\ndone separately");
							return;
						} else {
							lineItem.setReturnedMaterial(false);
						}
					}
					if (isSO && isExTruckRoute && rowIdx != 0) {
						int lastItemId = order.getItemIds().get(rowIdx - 1);
						// int lastItemBizUnitId = item.

					}
					if (hasDatum()) {
						txtItemId.setEditable(true);
						txtItemId.setBackground(View.yellow());
						return;
					}
					tableItem.setText(2, item.getName(itemId));
					next();
				}
			}
		});
	}

	private boolean hasDatum() {
		itemName = item.getName(itemId);
		if (itemName == null) {
			clearEntry("Item ID " + itemId + "\nis not in our system");
			return true;
		}

		// Check for aging A/R
		if (!new Overdue(partnerId, DIS.OVERDUE_CUTOFF).getBalance().equals(
				BigDecimal.ZERO)) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					new OverdueView(partnerId, DIS.OVERDUE_CUTOFF);
				}
			});
			new InfoDialog("" + "Click the PHONE icon to request\n"
					+ "approval to deliver to\n" + customer.getName(partnerId)
					+ "\n" + "today and/or tomorrow;\n"
					+ "You may click the PRINTER button\n"
					+ "if you want copy of the outlet's A/R" + "");
			txtItemId.setText("");
			return true;
		}

		// check if item has been previously entered
		if (order.getItemIds().contains(Math.abs(itemId))) {
			clearEntry(itemName + "\nis already on the list");
			return true;
		}

		Object[] soIdWithSameItemDiscountGroup = orderHlp
				.getSoIdAndItemDiscountGroup(itemId, partnerId, postDate);
		if (isSO && soIdWithSameItemDiscountGroup != null) {
			clearEntry("Item ID " + itemId + "\nis already in\nS/O #"
					+ soIdWithSameItemDiscountGroup);
			return true;
		}

		// check if item has price in the system
		BigDecimal unitPrice;
		if (isMonetaryTransaction) {
			if (rowIdx != 0) {
				clearEntry("EWTs, PCVs or O/Rs\nmust be done separately");
				return true;
			}
			unitPrice = new BigDecimal(-1);
		} else {
			unitPrice = new Price().get(itemId, partnerId, postDate);
		}
		if (unitPrice == null) {
			clearEntry("Item #" + itemId + "\nhas no price in our system");
			return true;
		}
		// check if there are available stocks when making an S/O
		BigDecimal goodQty = item.getAvailableStock(itemId);
		BigDecimal badQty = item.getBadStock(itemId);
		if (isSO) {
			if (isDisposal && badQty.compareTo(BigDecimal.ZERO) <= 0) {
				clearEntry("No " + itemName + "\n for disposal;\n"
						+ "go to Inventory Module for details ");
				return true;
			} else if (!isRMA && !isMonetaryTransaction && !isDisposal
					&& goodQty.compareTo(BigDecimal.ZERO) <= 0) {
				clearEntry("No bookable\n" + itemName + ";\n"
						+ "go to Inventory Module for details ");
				return true;
			}
		}
		lineItem.setQty(goodQty);
		lineItem.setItemId(itemId);
		if (isMonetaryTransaction) {
			lineItem.setUoms(new String[] { "₱" });
		} else {
			lineItem.setUoms(new UOM().getSoldUoms(itemId));
		}
		lineItem.setUnitPrice(unitPrice);
		return false;
	}

	private void next() {
		lineItem.getBtnItemId().dispose();
		btnPost.setEnabled(false);
		lineItem.setItemName(item.getName(itemId));
		Combo cmbUom = lineItem.getCmbUnit();
		// show unit price (column 5)
		tableItem.setText(5, DIS.LNF.format(lineItem.getUnitPrice()));
		// move to cmbUnit
		cmbUom.setEnabled(true);
		cmbUom.setFocus();
		cmbUom.setItems(lineItem.getUoms());
		cmbUom.select(0);
		// get discount per customer & product line
		PartnerDiscount discount = new PartnerDiscount(partnerId, itemId,
				postDate);
		order.setDiscountRate1(discount.getRate1());
		order.setDiscountRate2(discount.getRate2());
	}

	protected void clearEntry(String msg) {
		new ErrorDialog(msg);
		txtItemId.setText("");
		tableItem.setText(2, "");
		txtItemId.setEditable(true);
		txtItemId.setBackground(View.yellow());
	}
}

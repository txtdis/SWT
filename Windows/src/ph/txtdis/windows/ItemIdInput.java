package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public abstract class ItemIdInput {
	private int rowIdx;
	private boolean isAnSI;
	private BigDecimal price;
	private Button postButton, itemListButton;
	private Combo uomCombo;
	private Date date;
	private String salesType;
	private Text itemIdInput;

	protected boolean isForAnExTruck, isAMonetaryTransaction, isEnteredTotalNegative, isAtFirstRow;
	protected int partnerId, itemId;
	protected ItemHelper item;
	protected Order order;
	protected OrderView orderView;
	protected PartnerDiscount discount;
	protected String itemName;
	protected TableItem tableItem;
	protected Text txtLimit;

	public ItemIdInput(OrderView view, Order report) {
		order = report;
		orderView = view;
		postButton = orderView.getPostButton();
		txtLimit = orderView.getTxtEnteredTotal();

		rowIdx = order.getRowIdx();
		tableItem = orderView.getTableItem(rowIdx);
		itemListButton = new TableButton(tableItem, rowIdx, 0, "Item List").getButton();
		itemIdInput = new TableTextInput(tableItem, rowIdx, 1, 0).getText();
		itemIdInput.setText(tableItem.getText(1));
		itemIdInput.setFocus();

		orderView.setItemIdInput(itemIdInput);
		orderView.setTableListButton(itemListButton);

		item = new ItemHelper();

		partnerId = order.getPartnerId();
		date = order.getDate();
		price = order.getPrice();
		isAtFirstRow = rowIdx == 0;
		isEnteredTotalNegative = order.getEnteredTotal().signum() == -1;
		isForAnExTruck = order.isForAnExTruck();
		isAnSI = order.isAnSI();
		salesType = isAnSI ? "S/I" : "S/O";

		new TextInputter(itemIdInput, uomCombo) {
			@Override
			protected boolean isTheNegativeNumberNotValid() {
				if (!isNegativeItemIdInputValid()) {
					shouldReturn = true;
					return true;
				}
				shouldReturn = false;
				return false;
			}

			@Override
			protected boolean isTheSignedNumberValid() {
				itemId = numericInput.intValue();
				if (hasItemBeenEnteredBefore())
					return false;

				if (!isItemOnFile())
					return false;

				if (!isItemMonetaryAndTransactionValid())
					return false;

				if (isAtFirstRow && arePartnerReceivablesAging() && date.after(DIS.NO_SO_WITH_OVERDUE_CUTOFF))
					return false;

				discount = new PartnerDiscount(partnerId, Math.abs(itemId), date);
				BigDecimal discount1 = discount.getFirstLevel();
				BigDecimal discount2 = discount.getSecondLevel();
				order.setDiscount1Percent(discount1);
				order.setDiscount2Percent(discount2);
				if (isAtFirstRow) {
					order.setTotalDiscountRate(discount.getTotal());
					// if (isItemDiscountSameAsFromSameDayOrders())
					// return false;
				}

				if (!isItemDiscountSameAsPrevious())
					return false;

				if (!isItemBizUnitSameAsPrevious())
					return false;

				if (!isItemOnReferenceOrder())
					return false;

				if (!doesItemHavePrice())
					return false;

				postButton.setEnabled(false);
				itemListButton.dispose();
				order.setItemId(itemId);

				tableItem.setText(0, String.valueOf(rowIdx + 1));
				tableItem.setText(orderView.ITEM_ID_COLUMN, textInput);
				tableItem.setText(orderView.ITEM_COLUMN, itemName);
				if (price != null) {
					DIS.formatTo2Places(price);
					computeTotals(tableItem.getText(orderView.TOTAL_COLUMN));
					tableItem.setText(orderView.TOTAL_COLUMN, "");
				}
				itemIdInput.dispose();
				setNextTableWidget(price);
				return true;
			}
		};
	}

	private void computeTotals(String subtotal) {
		order.recomputeTotals(subtotal);
		orderView.getComputedTotalDisplay().setText(DIS.formatTo2Places(order.getComputedTotal()));
		orderView.getTxtTotalVatable().setText(DIS.formatTo2Places(order.getTotalVatable()));
		orderView.getTxtTotalVat().setText(DIS.formatTo2Places(order.getTotalVat()));
		orderView.getDiscount1Display().getText().setText(DIS.formatTo2Places(order.getDiscount1Total()));
		orderView.getDiscount2Display().getText().setText(DIS.formatTo2Places(order.getDiscount2Total()));
	}

	protected boolean isItemBizUnitSameAsPrevious() {
		ArrayList<String> bizUnits = order.getBizUnits();
		String bizUnit = item.getBizUnit(itemId);
		if (bizUnits.isEmpty()) {
			bizUnits.add(bizUnit);
			return true;
		}

		if (isAtFirstRow) {
			bizUnits.set(0, bizUnit);
			return true;
		}

		int bizUnitSize = bizUnits.size();
		final int last = bizUnitSize - 1;
		final int beforeLast = bizUnitSize - 2;
		if (bizUnits.get(last).equals(bizUnit))
			return true;

		int bizUnitLastOccurance = bizUnits.subList(0, last).indexOf(bizUnit);
		if (bizUnitLastOccurance < 0) {
			String previousBizUnits = "";
			String conjunction = " ";
			for (int i = 0; i < bizUnitSize; i++) {
				if (i == beforeLast) {
					conjunction = " and\n";
				} else if (i != last) {
					conjunction = ",\n";
				}
				previousBizUnits += (bizUnits.get(i) + conjunction);
			}
			new InfoDialog("A/n " + bizUnit + "\nitem was just added;\n" + previousBizUnits
			        + "\ncan no longer be entered after this");
			bizUnits.add(bizUnit);
			return true;
		}

		clearTableItemEntries(bizUnit + "\nhas been entered before;\nstarting at line #" + (bizUnitLastOccurance + 1));
		return false;
	}

	protected boolean isItemDiscountSameAsFromSameDayOrders() {
		BigDecimal newItemDiscount = discount.getTotal();
		if (isForAnExTruck && !(isAnSI && order.isForAnExTruck()) && order.isAnRMA())
			return false;

		int orderIdWithSameDiscount = order.getIdWithSameDiscount(itemId);
		if (orderIdWithSameDiscount == 0) {
			// order.setTotalDiscountRate(newItemDiscount);
			return false;
		}

		clearTableItemEntries("One " + salesType + " per discount rate per outlet per day:\n" + itemName
		        + "\nis discounted " + DIS.formatTo2Places(newItemDiscount) + "%, the same as items in " + salesType
		        + " #" + orderIdWithSameDiscount);
		orderView.getShell().dispose();
		if (isAnSI)
			new InvoiceView(orderIdWithSameDiscount);
		else
			new SalesOrderView(orderIdWithSameDiscount);
		return true;
	}

	protected boolean isItemDiscountSameAsPrevious() {
		BigDecimal currentDiscount = order.getTotalDiscountRate();
		BigDecimal newItemDiscount = discount.getTotal();
		if (!isForAnExTruck && !order.isAnRMA()
		// && (isAnSI && order.isForAnExTruck())
		        && currentDiscount.compareTo(newItemDiscount) != 0) {
			clearTableItemEntries("One " + salesType + " per discount rate per outlet per day:\n" + itemName
			        + "\nis discounted " + DIS.formatTo2Places(newItemDiscount) + "%; other items in this " + salesType
			        + " have " + DIS.formatTo2Places(currentDiscount));
			return false;
		}
		return true;
	}

	protected boolean hasItemBeenEnteredBefore() {
		ArrayList<Integer> itemIds = order.getItemIds();
		if (itemIds.isEmpty())
			return false;
		int lineIdWithItemid = itemIds.indexOf(itemId);
		if (lineIdWithItemid > -1 && lineIdWithItemid != rowIdx) {
			clearTableItemEntries("Item # " + itemId + "\nis already on line #" + (lineIdWithItemid + 1));
			return true;
		}
		return false;
	}

	protected boolean isNegativeItemIdInputValid() {
		return false;
	}

	protected boolean isItemMonetaryAndTransactionValid() {
		return true;
	}

	protected void clearTableItemEntries(String msg) {
		new ErrorDialog(msg);
		if (tableItem != null) {
			tableItem.setText(orderView.ITEM_COLUMN, "");
			tableItem.setText(orderView.UOM_COLUMN, "");
			tableItem.setText(orderView.PRICE_COLUMN, "");
			tableItem.setText(orderView.QTY_COLUMN, "");
			if (!order.isAnRR)
				computeTotals(tableItem.getText(orderView.TOTAL_COLUMN));
			itemIdInput.setText("");
		}
	}

	private boolean arePartnerReceivablesAging() {
		if (order.isAnRMA())
			return false;
		if (order.getOverdue().compareTo(BigDecimal.ONE) > 0) {
			new OverdueView(partnerId, DIS.NO_SO_WITH_OVERDUE_CUTOFF);
			return true;
		}
		return false;
	}

	protected void setNextTableWidget(BigDecimal input) {
		String uom = DIS.CURRENCY_SIGN;
		if (isAMonetaryTransaction) {
			tableItem.setText(orderView.UOM_COLUMN, uom);
			order.setUomId(new UOM(uom).getId());
			price = itemId == DIS.SALARY_CREDIT ? BigDecimal.ONE : BigDecimal.ONE.negate();
			new OrderItemQtyInput(orderView, order);
			return;
		}
		String[] uoms = new UOM().getSellingUoms(Math.abs(itemId));
		if (uoms == null)
			return;
		if (uoms.length == 1 && !order.isAnRR()) {
			uom = uoms[0];
			BigDecimal qtyPerUOM = new QtyPerUOM().getQty(Math.abs(itemId), uom);
			order.setUomId(new UOM(uom).getId());
			tableItem.setText(orderView.UOM_COLUMN, uom);
			if (price != null) {
				order.setPrice(price.multiply(qtyPerUOM));
				tableItem.setText(orderView.PRICE_COLUMN, DIS.formatTo2Places(price));
			}
			new OrderItemQtyInput(orderView, order);
			return;
		}
		order.setUoms(uoms);
		new OrderItemUomCombo(orderView, order);
	}

	protected boolean isItemOnFile() {
		itemName = item.getName(Math.abs(itemId));
		if (itemName.isEmpty()) {
			clearTableItemEntries("Item #" + itemId + "\nis not on file");
			return false;
		} else {
			return isItemMonetaryAndTransactionValid();
		}
	}

	protected boolean doesItemHavePrice() {
		if (price == null) {
			if (isAMonetaryTransaction) {
				price = itemId == DIS.SALARY_CREDIT ? BigDecimal.ONE : BigDecimal.ONE.negate();
			} else {
				price = new Price().get(Math.abs(itemId), partnerId, date);
				if (price.equals(BigDecimal.ZERO)) {
					clearTableItemEntries("Item #" + itemId + "\nhas no price for\n" + order.getPartner()
					        + "in our system");
					new ItemView(itemId);
					return false;
				}
				if (order.isAnRMA())
					price = price.negate();
				tableItem.setText(orderView.PRICE_COLUMN, DIS.formatTo2Places(price));
			}
		}
		order.setPrice(price);
		return true;
	}

	protected boolean isItemOnReferenceOrder() {
		int referenceId = order.getReferenceId();
		isAMonetaryTransaction = item.isMonetaryType(itemId, order.getType());
		if (!isAMonetaryTransaction && !order.isMaterialTransfer()) {
			BigDecimal referenceQty = item.getReferenceQty(itemId, referenceId);
			if (referenceQty.equals(BigDecimal.ZERO)) {
				String referenceType = order.isReferenceAnSO() ? "S/O" : "P/O";
				clearTableItemEntries(itemName + "\nis not in " + referenceType + " #" + Math.abs(referenceId));
				return false;
			}
			order.setReferenceQty(referenceQty);
		}
		return true;
	}

	protected void clearTableItemEntry(String msg) {
		new ErrorDialog(msg);
		if (tableItem == null)
			return;
		tableItem.setText(Order.ITEM_COLUMN, "");
		tableItem.setText(Order.UOM_COLUMN, "");
		tableItem.setText(Order.PRICE_COLUMN, "");
		tableItem.setText(order.getQtyColumnNo(), "");
		clearLineItemAndRecomputeTotals(tableItem.getText(Order.TOTAL_COLUMN));
		itemIdInput.setText("");
	}

	private void clearLineItemAndRecomputeTotals(String subTotalText) {
		if (subTotalText == null || subTotalText.trim().isEmpty())
			return;
		BigDecimal subTotal = new BigDecimal(subTotalText);
		BigDecimal discount1Total = subTotal.multiply(DIS.getRate(order.getDiscount1Percent()));
		subTotal = subTotal.subtract(discount1Total);
		BigDecimal discount2Total = subTotal.multiply(DIS.getRate(order.getDiscount2Percent()));
		subTotal = subTotal.subtract(discount2Total);
		BigDecimal vatable = DIS.getQuotient(subTotal, DIS.VAT);
		BigDecimal vat = subTotal.subtract(vatable);

		BigDecimal total = order.getComputedTotal().subtract(subTotal);
		vatable = order.getTotalVatable().subtract(vatable);
		vat = order.getTotalVat().subtract(vat);
		discount1Total = order.getDiscount1Total().subtract(discount1Total);
		discount2Total = order.getDiscount2Total().subtract(discount2Total);

		orderView.getComputedTotalDisplay().setText(DIS.formatTo2Places(total));
		orderView.getTxtTotalVatable().setText(DIS.formatTo2Places(vatable));
		orderView.getTxtTotalVat().setText(DIS.formatTo2Places(vat));
		orderView.getDiscount1Display().getText().setText(DIS.formatTo2Places(discount1Total));
		orderView.getDiscount2Display().getText().setText(DIS.formatTo2Places(discount2Total));

		order.setDiscount1Total(discount1Total);
		order.setDiscount2Total(discount2Total);
		order.setComputedTotal(total);
		order.setTotalVatable(vatable);
		order.setTotalVat(vat);

		tableItem.setText(Order.TOTAL_COLUMN, "");
	}
}

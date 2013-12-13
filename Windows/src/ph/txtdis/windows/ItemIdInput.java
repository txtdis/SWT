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

	protected boolean isForAnExTruck, isAMonetaryTransaction,
			isEnteredTotalNegative, isAtFirstRow;
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
		itemListButton = new TableButton(tableItem, rowIdx, 0, "Item List")
				.getButton();
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

				if (isAtFirstRow && arePartnerReceivablesAging()
						&& date.after(DIS.NO_SO_WITH_OVERDUE_CUTOFF))
					return false;

				discount = new PartnerDiscount(partnerId, Math.abs(itemId),
						date);
				BigDecimal discount1 = discount.getFirstLevel();
				BigDecimal discount2 = discount.getSecondLevel();
				order.setFirstLevelDiscount(discount1);
				order.setSecondLevelDiscount(discount2);
				if (isAtFirstRow) {
					order.setTotalDiscountRate(discount.getTotal());
//					if (isItemDiscountSameAsFromSameDayOrders())
//						return false;
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
							DIS.TWO_PLACE_DECIMAL.format(price);
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
		orderView.getComputedTotalDisplay().setText(
				DIS.TWO_PLACE_DECIMAL.format(order.getComputedTotal()));
		orderView.getTxtTotalVatable().setText(
				DIS.TWO_PLACE_DECIMAL.format(order.getTotalVatable()));
		orderView.getTxtTotalVat().setText(
				DIS.TWO_PLACE_DECIMAL.format(order.getTotalVat()));
		orderView
				.getFirstLevelDiscountBox()
				.getText()
				.setText(
						DIS.TWO_PLACE_DECIMAL.format(order
								.getFirstLevelDiscountTotal()));
		orderView
				.getSecondLevelDiscountBox()
				.getText()
				.setText(
						DIS.TWO_PLACE_DECIMAL.format(order
								.getSecondLevelDiscountTotal()));
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
			new InfoDialog("A/n " + bizUnit + "\nitem was just added;\n"
					+ previousBizUnits
					+ "\ncan no longer be entered after this");
			bizUnits.add(bizUnit);
			return true;
		}

		clearTableItemEntries(bizUnit
				+ "\nhas been entered before;\nstarting at line #"
				+ (bizUnitLastOccurance + 1));
		return false;
	}

	protected boolean isItemDiscountSameAsFromSameDayOrders() {
		BigDecimal newItemDiscount = discount.getTotal();
		if (isForAnExTruck && !(isAnSI && order.isForAnExTruck())
				&& order.isAnRMA())
			return false;

		int orderIdWithSameDiscount = order.getIdWithSameDiscount(itemId);
		if (orderIdWithSameDiscount == 0) {
			//order.setTotalDiscountRate(newItemDiscount);
			return false;
		}

		clearTableItemEntries("One " + salesType
				+ " per discount rate per outlet per day:\n" + itemName
				+ "\nis discounted "
				+ DIS.TWO_PLACE_DECIMAL.format(newItemDiscount)
				+ "%, the same as items in " + salesType + " #"
				+ orderIdWithSameDiscount);
		orderView.getShell().dispose();
		if (isAnSI)
			new InvoiceView(orderIdWithSameDiscount);
		else
			new SalesOrderView(orderIdWithSameDiscount);
		return true;
	}

	protected boolean isItemDiscountSameAsPrevious() {
		BigDecimal currentDiscount = order.getTotalDiscountRate();
		System.out.println("currentDiscount: " + currentDiscount);
		BigDecimal newItemDiscount = discount.getTotal();
		System.out.println("newItemDiscount: " + newItemDiscount);
		if (!isForAnExTruck && !order.isAnRMA()
				//&& (isAnSI && order.isForAnExTruck())
				&& currentDiscount.compareTo(newItemDiscount) != 0) {
			clearTableItemEntries("One " + salesType
					+ " per discount rate per outlet per day:\n" + itemName
					+ "\nis discounted "
					+ DIS.TWO_PLACE_DECIMAL.format(newItemDiscount)
					+ "%; other items in this " + salesType + " have "
					+ DIS.TWO_PLACE_DECIMAL.format(currentDiscount));
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
			clearTableItemEntries("Item # " + itemId + "\nis already on line #"
					+ (lineIdWithItemid + 1));
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

	protected void setNextTableWidget(BigDecimal price) {
		String uom = DIS.CURRENCY_SIGN;
		if (isAMonetaryTransaction) {
			tableItem.setText(orderView.UOM_COLUMN, uom);
			order.setUomId(new UOM(uom).getId());
			new OrderItemQtyInput(orderView, order);
			return;
		}
		String[] uoms = new UOM().getSellingUoms(Math.abs(itemId));
		System.out.println("RR? " + order.isAnRR());
		if (uoms == null)
			return;
		if (uoms.length == 1 && !order.isAnRR()) {
			uom = uoms[0];
			BigDecimal qtyPerUOM = new QtyPerUOM().getQty(Math.abs(itemId), uom);
			order.setUomId(new UOM(uom).getId());
			tableItem.setText(orderView.UOM_COLUMN, uom);
			if (price != null) {
			order.setPrice(price.multiply(qtyPerUOM));
			tableItem.setText(orderView.PRICE_COLUMN,
					DIS.TWO_PLACE_DECIMAL.format(price));
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
			isItemMonetaryAndTransactionValid();
			return true;
		}
	}

	protected boolean doesItemHavePrice() {
		// check if item has price in the system
		if (price == null) {
			if (isAMonetaryTransaction) {
				price = BigDecimal.ONE.negate();
			} else {
			price = new Price().get(Math.abs(itemId), partnerId, date);
			if (price.equals(BigDecimal.ZERO)) {
				clearTableItemEntries("Item #" + itemId
						+ "\nhas no price for\n" + order.getPartner()
						+ "in our system");
				new ItemView(itemId);
				return false;
			}
			if (order.isAnRMA())
				price = price.negate();
			}
		}
		order.setPrice(price);
		return true;
	}

	protected boolean isItemOnReferenceOrder() {
		int referenceId = order.getReferenceId();
		if (!isAMonetaryTransaction) {
			BigDecimal referenceQty = item.getReferenceQty(itemId, referenceId);
			if (referenceQty.equals(BigDecimal.ZERO)) {
				String referenceType = order.isReferenceAnSO() ? "S/O" : "P/O";
				clearTableItemEntries(itemName + "\nis not in " + referenceType
						+ " #" + Math.abs(referenceId));
				return false;
			}
			order.setReferenceQty(referenceQty);
		}
		return true;
	}

	protected void clearTableItemEntry(String msg) {
		new ErrorDialog(msg);
		if (tableItem != null) {
			tableItem.setText(Order.ITEM_COLUMN, "");
			tableItem.setText(Order.UOM_COLUMN, "");
			tableItem.setText(Order.PRICE_COLUMN, "");
			tableItem.setText(order.getQtyColumnNo(), "");
			clearLineItemAndRecomputeTotals(tableItem
					.getText(Order.TOTAL_COLUMN));
			itemIdInput.setText("");
		}
	}

	private void clearLineItemAndRecomputeTotals(String subTotalText) {
		if (!subTotalText.isEmpty()) {
			BigDecimal subTotal = new BigDecimal(subTotalText);
			BigDecimal firstLevelDiscountTotal = subTotal.multiply(order
					.getFirstLevelDiscountRate().divide(DIS.HUNDRED,
							BigDecimal.ROUND_HALF_EVEN));
			subTotal = subTotal.subtract(firstLevelDiscountTotal);
			BigDecimal secondLevelDiscountTotal = subTotal.multiply(order
					.getSecondLevelDiscountRate().divide(DIS.HUNDRED,
							BigDecimal.ROUND_HALF_EVEN));
			subTotal = subTotal.subtract(secondLevelDiscountTotal);
			BigDecimal vatable = subTotal.divide(DIS.VAT,
					BigDecimal.ROUND_HALF_EVEN);
			BigDecimal vat = subTotal.subtract(vatable);

			BigDecimal total = order.getComputedTotal().subtract(subTotal);
			vatable = order.getTotalVatable().subtract(vatable);
			vat = order.getTotalVat().subtract(vat);
			firstLevelDiscountTotal = order.getFirstLevelDiscountTotal()
					.subtract(firstLevelDiscountTotal);
			secondLevelDiscountTotal = order.getSecondLevelDiscountTotal()
					.subtract(secondLevelDiscountTotal);

			orderView.getComputedTotalDisplay().setText(
					DIS.TWO_PLACE_DECIMAL.format(total));
			orderView.getTxtTotalVatable().setText(
					DIS.TWO_PLACE_DECIMAL.format(vatable));
			orderView.getTxtTotalVat().setText(
					DIS.TWO_PLACE_DECIMAL.format(vat));
			orderView
					.getFirstLevelDiscountBox()
					.getText()
					.setText(
							DIS.TWO_PLACE_DECIMAL
									.format(firstLevelDiscountTotal));
			orderView
					.getSecondLevelDiscountBox()
					.getText()
					.setText(
							DIS.TWO_PLACE_DECIMAL
									.format(secondLevelDiscountTotal));

			order.setFirstLevelDiscountTotal(firstLevelDiscountTotal);
			order.setSecondLevelDiscountTotal(secondLevelDiscountTotal);
			order.setComputedTotal(total);
			order.setTotalVatable(vatable);
			order.setTotalVat(vat);

			tableItem.setText(Order.TOTAL_COLUMN, "");
		}
	}
}

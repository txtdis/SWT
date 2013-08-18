package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public abstract class ItemIdInput {
	private final BigDecimal VAT = Constant.getInstance().getVat();

	private int rowIdx;
	private BigDecimal price;
	private Button postButton, itemListButton;
	private Combo uomCombo;
	private Date date;
	private String referenceType;
	private Text itemIdInput;

	protected boolean isForAnExTruck, isAMonetaryTransaction, isEnteredTotalNegative, isAtFirstRow, isReferenceAnSO;
	protected int partnerId, referenceId, itemId;
	protected ItemHelper item;
	protected Order order;
	protected OrderView view;
	protected OrderHelper helper;
	protected PartnerDiscount discount;
	protected String itemName;
	protected TableItem tableItem;
	protected Text txtLimit;

	public ItemIdInput(OrderView orderView, Order report) {
		order = report;
		view = orderView;
		postButton = view.getPostButton();
		txtLimit = view.getTxtEnteredTotal();

		rowIdx = order.getRowIdx();
		tableItem = view.getTableItem(rowIdx);
		itemListButton = new TableButton(tableItem, rowIdx, 0, "Item List").getButton();
		itemIdInput = new TableTextInput(tableItem, rowIdx, 1, 0).getText();
		itemIdInput.setText(tableItem.getText(1));
		itemIdInput.setFocus();

		view.setItemIdInput(itemIdInput);
		view.setTableListButton(itemListButton);

		helper = new OrderHelper();
		item = new ItemHelper();

		partnerId = order.getPartnerId();
		date = order.getDate();
		price = order.getPrice();
		referenceId = order.getReferenceId();
		if (referenceId < 0) {
			referenceType = "P/O";
		} else {
			isReferenceAnSO = true;
			referenceType = "S/O";
		}
		order.setReferenceAnSO(isReferenceAnSO);
		isAtFirstRow = rowIdx == 0;
		isEnteredTotalNegative = order.getEnteredTotal().signum() == -1;
		isForAnExTruck = order.isForAnExTruck();

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
			protected boolean isThePositiveNumberValid() {
				itemId = numericInput.intValue();
				if (hasItemBeenEnteredBefore())
					return false;

				if (!isItemOnFile())
					return false;

				if (!isItemMonetaryAndTransactionValid())
					return false;

				if (isAtFirstRow && arePartnerReceivablesAging() && date.after(DIS.OVERDUE_CUTOFF))
					return false;

				discount = new PartnerDiscount(partnerId, Math.abs(itemId), date);
				order.setFirstLevelDiscount(discount.getFirstLevel());
				order.setSecondLevelDiscount(discount.getSecondLevel());
				if (isAtFirstRow) {
					order.setTotalDiscountRate(null);
					if (isItemDiscountSameAsFromSameDayOrders())
						return false;
				} else {
					if (!isItemDiscountSameAsPrevious())
						return false;
				}

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
				tableItem.setText(order.ITEM_ID_COLUMN, String.valueOf(itemId));
				tableItem.setText(order.ITEM_COLUMN, itemName);
				if (price != null) {
					tableItem.setText(order.PRICE_COLUMN, DIS.TWO_PLACE_DECIMAL.format(price));
					String subTotalText = tableItem.getText(order.TOTAL_COLUMN).replace(",", "").replace("(", "-")
					        .replace(")", "");
					clearLineItemAndRecomputeTotals(subTotalText);
				}
				itemIdInput.dispose();
				itemListButton.dispose();
				setNextTableWidget(price);
				return true;
			}
		};
	}

	private void clearLineItemAndRecomputeTotals(String subTotalText) {
		if (!subTotalText.isEmpty()) {
			BigDecimal subTotal = new BigDecimal(subTotalText);
			BigDecimal firstLevelDiscountTotal = subTotal.multiply(order.getFirstLevelDiscountRate().divide(
			        DIS.HUNDRED, BigDecimal.ROUND_HALF_EVEN));
			subTotal = subTotal.subtract(firstLevelDiscountTotal);
			BigDecimal secondLevelDiscountTotal = subTotal.multiply(order.getSecondLevelDiscountRate().divide(
			        DIS.HUNDRED, BigDecimal.ROUND_HALF_EVEN));
			subTotal = subTotal.subtract(secondLevelDiscountTotal);
			BigDecimal vatable = subTotal.divide(VAT, BigDecimal.ROUND_HALF_EVEN);
			BigDecimal vat = subTotal.subtract(vatable);

			BigDecimal total = order.getComputedTotal().subtract(subTotal);
			vatable = order.getTotalVatable().subtract(vatable);
			vat = order.getTotalVat().subtract(vat);
			firstLevelDiscountTotal = order.getFirstLevelDiscountTotal().subtract(firstLevelDiscountTotal);
			secondLevelDiscountTotal = order.getSecondLevelDiscountTotal().subtract(secondLevelDiscountTotal);

			view.getComputedTotalDisplay().setText(DIS.TWO_PLACE_DECIMAL.format(total));
			view.getTxtTotalVatable().setText(DIS.TWO_PLACE_DECIMAL.format(vatable));
			view.getTxtTotalVat().setText(DIS.TWO_PLACE_DECIMAL.format(vat));
			view.getFirstLevelDiscountBox().getText().setText(DIS.TWO_PLACE_DECIMAL.format(firstLevelDiscountTotal));
			view.getSecondLevelDiscountBox().getText().setText(DIS.TWO_PLACE_DECIMAL.format(secondLevelDiscountTotal));

			order.setFirstLevelDiscountTotal(firstLevelDiscountTotal);
			order.setSecondLevelDiscountTotal(secondLevelDiscountTotal);
			order.setComputedTotal(total);
			order.setTotalVatable(vatable);
			order.setTotalVat(vat);

			tableItem.setText(order.TOTAL_COLUMN, "");
		}
	}

	protected boolean isItemBizUnitSameAsPrevious() {
		ArrayList<String> bizUnits = order.getBizUnits();
		String currentBizUnit = item.getBizUnit(itemId);
		if (bizUnits.isEmpty()) {
			bizUnits.add(currentBizUnit);
		} else if (isAtFirstRow) {
			bizUnits.set(0, currentBizUnit);
		} else {
			int bizUnitSize = bizUnits.size();
			final int last = bizUnitSize - 1;
			final int beforeLast = bizUnitSize - 2;
			if (!bizUnits.get(last).equals(currentBizUnit)) {
				int currentBizUnitPreviousFirstOccurance = bizUnits.subList(0, last).indexOf(currentBizUnit);
				if (currentBizUnitPreviousFirstOccurance < 0) {
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
					new InfoDialog("A/n " + currentBizUnit + "\nitem was just added;\n" + previousBizUnits
					        + "\ncan no longer be entered after this");
					bizUnits.add(currentBizUnit);
				} else { // biz unit has been entered before last
					clearTableItemEntry(currentBizUnit + "\nhas been entered before;\nstarting at line #"
					        + (currentBizUnitPreviousFirstOccurance + 1));
					return false;
				}
			}
		}
		return true;
	}

	protected boolean isItemDiscountSameAsFromSameDayOrders() {
		BigDecimal newItemDiscount = discount.getTotal();
		if (!isForAnExTruck && !order.isAnRMA()) {
			int soIdWithSameDiscount = helper.getOrderIdWithSameDiscount(itemId, partnerId, date, order.getType());
			if (soIdWithSameDiscount != 0) {
				clearTableItemEntry("One S/O per discount rate per outlet per day:\n" + itemName + "\nis discounted "
				        + DIS.TWO_PLACE_DECIMAL.format(newItemDiscount) + "%, the same as items in S/O #"
				        + soIdWithSameDiscount);
				view.getShell().dispose();
				new SalesOrderView(soIdWithSameDiscount);
				return true;
			}
			order.setTotalDiscountRate(newItemDiscount);
		}
		return false;
	}

	protected boolean isItemDiscountSameAsPrevious() {
		BigDecimal currentDiscount = order.getTotalDiscountRate();
		BigDecimal newItemDiscount = discount.getTotal();
		if (!isForAnExTruck && !order.isAnRMA() && !currentDiscount.equals(newItemDiscount)) {
			clearTableItemEntry("One S/O per discount rate per outlet per day:\n" + itemName + "\nis discounted "
			        + DIS.TWO_PLACE_DECIMAL.format(newItemDiscount) + "%; other items in this S/O have "
			        + DIS.TWO_PLACE_DECIMAL.format(currentDiscount));
			return false;
		}
		return true;
	}

	protected boolean hasItemBeenEnteredBefore() {
		ArrayList<Integer> itemIds = order.getItemIds();
		if (!itemIds.isEmpty()) {
			int lineIdWithItemid = itemIds.indexOf(itemId);
			if (lineIdWithItemid > -1 && lineIdWithItemid != rowIdx) {
				clearTableItemEntry("Item # " + itemId + "\nis already on line #" + (lineIdWithItemid + 1));
				return true;
			}
		}
		return false;
	}

	protected boolean isNegativeItemIdInputValid() {
		return false;
	}

	protected boolean isItemMonetaryAndTransactionValid() {
		return true;
	}

	protected void clearTableItemEntry(String msg) {
		new ErrorDialog(msg);
		if (tableItem != null) {
			tableItem.setText(order.ITEM_COLUMN, "");
			tableItem.setText(order.UOM_COLUMN, "");
			tableItem.setText(order.PRICE_COLUMN, "");
			tableItem.setText(order.QTY_COLUMN, "");
			clearLineItemAndRecomputeTotals(tableItem.getText(order.TOTAL_COLUMN));
			itemIdInput.setText("");
		}
	}

	private boolean arePartnerReceivablesAging() {
		if (!order.getOverdue().equals(BigDecimal.ZERO)) {
			new OverdueView(partnerId, DIS.OVERDUE_CUTOFF);
			return true;
		} else {
			return false;
		}
	}

	protected void setNextTableWidget(BigDecimal price) {
		String uom = Constant.getInstance().getCurrencySign();
		if (isAMonetaryTransaction) {
			tableItem.setText(order.UOM_COLUMN, uom );
			order.setUomId(new UOM(uom).getId());
			new OrderItemQtyInput(view, order);
		} else {
			String[] uoms = new UOM().getSellingUoms(Math.abs(itemId));
			if (uoms != null) {
				if (uoms.length == 1) {
					uom = uoms[0];
					BigDecimal qtyPerUOM = new QtyPerUOM().getQty(itemId, uom);
					order.setPrice(price.multiply(qtyPerUOM));
					order.setUomId(new UOM(uom).getId());
					tableItem.setText(order.UOM_COLUMN, uom);
					tableItem.setText(order.PRICE_COLUMN, DIS.TWO_PLACE_DECIMAL.format(price));
					new OrderItemQtyInput(view, order);
				} else {
					order.setUoms(uoms);
					new OrderItemUomCombo(view, order);
				}
			}
		}
	}

	protected boolean isItemOnFile() {
		itemName = item.getName(Math.abs(itemId));
		if (itemName.isEmpty()) {
			clearTableItemEntry("Item #" + itemId + "\nis not on file");
			return false;
		} else {
			isItemMonetaryAndTransactionValid();
			return true;
		}
	}

	protected boolean doesItemHavePrice() {
		// check if item has price in the system
		if (price == null) {
			price = new Price().get(Math.abs(itemId), partnerId, date);
			if (price.equals(BigDecimal.ZERO)) {
				clearTableItemEntry("Item #" + itemId + "\nhas no price in our system");
				return false;
			}
			if (order.isAnRMA())
				price = price.negate();
		}
		order.setPrice(price);
		return true;
	}

	protected boolean isItemOnReferenceOrder() {
		if (!isAMonetaryTransaction) {
			BigDecimal referenceQty = item.getReferenceQty(itemId, referenceId);
			if (referenceQty.equals(BigDecimal.ZERO)) {
				clearTableItemEntry(itemName + "\nis not in " + referenceType + "#" + Math.abs(referenceId));
				return false;
			}
			order.setRefQty(referenceQty);
		}
		return true;
	}
}

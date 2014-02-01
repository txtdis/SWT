package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public abstract class ItemIdInput {
	private boolean isAnSI;
	private BigDecimal price;
	private Button itemListButton;
	private Combo uomCombo;
	private Date date;
	private String salesType;
	private Text itemIdInput;

	protected boolean isForAnExTruck, isEnteredTotalNegative, isAtFirstRow;
	protected int partnerId, itemId, rowIdx;
	protected Button postButton;
	protected Item item;
	protected OrderData data;
	protected OrderView view;
	protected PartnerDiscount discount;
	protected String itemName;
	protected TableItem tableItem;

	public ItemIdInput(OrderView orderView, OrderData orderData) {
		data = orderData;
		view = orderView;
		
		postButton = view.getPostButton();
		rowIdx = view.getRowIdx();
		tableItem = view.getTableItem(rowIdx);

		itemListButton = new TableButton(tableItem, rowIdx, 0, "Item List").getButton();
		itemIdInput = new TableTextInput(tableItem, 1, 0).getText();
		itemIdInput.setText(tableItem.getText(1));

		view.setItemIdInput(itemIdInput);
		view.setTableListButton(itemListButton);

		item = new Item();

		partnerId = data.getPartnerId();
		date = data.getDate();
		price = data.getPrice();
		isAtFirstRow = rowIdx == 0;
		salesType = isAnSI ? "S/I" : "S/O";

		new DataInputter(itemIdInput, uomCombo) {
			@Override
            protected Boolean isNonBlank() {
				itemId = DIS.parseInt(textInput);
				if (!isItemOnFile())
					return false;
	            return super.isNonBlank();
            }

			@Override
            protected Boolean isNegativeNot() {
	            return isBOInputValid();
            }

			@Override
            protected Boolean isPositive() {
	            return null;
            }

			@Override
            protected boolean isAnyNonZero() {
				if((Item.isMonetary(itemId)) ? isMonetaryInputValid() : isStockInputValid()) {
					data.setItemId(itemId);
					postButton.setEnabled(false);
					itemListButton.dispose();

					tableItem.setText(0, String.valueOf(rowIdx + 1));
					tableItem.setText(OrderView.ITEM_ID_COLUMN, String.valueOf(itemId));
					tableItem.setText(OrderView.ITEM_COLUMN, itemName);
					if (price != null) {
						DIS.formatTo2Places(price);
						tableItem.setText(OrderView.TOTAL_COLUMN, "");
					}
					itemIdInput.dispose();
					setNextTableWidget(price);
				}
				return false;
			}
		};
		itemIdInput.setFocus();
	}
	
	protected Boolean isBOInputValid() {
	    return null;
    }

	protected boolean isMonetaryInputValid() {
		new ErrorDialog("Monetary transactions\nare not allowed here");
		return false;
	}

	protected boolean isStockInputValid() {
			
		if (hasItemBeenEnteredBefore())
			return false;

		if (isAtFirstRow && arePartnerReceivablesAging() && date.after(DIS.NO_SO_WITH_OVERDUE_CUTOFF))
			return false;

		discount = new PartnerDiscount(partnerId, Math.abs(itemId), date);
		BigDecimal discount1 = discount.getFirstLevel();
		BigDecimal discount2 = discount.getSecondLevel();
		data.setDiscount1Percent(discount1);
		data.setDiscount2Percent(discount2);

		if (isAtFirstRow) {
			data.setTotalDiscountRate(discount.getTotal());
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

		return true;
    }

	protected boolean isItemBizUnitSameAsPrevious() {
		ArrayList<String> bizUnits = data.getBizUnits();
		String bizUnit = Item.getBizUnit(itemId);
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

	protected boolean isItemDiscountSameAsFromSameDayOrders(Type type) {
		BigDecimal newItemDiscount = discount.getTotal();
		if (isForAnExTruck && !(isAnSI && data.isForAnExTruck()) && data.isAnRMA())
			return false;

		int orderIdWithSameDiscount = OrderControl.getIdWithSameDiscount(type, itemId, partnerId, date );
		if (orderIdWithSameDiscount == 0) {
			// order.setTotalDiscountRate(newItemDiscount);
			return false;
		}

		clearTableItemEntries("One " + salesType + " per discount rate per outlet per day:\n" + itemName
		        + "\nis discounted " + DIS.formatTo2Places(newItemDiscount) + "%, the same as items in " + salesType
		        + " #" + orderIdWithSameDiscount);
		view.getShell().close();
		if (isAnSI)
			new InvoiceView(orderIdWithSameDiscount);
		else
			new SalesView(new SalesData(orderIdWithSameDiscount));
		return true;
	}

	protected boolean isItemDiscountSameAsPrevious() {
		BigDecimal currentDiscount = data.getTotalDiscountRate();
		BigDecimal newItemDiscount = discount.getTotal();
		if (!isForAnExTruck && !data.isAnRMA()
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
		ArrayList<Integer> itemIds = data.getItemIds();
		if (itemIds.isEmpty())
			return false;
		int lineIdWithItemid = itemIds.indexOf(itemId);
		if (lineIdWithItemid > -1 && lineIdWithItemid != rowIdx) {
			clearTableItemEntries("Item # " + itemId + "\nis already on line #" + (lineIdWithItemid + 1));
			return true;
		}
		return false;
	}

	protected void clearTableItemEntries(String msg) {
		new ErrorDialog(msg);
		if (tableItem != null) {
			tableItem.setText(OrderView.ITEM_COLUMN, "");
			tableItem.setText(OrderView.UOM_COLUMN, "");
			tableItem.setText(OrderView.PRICE_COLUMN, "");
			tableItem.setText(view.getQtyColumnIdx(), "");
			itemIdInput.setText("");
		}
	}

	private boolean arePartnerReceivablesAging() {
		if (data.isAnRMA())
			return false;
		if (DIS.isPositive(new Overdue().getBalance(partnerId))) {
			new OverdueStatementView(partnerId);
			return true;
		}
		return false;
	}

	protected void setNextTableWidget(BigDecimal input) {
		Type uom = Type.$;
		String[] uoms = UOM.getSellingUoms(Math.abs(itemId));
		if (uoms == null)
			return;
		if (uoms.length == 1 && !data.isAnRR()) {
			uom = Type.valueOf(uoms[0]);
			BigDecimal qtyPerUOM = QtyPerUOM.getQty(Math.abs(itemId), uom);
			data.setUom(uom);
			tableItem.setText(OrderView.UOM_COLUMN, uom.toString());
			if (price != null) {
				data.setPrice(price.multiply(qtyPerUOM));
				tableItem.setText(OrderView.PRICE_COLUMN, DIS.formatTo2Places(price));
			}
			new ItemQtyInput(view, data);
			return;
		}
		data.setUnitsOfMeasure(uoms);
		new OrderItemUomCombo(view, data);
	}

	protected boolean isItemOnFile() {
		itemName = Item.getShortId(Math.abs(itemId));
		if (itemName.isEmpty()) {
			clearTableItemEntries("Item #" + itemId + "\nis not on file");
			return false;
		}
		return true;
	}

	protected boolean doesItemHavePrice() {
		if (price == null) {
				price = new Price().get(Math.abs(itemId), partnerId, date);
				if (price.equals(BigDecimal.ZERO)) {
					clearTableItemEntries("Item #" + itemId + "\nhas no price for\n" + data.getPartner()
					        + "in our system");
					new ItemView(new ItemData(itemId));
					return false;
				}
				if (data.isAnRMA())
					price = price.negate();
				tableItem.setText(OrderView.PRICE_COLUMN, DIS.formatTo2Places(price));
		}
		data.setPrice(price);
		return true;
	}

	protected boolean isItemOnReferenceOrder() {
		int referenceId = data.getReferenceId();
		if (!data.isMaterialTransfer()) {
			BigDecimal referenceQty = Item.getReferenceQty(itemId, referenceId);
			if (referenceQty.equals(BigDecimal.ZERO)) {
				String referenceType = data.isReferenceAnSO() ? "S/O" : "P/O";
				clearTableItemEntries(itemName + "\nis not in " + referenceType + " #" + Math.abs(referenceId));
				return false;
			}
			data.setReferenceQuantity(referenceQty);
		}
		return true;
	}

	protected void clearTableItemEntry(String msg) {
		new ErrorDialog(msg);
		if (tableItem == null)
			return;
		tableItem.setText(OrderView.ITEM_COLUMN, "");
		tableItem.setText(OrderView.UOM_COLUMN, "");
		tableItem.setText(OrderView.PRICE_COLUMN, "");
		tableItem.setText(view.getQtyColumnIdx(), "");
		clearLineItemAndRecomputeTotals(tableItem.getText(OrderView.TOTAL_COLUMN));
		itemIdInput.setText("");
	}

	private void clearLineItemAndRecomputeTotals(String subTotalText) {
		if (subTotalText == null || subTotalText.trim().isEmpty())
			return;

		BigDecimal subTotal = new BigDecimal(subTotalText);
		BigDecimal discount1Total = subTotal.multiply(DIS.getRate(data.getDiscount1Percent()));
		subTotal = subTotal.subtract(discount1Total);
		BigDecimal discount2Total = subTotal.multiply(DIS.getRate(data.getDiscount2Percent()));
		subTotal = subTotal.subtract(discount2Total);
		BigDecimal vatable = DIS.divide(subTotal, DIS.VAT);
		BigDecimal vat = subTotal.subtract(vatable);

		BigDecimal total = data.getComputedTotal().subtract(subTotal);
		vatable = data.getTotalVatable().subtract(vatable);
		vat = data.getTotalVat().subtract(vat);
		discount1Total = data.getDiscount1Total().subtract(discount1Total);
		discount2Total = data.getDiscount2Total().subtract(discount2Total);

		TextDisplayBox discount1Box = view.getDiscount1Box();
		TextDisplayBox discount2Box = view.getDiscount2Box();

		Text computedTotalDisplay = view.getComputedTotalDisplay();
		Text discount1Display = discount1Box.getText();
		Text discount2Display = discount2Box.getText();
		Text totalVatDisplay = view.getTotalVatDisplay();
		Text totalVatableDisplay = view.getTotalVatableDisplay();
		
		computedTotalDisplay.setText(DIS.formatTo2Places(total));
		discount1Display.setText(DIS.formatTo2Places(discount1Total));
		discount2Display.setText(DIS.formatTo2Places(discount2Total));
		totalVatDisplay.setText(DIS.formatTo2Places(vat));
		totalVatableDisplay.setText(DIS.formatTo2Places(vatable));
		
		data.setDiscount1Total(discount1Total);
		data.setDiscount2Total(discount2Total);
		data.setComputedTotal(total);
		data.setTotalVatable(vatable);
		data.setTotalVat(vat);

		tableItem.setText(OrderView.TOTAL_COLUMN, "");
	}
}

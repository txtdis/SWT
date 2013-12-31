package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class OrderItemQtyInput {
	protected boolean mustReturn, isMaterialTransfer;
	protected int rowIdx;
	protected BigDecimal quantity;
	protected OrderView orderView;
	protected Order order;
	protected String textInput;
	protected TableItem tableItem;
	protected Text qtyInput, itemIdInput;

	public OrderItemQtyInput(OrderView view, Order report) {
		order = report;
		orderView = view;
		rowIdx = order.getRowIdx();
		tableItem = orderView.getTableItem();
		qtyInput = new TableTextInput(tableItem, rowIdx, order.getQtyColumnNo(), BigDecimal.ZERO).getText();
		orderView.setQtyInput(qtyInput);
		qtyInput.setFocus();
		String partner = order.getPartner();
		
		if(partner != null)
			isMaterialTransfer = partner.contains("MATERIAL TRANSFER");

		new TextInputter(qtyInput, itemIdInput) {
			@Override
			protected boolean isThePositiveNumberValid() {
				quantity = numericInput;
				if (!isQtyInputValid(textInput))
					return false;
				if (mustReturn)
					return true;
				order.setRowIdx(orderView.getTable().getItemCount());
				new ItemIdInputSwitcher(orderView, order);
				return true;
			}

		};
	}

	protected boolean isQtyInputValid(String textInput) {
		tableItem.setText(6, textInput);
		qtyInput.dispose();

		BigDecimal price = order.getPrice();
		boolean isAMonetaryTransaction = order.isAMonetaryTransaction();
		if (isAMonetaryTransaction && quantity.multiply(price).compareTo(order.getEnteredTotal()) != 0) {
			new ErrorDialog("Quantity must equate to\nthe EWT, PCV or O/R amount");
			return false;
		}

		ItemHelper item = new ItemHelper();
		boolean isAnRMA = order.isAnRMA();
		boolean isNotAnRMA = order.isAnSO() && !isAnRMA;
		boolean isAPO = order.isA_PO();
		boolean isA_DR = order.isA_DR();
		int itemId = order.getItemId();
		// if (!isAMonetaryTransaction && !isAnRMA) {
		// BigDecimal goodStock = item.getAvailableStock(itemId);
		// boolean hasEnoughGoodStock = goodStock.compareTo(quantity) > -1;
		// BigDecimal badStock = item.getBadStock(itemId);
		// boolean hasEnoughBadStock = badStock.compareTo(quantity) > -1;
		// BigDecimal soQty = order.getReferenceQty();
		// boolean hasEnoughSOqty = soQty.compareTo(quantity) > -1;
		// boolean isForDisposal = order.isForDisposal();
		// if (isForDisposal && !hasEnoughBadStock) {
		// new ErrorDialog("Only " + DIS.NO_COMMA_INTEGER.format(badStock) +
		// " left;\nplease adjust quantity");
		// return false;
		// } else if (isNotAnRMA && !isForDisposal && !hasEnoughGoodStock) {
		// new ErrorDialog("Only " + DIS.NO_COMMA_INTEGER.format(goodStock) +
		// " left;\nplease adjust quantity");
		// return false;
		// } else if ((order.isAnSI() || isA_DR) && !hasEnoughSOqty) {
		// new ErrorDialog("Only " + DIS.NO_COMMA_INTEGER.format(soQty) +
		// " is in S/O;\nplease adjust quantity");
		// return false;
		// }
		// }

		int uomId = new UOM(tableItem.getText(3)).getId();
		BigDecimal volumeDiscountQty = order.getVolumeDiscountQty();
		BigDecimal volumeDiscountValue = order.getVolumeDiscountValue();
		BigDecimal discountRate1 = BigDecimal.ZERO;
		BigDecimal discountRate2 = BigDecimal.ZERO;

		if (!item.isDiscountExempt(itemId)) {
			discountRate1 = order.getFirstLevelDiscountRate();
			discountRate2 = order.getSecondLevelDiscountRate();
		}
		// compute volume-discounted price & show sub-total (column 6)
		BigDecimal subtotal = BigDecimal.ZERO;
		if (uomId == new VolumeDiscount().getUomId(itemId, order.getDate())) {
			subtotal = (price.multiply(quantity)).subtract(volumeDiscountValue.multiply(quantity.divide(
			        volumeDiscountQty, 0, BigDecimal.ROUND_DOWN)));
		} else {
			subtotal = price.multiply(quantity);
		}

		if (isAnRMA) {
			BigDecimal balance = order.getEnteredTotal().add(subtotal);
			if (balance.compareTo(BigDecimal.ZERO) < 0) {
				new ErrorDialog("Exceeded RMA limit;\nadjust quantity");
				return false;
			} else {
				order.setEnteredTotal(balance);
				orderView.getTxtEnteredTotal().setText(DIS.TWO_PLACE_DECIMAL.format(order.getEnteredTotal()));
			}
		}

		// change quantity from input (column 4)
		tableItem.setText(4, DIS.TWO_PLACE_DECIMAL.format(quantity));
		// /qtyInput.dispose();
		tableItem.setText(6, DIS.TWO_PLACE_DECIMAL.format(subtotal));

		// show discount1
		BigDecimal net;
		if (!isAMonetaryTransaction) {
			TextDisplayBox d1 = orderView.getFirstLevelDiscountBox();
			d1.getLabel().setText(DIS.TWO_PLACE_DECIMAL.format(discountRate1) + "%");
			BigDecimal discount1 = subtotal.multiply(discountRate1.divide(DIS.HUNDRED));
			order.setFirstLevelDiscountTotal(order.getFirstLevelDiscountTotal().add(discount1));
			d1.getText().setText("" + DIS.TWO_PLACE_DECIMAL.format(order.getFirstLevelDiscountTotal()));
			// show discount2
			TextDisplayBox d2 = orderView.getSecondLevelDiscountBox();
			d2.getLabel().setText(DIS.TWO_PLACE_DECIMAL.format(discountRate2) + "%");
			BigDecimal discount2 = (subtotal.subtract(discount1)).multiply(discountRate2.divide(DIS.HUNDRED));
			order.setSecondLevelDiscountTotal(order.getSecondLevelDiscountTotal().add(discount2));
			d2.getText().setText("" + DIS.TWO_PLACE_DECIMAL.format(order.getSecondLevelDiscountTotal()));
			net = subtotal.subtract(discount1).subtract(discount2);
		} else {
			net = subtotal;
		}
		// show VAT
		BigDecimal vatable;
		if (isAMonetaryTransaction && isA_DR)
			vatable = BigDecimal.ZERO;
		else
			vatable = net.divide(DIS.VAT, BigDecimal.ROUND_HALF_EVEN);
		order.setTotalVatable(order.getTotalVatable().add(vatable));
		orderView.getTxtTotalVatable().setText(DIS.TWO_PLACE_DECIMAL.format(order.getTotalVatable()));
		// show VATable
		BigDecimal vat;
		if (isAMonetaryTransaction && isA_DR)
			vat = BigDecimal.ZERO;
		else
			vat = net.subtract(vatable);
		order.setTotalVat(order.getTotalVat().add(vat));
		orderView.getTxtTotalVat().setText(DIS.TWO_PLACE_DECIMAL.format(order.getTotalVat()));
		// show total
		BigDecimal computedTotal = order.getComputedTotal().add(net);
		order.setComputedTotal(computedTotal);
		orderView.getComputedTotalDisplay().setText(DIS.TWO_PLACE_DECIMAL.format(computedTotal));

		// save line-item data
		int rowIdx = order.getRowIdx();
		order.saveLineItem(order.getItemIds(), itemId, rowIdx);
		order.saveLineItem(order.getUomIds(), uomId, rowIdx);
		order.saveLineItem(order.getQtys(), quantity, rowIdx);

		BigDecimal enteredTotal = order.getEnteredTotal();
		final Button postButton = orderView.getPostButton();
		if (enteredTotal.subtract(computedTotal).abs().compareTo(BigDecimal.ONE) < 1 || order.isAnSO() || isAPO
		        || isMaterialTransfer)
			postButton.setEnabled(true);
		if (isAMonetaryTransaction) {
			postButton.setFocus();
			mustReturn = true;
		}

		order.setPrice(null);
		order.setRowIdx(++rowIdx);

		return true;
	}
}
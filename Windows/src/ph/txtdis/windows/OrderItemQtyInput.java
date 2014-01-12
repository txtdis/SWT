package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
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
//		if (!isAMonetaryTransaction && !isAnRMA) {
//			BigDecimal goodStock = item.getAvailableStock(itemId);
//			boolean hasEnoughGoodStock = goodStock.compareTo(quantity) >= 0;
//			BigDecimal badStock = item.getBadStock(itemId);
//			boolean hasEnoughBadStock = badStock.compareTo(quantity) >= 0;
//			BigDecimal soQty = order.getReferenceQty();
//			boolean hasEnoughSOqty = soQty.compareTo(quantity) >= 0;
//			boolean isForDisposal = order.isForDisposal();
//			if (isForDisposal && !hasEnoughBadStock) {
//				new ErrorDialog("Only " + DIS.NO_COMMA_INTEGER.format(badStock) + " left;\nplease adjust quantity");
//				return false;
//			} else if (isNotAnRMA && !isForDisposal && !hasEnoughGoodStock) {
//				new ErrorDialog("Only " + DIS.NO_COMMA_INTEGER.format(goodStock) + " left;\nplease adjust quantity");
//				return false;
//			} else if ((order.isAnSI() || isA_DR) && !hasEnoughSOqty) {
//				new ErrorDialog("Only " + DIS.NO_COMMA_INTEGER.format(soQty) + " is in S/O;\nplease adjust quantity");
//				return false;
//			}
//		}

		int uomId = new UOM(tableItem.getText(3)).getId();
		boolean isDiscountExempt = item.isDiscountExempt(itemId);
		BigDecimal volumeDiscountQty = order.getVolumeDiscountQty();
		BigDecimal volumeDiscountValue = order.getVolumeDiscountValue();
		BigDecimal discount1Percent = isDiscountExempt ? BigDecimal.ZERO : order.getDiscount1Percent();
		BigDecimal discount2Percent = isDiscountExempt ? BigDecimal.ZERO : order.getDiscount2Percent();
		BigDecimal subtotal = price.multiply(quantity);
		int volumeDiscountUom = new VolumeDiscount().getUomId(itemId, order.getDate());
		
		if (uomId == volumeDiscountUom) {
			BigDecimal countQtyIsDiscounted = quantity.divideToIntegralValue(volumeDiscountQty);
			BigDecimal volumeDiscount = volumeDiscountValue.multiply(countQtyIsDiscounted);
			subtotal = subtotal.subtract(volumeDiscount);
		}

		if (isAnRMA) {
			BigDecimal balance = order.getEnteredTotal().add(subtotal);
			if (DIS.isNegative(balance)) {
				new ErrorDialog("Exceeded RMA limit;\nadjust quantity");
				return false;
			} else {
				order.setEnteredTotal(balance);
				orderView.getTxtEnteredTotal().setText(DIS.formatTo2Places(order.getEnteredTotal()));
			}
		}

		tableItem.setText(4, DIS.formatTo2Places(quantity));
		tableItem.setText(6, DIS.formatTo2Places(subtotal));
		BigDecimal net = subtotal;
		
		if (!isAMonetaryTransaction) {
			TextDisplayBox discount1Display = orderView.getDiscount1Display();
			Label discount1Label = discount1Display.getLabel();
			discount1Label.setText(DIS.formatTo2Places(discount1Percent) + "%");			
			BigDecimal discount1 = subtotal.multiply(DIS.getRate(discount1Percent));
			
			order.setDiscount1Total(order.getDiscount1Total().add(discount1));
			discount1Display.getText().setText(DIS.formatTo2Places(order.getDiscount1Total()));

			TextDisplayBox discount2Display = orderView.getDiscount2Display();
			Label discount2Label = discount2Display.getLabel();
			discount2Label.setText(DIS.formatTo2Places(discount2Percent) + "%");
			BigDecimal discount2 = (subtotal.subtract(discount1)).multiply(DIS.getRate(discount2Percent));
			order.setDiscount2Total(order.getDiscount2Total().add(discount2));
			discount2Display.getText().setText(DIS.formatTo2Places(order.getDiscount2Total()));

			net = subtotal.subtract(discount1).subtract(discount2);
		} 

		BigDecimal vatable = (isAMonetaryTransaction && isA_DR) ? BigDecimal.ZERO : DIS.getQuotient(net, DIS.VAT);
		order.setTotalVatable(order.getTotalVatable().add(vatable));
		orderView.getTxtTotalVatable().setText(DIS.formatTo2Places(order.getTotalVatable()));
		
		// show VATable
		BigDecimal vat = (isAMonetaryTransaction && isA_DR) ? BigDecimal.ZERO : net.subtract(vatable);
		order.setTotalVat(order.getTotalVat().add(vat));
		orderView.getTxtTotalVat().setText(DIS.formatTo2Places(order.getTotalVat()));
		
		// show total
		BigDecimal computedTotal = order.getComputedTotal().add(net);
		order.setComputedTotal(computedTotal);
		orderView.getComputedTotalDisplay().setText(DIS.formatTo2Places(computedTotal));

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
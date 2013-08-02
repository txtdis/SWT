package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class OrderItemQtyEntry {
	private Text txtQty;
	private int rowIdx, itemId;
	Date postDate;
	private boolean isSO, isSI, isDR, isRMA, isMonetary, isDisposal;
	private BigDecimal price;
	private BigDecimal vatRate = Constant.getInstance().getVat();

	public OrderItemQtyEntry(final OrderView view, final Order order) {
		final Button btnPost = view.getBtnPost();
		rowIdx = order.getRowIdx();
		txtQty = view.getTxtQty();
		txtQty.setTouchEnabled(true);
		txtQty.setFocus();

		itemId = order.getItemId();
		postDate = order.getPostDate();
		isSO = order.isSO();
		isSI = order.isSI();
		isDR = order.isDR();
		isRMA = order.isRMA();
		isMonetary = order.isMonetary();
		isDisposal = order.isForDisposal();

		new DecimalVerifier(txtQty);
		txtQty.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event e) {

				String strQty = txtQty.getText().trim();
				if (strQty.isEmpty())
					return;

				BigDecimal qty = new BigDecimal(strQty);
				if (qty.compareTo(BigDecimal.ZERO) < 1) {
					clearText("Quantity must be\npositive");
					return;
				}

				price = order.getPrice();
				if (isMonetary && qty.multiply(price).compareTo(order.getEnteredTotal()) != 0) {
					clearText("Quantity must equate to\nthe EWT, PCV or O/R amount");
					return;
				}

				ItemHelper item = new ItemHelper();
				if ((isSI && postDate.after(DIS.SI_WITH_SO_CUTOFF)) && !(isSI && isRMA) && !order.isDealerIncentive()) {
					BigDecimal goodStock = item.getAvailableStock(itemId);
					boolean hasEnoughGoodStock = goodStock.compareTo(qty) > -1;
					BigDecimal badStock = item.getBadStock(itemId);
					boolean hasEnoughBadStock = badStock.compareTo(qty) > -1;
					BigDecimal soQty = order.getRefQty();
					boolean hasEnoughSOqty = soQty.compareTo(qty) > -1;
					if (isDisposal && !hasEnoughBadStock) {
						clearText("Only " + DIS.NO_COMMA_INTEGER.format(badStock) + " left;\nplease adjust quantity");
						return;
					} else if (isSO && !isRMA && !isDisposal && !hasEnoughGoodStock) {
						clearText("Only " + DIS.NO_COMMA_INTEGER.format(goodStock) + " left;\nplease adjust quantity");
						return;
					} else if ((isSI || isDR) && !hasEnoughSOqty) {
						clearText("Only " + DIS.NO_COMMA_INTEGER.format(soQty) + " is in S/O;\nplease adjust quantity");
						return;
					}
				}

				TableItem tableItem = view.getTableItem(rowIdx);
				int uomId = new UOM(tableItem.getText(3)).getId();
				BigDecimal volumeDiscountQty = order.getVolumeDiscountQty();
				BigDecimal volumeDiscountValue = order.getVolumeDiscountValue();
				BigDecimal discountRate1 = BigDecimal.ZERO;
				BigDecimal discountRate2 = BigDecimal.ZERO;

				if (!item.isDiscountExempt(itemId)) {
					discountRate1 = order.getDiscountRate1();
					discountRate2 = order.getDiscountRate2();
				}
				// compute volume-discounted pric & show sub-total (column 6)
				BigDecimal subtotal = BigDecimal.ZERO;
				if (uomId == new VolumeDiscount().getUomId(itemId, postDate)) {
					subtotal = (price.multiply(qty)).subtract(volumeDiscountValue.multiply(qty.divide(
					        volumeDiscountQty, 0, BigDecimal.ROUND_DOWN)));
				} else {
					subtotal = price.multiply(qty);
				}

				if (isRMA) {
					BigDecimal balance = order.getEnteredTotal().add(subtotal);
					if (balance.compareTo(BigDecimal.ZERO) < 0) {
						clearText("Exceeded RMA limit;\nadjust quantity");
						return;
					} else {
						order.setEnteredTotal(balance);
						view.getTxtEnteredTotal().setText(DIS.TWO_PLACE_DECIMAL.format(order.getEnteredTotal()));
					}
				}
				// change quantity from input (column 4)
				tableItem.setText(4, DIS.TWO_PLACE_DECIMAL.format(qty));
				txtQty.dispose();
				tableItem.setText(6, DIS.TWO_PLACE_DECIMAL.format(subtotal));
				// show discount1
				DataDisplay d1 = view.getDiscount1();
				d1.getLabel().setText(DIS.TWO_PLACE_DECIMAL.format(discountRate1) + "%");
				BigDecimal discount1 = subtotal.multiply(discountRate1.divide(DIS.HUNDRED, BigDecimal.ROUND_HALF_EVEN));
				order.setTotalDiscount1(order.getTotalDiscount1().add(discount1));
				d1.getText().setText("" + DIS.TWO_PLACE_DECIMAL.format(order.getTotalDiscount1()));
				// show discount2
				DataDisplay d2 = view.getDiscount2();
				d2.getLabel().setText(DIS.TWO_PLACE_DECIMAL.format(discountRate2) + "%");
				BigDecimal discount2 = (subtotal.subtract(discount1)).multiply(discountRate2.divide(DIS.HUNDRED,
				        BigDecimal.ROUND_HALF_EVEN));
				order.setTotalDiscount2(order.getTotalDiscount2().add(discount2));
				d2.getText().setText("" + DIS.TWO_PLACE_DECIMAL.format(order.getTotalDiscount2()));
				// show VAT
				BigDecimal net = subtotal.subtract(discount1).subtract(discount2);
				BigDecimal vatable;
				if (isMonetary)
					vatable = BigDecimal.ZERO;
				else
					vatable = net.divide(vatRate, BigDecimal.ROUND_HALF_EVEN);
				order.setTotalVatable(order.getTotalVatable().add(vatable));
				view.getTxtTotalVatable().setText(DIS.TWO_PLACE_DECIMAL.format(order.getTotalVatable()));
				// show VATable
				BigDecimal vat;
				if (isMonetary)
					vat = BigDecimal.ZERO;
				else
					vat = net.subtract(vatable);
				order.setTotalVat(order.getTotalVat().add(vat));
				view.getTxtTotalVat().setText(DIS.TWO_PLACE_DECIMAL.format(order.getTotalVat()));
				// show total
				BigDecimal computedTotal = order.getComputedTotal().add(net);
				order.setComputedTotal(computedTotal);
				view.getTxtComputedTotal().setText(DIS.TWO_PLACE_DECIMAL.format(computedTotal));
				// save line-item data
				order.getItemIds().add(isRMA ? -itemId : itemId);
				order.getUomIds().add(uomId);
				order.getQtys().add(qty);
				Text txtId = view.getTxtId();
				if (txtId != null) {
					String strId = txtId.getText().trim();
					if (!strId.isEmpty())
						order.setId(Integer.parseInt(strId));
				}
				order.setPostDate(new DateAdder(view.getTxtPostDate().getText()).plus(0));
				// go to next line item, enabling postButton if variance <= 1.00
				BigDecimal enteredTotal = order.getEnteredTotal();
				if (enteredTotal.subtract(computedTotal).abs().compareTo(BigDecimal.ONE) < 1)
					btnPost.setEnabled(true);
				if (isMonetary) {
					btnPost.setFocus();
				} else {
					int tableItemCount = view.getTable().getItemCount();
					order.setRowIdx(tableItemCount);
					new OrderItemIdEntry(view, order);
				}
			}
		});
	}

	private void clearText(String msg) {
		new ErrorDialog(msg);
		txtQty.setText("");
		txtQty.setBackground(DIS.YELLOW);
		txtQty.setEditable(true);
	}
}

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
	private int itemId, uom, row;
	private Button btnPost;
	private BigDecimal qty, perQty, volumeDiscount, price, subtotal, net, vat;
	private BigDecimal discount1, discount2, discountRate1, discountRate2;
	private String strId;
	private Date date;
	private Text txtQty;
	private boolean isRMA, isSO;

	public OrderItemQtyEntry(final OrderView view,
			final InvoiceLineItem lineItem, final Order order, int iRow) {
		row = iRow;
		btnPost = view.getBtnPost();
		txtQty = lineItem.getTxtQty();

		new DecimalVerifier(txtQty);
		txtQty.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				TableItem tableItem = lineItem.getTableItem();
				itemId = lineItem.getItemId();
				isRMA = lineItem.isReturnedMaterial();
				date = order.getPostDate();
				isSO = order.getModule().contains("Sales Order");
				boolean isMonetary = new ItemHelper().isMonetaryType(itemId);
				boolean isDisposal = view.getTxtPartnerName().getText().trim()
						.equals("BO DISPOSAL");
				String strQty = txtQty.getText();
				if (strQty.isEmpty())
					return;
				qty = new BigDecimal(strQty);
				if (qty.compareTo(BigDecimal.ZERO) <= 0) {
					txtQty.setText("");
					return;
				}
				price = lineItem.getPrice();
				if (isMonetary
						&& qty.multiply(price).compareTo(order.getActual()) != 0) {
					clearText("Quantity must equate to\nthe EWT, PCV or O/R amount");
					return;
				}
				ItemHelper iHelp = new ItemHelper();
				BigDecimal goodStock = iHelp.getAvailableStock(itemId);
				boolean hasEnoughGoodStock = goodStock.compareTo(qty) >= 0;
				BigDecimal badStock = iHelp.getBadStock(itemId);
				boolean hasEnoughBadStock = badStock.compareTo(qty) >= 0;
				if (isDisposal && !hasEnoughBadStock) {
					clearText("Only " + DIS.BIF.format(badStock)
							+ " left;\nplease adjust quantity");
					return;
				} else if (isSO && !isRMA && !isDisposal && !hasEnoughGoodStock) {
					clearText("Only " + DIS.BIF.format(goodStock)
							+ " left;\nplease adjust quantity");
					return;
				}
				uom = lineItem.getUom();
				perQty = lineItem.getPerQty();
				volumeDiscount = lineItem.getVolumeDiscount();
				if (new ItemHelper().isNotDiscounted(itemId)) {
					discountRate1 = BigDecimal.ZERO;
					discountRate2 = BigDecimal.ZERO;
				} else {
					discountRate1 = order.getDiscountRate1();
					discountRate2 = order.getDiscountRate2();
				}
				// compute volume-discounted price
				if (isRMA)
					price = price.multiply(new BigDecimal(-1));
				// show sub-total (column 6)
				if (uom == new VolumeDiscount().getUom(itemId, date)) {
					subtotal = (price.multiply(qty)).subtract(volumeDiscount
							.multiply(qty.divide(perQty, 0,
									BigDecimal.ROUND_DOWN)));
				} else {
					subtotal = price.multiply(qty);
				}
				if (isRMA && isSO) {
					BigDecimal balance = order.getActual().add(subtotal);
					if (balance.compareTo(BigDecimal.ZERO) < 0) {
						clearText("Exceeded limit;\nadjust quantity");
						return;
					} else {
						order.setActual(balance);
						view.getTxtActual().setText(
								DIS.LNF.format(order.getActual()));
					}
				}
				// change item id from input (column 1)
				tableItem.setText(1, String.valueOf(itemId));
				lineItem.getTxtItemId().dispose();
				// disable combo (column 3)
				tableItem.setText(3, lineItem.getCmbUnit().getText());
				lineItem.getCmbUnit().dispose();
				// change quantity from input (column 4)
				tableItem.setText(4, DIS.LIF.format(qty));
				txtQty.dispose();
				tableItem.setText(6, DIS.LNF.format(subtotal));
				// show discount1
				DataDisplay d1 = view.getDiscount1();
				d1.setLabel("" + DIS.LNF.format(discountRate1) + "%");
				discount1 = subtotal.multiply(discountRate1.divide(
						new BigDecimal(100), BigDecimal.ROUND_HALF_EVEN));
				order.setTotalDiscount1(order.getTotalDiscount1()
						.add(discount1));
				d1.setText(DIS.LNF.format(order.getTotalDiscount1()));
				// show discount2
				DataDisplay d2 = view.getDiscount2();
				d2.setLabel("" + DIS.LNF.format(discountRate2) + "%");
				discount2 = (subtotal.subtract(discount1))
						.multiply(discountRate2.divide(new BigDecimal(100),
								BigDecimal.ROUND_HALF_EVEN));
				order.setTotalDiscount2(order.getTotalDiscount2()
						.add(discount2));
				d2.setText(DIS.LNF.format(order.getTotalDiscount2()));
				// show VAT
				net = subtotal.subtract(discount1).subtract(discount2);
				BigDecimal vatRate = BigDecimal.ONE.add(DIS.VAT);
				vat = net.subtract(net.divide(vatRate,
						BigDecimal.ROUND_HALF_EVEN));
				if (isMonetary)
					vat = BigDecimal.ZERO;
				order.setTotalVat(order.getTotalVat().add(vat));
				view.getTxtTotalVat().setText(
						DIS.LNF.format(order.getTotalVat()));
				// show VATable
				BigDecimal totalVatable = order.getTotalVatable().add(
						net.subtract(vat));
				if (isMonetary)
					totalVatable = BigDecimal.ZERO;
				order.setTotalVatable(totalVatable);
				view.getTxtTotalVatable().setText(
						DIS.LNF.format(order.getTotalVatable()));
				// show total
				BigDecimal sumTotal = order.getSumTotal().add(net);
				order.setSumTotal(sumTotal);
				view.getTxtSumTotal().setText(DIS.LNF.format(sumTotal));
				// save line-item data
				order.getItemIds().add(itemId * (isRMA ? -1 : 1));
				order.getUoms().add(uom);
				order.getQtys().add(qty);
				Text txtId = view.getTxtId();
				if (txtId != null) {
					strId = txtId.getText().trim();
					if (!strId.isEmpty())
						order.setId(Integer.parseInt(strId));
				}
				order.setPostDate(new DateAdder(view.getTxtPostDate().getText())
						.plus(0));
				// go to next line item, enabling postButton if variance <= 1.00
				BigDecimal actual = order.getActual();
				if (actual.subtract(sumTotal).abs().compareTo(BigDecimal.ONE) <= 0)
					btnPost.setEnabled(true);
				if (isMonetary) {
					btnPost.setFocus();
				} else {
					new InvoiceLineItem(view, order, ++row);
				}
			}
		});
	}

	private void clearText(String msg) {
		new ErrorDialog(msg);
		txtQty.setText("");
		txtQty.setBackground(View.yellow());
		txtQty.setEditable(true);
	}
}

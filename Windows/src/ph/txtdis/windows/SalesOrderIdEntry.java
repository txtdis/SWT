package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class SalesOrderIdEntry {
	private int referenceId, orderId;
	private boolean isRMA;
	private ArrayList<BigDecimal> referenceQtys;
	private String module;
	private Text referenceIdInput, txtOrderId, txtSeries, txtActual;
	private Object[][] rmaData, referenceData;
	private Order referenceOrder;

	public SalesOrderIdEntry(final OrderView view, final Order order) {
		referenceIdInput = view.getReferenceIdInput();
		txtOrderId = view.getIdInput();
		txtSeries = view.getTxtSeries();
		txtActual = view.getTxtEnteredTotal();
		module = order.getModule();
		referenceData = order.getData();

		new IntegerVerifier(referenceIdInput);
		referenceIdInput.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				String referenceIdText = referenceIdInput.getText().trim();
				boolean isSI = order.isAnSI();
				boolean isDR = order.isA_DR();
				boolean isReferenceAPO;
				if (!referenceIdText.isEmpty()) {
					referenceId = Integer.parseInt(referenceIdText);
					isReferenceAPO = referenceId < 0;
					if (referenceId == 0) {
						new ErrorDialog("Enter only positive integers for S/O;\nnegative for P/O.");
						clearText();
					} else {
						OrderHelper helper = new OrderHelper();
						orderId = helper.getOrderId(referenceId);
						boolean isFromExTruck = helper.isFromExTruck(referenceId);
						order.setForAnExTruck(isFromExTruck);
						order.setFromAnExTruck(isFromExTruck);
						order.setReferenceId(referenceId);
						String currentModule;
						if (orderId != 0 && !isFromExTruck) {
							int thisId;
							if (orderId < 0) {
								currentModule = "Delivery Report";
								thisId = -orderId;
							} else {
								currentModule = "Invoice";
								thisId = orderId;
							}
							new ErrorDialog((isReferenceAPO ? "P/O" : "S/O") + " #" + Math.abs(referenceId)
							        + "\nhas already been posted\nas " + currentModule + " #" + thisId);
							orderId = 0;
							clearText();
						} else {
							if (isReferenceAPO)
								referenceOrder = new PurchaseOrder(-referenceId);
							else
								referenceOrder = new SalesOrder(referenceId);

							BigDecimal referenceTotal = referenceOrder.getComputedTotal();
							if (referenceTotal.equals(BigDecimal.ZERO)) {
								new ErrorDialog("S/O #" + referenceId + "\nis not in our system");
								clearText();
								return;
							}

							referenceQtys = referenceOrder.getQtys();
							referenceData = referenceOrder.getData();
							if (!isFromExTruck) {
								if (isRMA = referenceOrder.getComputedTotal().compareTo(BigDecimal.ZERO) < 0) {
									if (isSI || isDR) {
										rmaData = helper.getReceivedReturnedMaterials(referenceId);
										if (rmaData == null) {
											new ErrorDialog("Warehouse still has not received\n"
											        + "materials to be returned\nper S/O #" + referenceId);
											clearText();
											return;
										}
										Map<Object, Object> rmMap = ArrayUtils.toMap(rmaData);
										int itemId;
										BigDecimal qtyPer, soQty, rmQty;
										// [1]item_id, [3]uom.unit, [4]qty
										for (int i = 0; i < referenceData.length; i++) {
											itemId = -(int) referenceData[i][1];
											qtyPer = new QtyPerUOM().getQty(itemId, (String) referenceData[i][3]);
											if (rmMap.containsKey(itemId)) {
												rmQty = (BigDecimal) rmMap.get(itemId);
											} else {
												rmQty = BigDecimal.ZERO;
											}
											soQty = ((BigDecimal) referenceData[i][4]).multiply(qtyPer);
											if (!rmQty.equals(soQty)) {
												new ErrorDialog("R/R received quantity\n"
												        + "differs from approved S/O;\ncorrect either or both\n"
												        + "before continuing.");
												clearText();
												return;
											}
										}
									} else {
										new ErrorDialog("Negative D/R\nis not an option");
										clearText();
										return;
									}
								}
								boolean isProductReturn = isReferenceAPO && isSI;
								if (isProductReturn) {
									negatePrice();
								} else if ((isSI || isDR) && !isRMA) {
									Object[][] rrData = helper.getReceivedMaterials(referenceId);
									if (rrData != null) {
										Map<Object, Object> rrMap = ArrayUtils.toMap(rrData);
										int itemId;
										BigDecimal qtyPer, soQty, rmQty, netQty, subtotal;
										BigDecimal discount1, rate1, discount2, rate2;
										BigDecimal vatable, vat;
										BigDecimal total = BigDecimal.ZERO;
										// [1]item_id, [3]uom.unit, [4]qty
										for (int i = 0; i < referenceData.length; i++) {
											itemId = (int) referenceData[i][1];
											qtyPer = new QtyPerUOM().getQty(itemId, (String) referenceData[i][3]);
											if (rrMap.containsKey(itemId)) {
												rmQty = (BigDecimal) rrMap.get(itemId);
											} else {
												rmQty = BigDecimal.ZERO;
											}
											soQty = ((BigDecimal) referenceData[i][4]).multiply(qtyPer);
											netQty = (soQty.subtract(rmQty)).divide(
											        qtyPer.equals(BigDecimal.ZERO) ? BigDecimal.ONE : qtyPer,
											        BigDecimal.ROUND_HALF_EVEN);
											referenceData[i][4] = netQty;
											referenceQtys.set(i, netQty);
											subtotal = netQty.multiply((BigDecimal) referenceData[i][5]);
											referenceData[i][6] = subtotal;
											total = total.add(subtotal);
										}
										rate1 = referenceOrder.getDiscount1Percent().divide(DIS.HUNDRED,
										        BigDecimal.ROUND_HALF_EVEN);
										rate2 = referenceOrder.getDiscount2Percent().divide(DIS.HUNDRED,
										        BigDecimal.ROUND_HALF_EVEN);
										discount1 = total.multiply(rate1);
										total = total.subtract(discount1);
										discount2 = total.multiply(rate2);
										total = total.subtract(discount2);
										vatable = total.divide(DIS.VAT, BigDecimal.ROUND_HALF_EVEN);
										vat = total.subtract(vatable);
										referenceOrder.setDiscount1Percent(rate1.multiply(DIS.HUNDRED));
										referenceOrder.setDiscount2Percent(rate2.multiply(DIS.HUNDRED));
										referenceOrder.setDiscount1Total(discount1);
										referenceOrder.setDiscount2Total(discount2);
										referenceOrder.setTotalVat(vat);
										referenceOrder.setTotalVatable(vatable);
										referenceOrder.setComputedTotal(total);
										referenceOrder.setData(referenceData);
									}
								}
								txtOrderId.getShell().close();
								if (isSI) {
									new InvoiceView(referenceOrder);
								} else {
									new DeliveryView(referenceOrder);
								}
							} else {
								next();
							}
						}
					}
				} else {
					next();
				}
			}

			private void negatePrice() {
				BigDecimal qty, subtotal;
				for (int i = 0; i < referenceData.length; i++) {
					qty = ((BigDecimal) referenceData[i][4]).negate();
					referenceData[i][4] = qty;
					referenceQtys.set(i, qty);
					subtotal = (BigDecimal) referenceData[i][6];
					referenceData[i][6] = subtotal.negate();
				}
				referenceOrder.setDiscount1Total(referenceOrder.getDiscount1Total().negate());
				referenceOrder.setDiscount2Total(referenceOrder.getDiscount2Total().negate());
				referenceOrder.setTotalVat(referenceOrder.getTotalVat().negate());
				referenceOrder.setTotalVatable(referenceOrder.getTotalVatable().negate());
				referenceOrder.setComputedTotal(referenceOrder.getComputedTotal().negate());
				referenceOrder.setReferenceId(-referenceId);
			}
		});
	}

	private void clearText() {
		referenceIdInput.setText("");
		referenceIdInput.setEditable(true);
		referenceIdInput.setBackground(UI.YELLOW);
		referenceIdInput.setFocus();
	}

	private void next() {
		referenceIdInput.setTouchEnabled(false);
		if (module.contains("Invoice")) {
			txtSeries.setTouchEnabled(true);
			txtSeries.setFocus();
		} else {
			txtActual.setTouchEnabled(true);
			txtActual.setFocus();
		}
	}
}

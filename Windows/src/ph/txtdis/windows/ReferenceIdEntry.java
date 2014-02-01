package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class ReferenceIdEntry {
	private int referenceId, orderId;
	private boolean isRMA;
	private ArrayList<BigDecimal> referenceQtys;
	private Text referenceIdInput;
	private Object[][] receivingTableData, tableData;
	private OrderData orderData, referenceData;
	private OrderView view;

	public ReferenceIdEntry(OrderView orderView, OrderData data) {
		orderData = data;
		view = orderView;
		referenceIdInput = ((DeliveryView) view).getReferenceIdInput();
		tableData = orderData.getTableData();

		referenceIdInput.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				String referenceIdText = referenceIdInput.getText().trim();
				boolean isSI = orderData.isSI();
				boolean isDR = orderData.isA_DR();
				boolean isReferenceAPO;
				if (!referenceIdText.isEmpty()) {
					referenceId = Integer.parseInt(referenceIdText);
					isReferenceAPO = referenceId < 0;
					if (referenceId == 0) {
						new ErrorDialog("Enter only positive integers for S/O;\nnegative for P/O.");
						clearText();
					} else {
						orderId = OrderControl.getOrderId(referenceId);
						boolean isFromExTruck = OrderControl.isFromExTruck(referenceId);
						((OrderData) orderData).setForAnExTruck(isFromExTruck);
						((OrderData) orderData).setFromAnExTruck(isFromExTruck);
						((OrderData) orderData).setReferenceId(referenceId);
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
								referenceData = new PurchaseData(-referenceId);
							else
								referenceData = new SalesData(referenceId);

							BigDecimal referenceTotal = referenceData.getComputedTotal();
							if (referenceTotal.equals(BigDecimal.ZERO)) {
								new ErrorDialog("S/O #" + referenceId + "\nis not in our system");
								clearText();
								return;
							}

							referenceQtys = referenceData.getQtys();
							tableData = referenceData.getTableData();
							if (!isFromExTruck) {
								if (isRMA = referenceData.getComputedTotal().compareTo(BigDecimal.ZERO) < 0) {
									if (isSI || isDR) {
										receivingTableData = OrderControl.getReceivedReturnedMaterials(referenceId);
										if (receivingTableData == null) {
											new ErrorDialog("Warehouse still has not received\n"
											        + "materials to be returned\nper S/O #" + referenceId);
											clearText();
											return;
										}
										Map<Object, Object> rmMap = ArrayUtils.toMap(receivingTableData);
										int itemId;
										BigDecimal qtyPer, soQty, rmQty;
										// [1]item_id, [3]uom.unit, [4]qty
										for (int i = 0; i < tableData.length; i++) {
											itemId = -(int) tableData[i][1];
											qtyPer = QtyPerUOM.getQty(itemId, Type.valueOf((String) tableData[i][3]));
											if (rmMap.containsKey(itemId)) {
												rmQty = (BigDecimal) rmMap.get(itemId);
											} else {
												rmQty = BigDecimal.ZERO;
											}
											soQty = ((BigDecimal) tableData[i][4]).multiply(qtyPer);
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
								boolean isProductReturn = isReferenceAPO && (isSI || isDR) ;
								if (isProductReturn) {
									negatePrice();
								} else if ((isSI || isDR) && !isRMA) {
									Object[][] rrData = OrderControl.getReceivedMaterials(referenceId);
									if (rrData != null) {
										Map<Object, Object> rrMap = ArrayUtils.toMap(rrData);
										int itemId;
										BigDecimal qtyPer, soQty, rmQty, netQty, subtotal;
										BigDecimal discount1, rate1, discount2, rate2;
										BigDecimal vatable, vat;
										BigDecimal total = BigDecimal.ZERO;
										// [1]item_id, [3]uom.unit, [4]qty
										for (int i = 0; i < tableData.length; i++) {
											itemId = (int) tableData[i][1];
											qtyPer = QtyPerUOM.getQty(itemId, Type.valueOf((String) tableData[i][3]));
											if (rrMap.containsKey(itemId)) {
												rmQty = (BigDecimal) rrMap.get(itemId);
											} else {
												rmQty = BigDecimal.ZERO;
											}
											soQty = ((BigDecimal) tableData[i][4]).multiply(qtyPer);
											netQty = (soQty.subtract(rmQty)).divide(
											        qtyPer.equals(BigDecimal.ZERO) ? BigDecimal.ONE : qtyPer,
											        BigDecimal.ROUND_HALF_EVEN);
											tableData[i][4] = netQty;
											referenceQtys.set(i, netQty);
											subtotal = netQty.multiply((BigDecimal) tableData[i][5]);
											tableData[i][6] = subtotal;
											total = total.add(subtotal);
										}
										rate1 = referenceData.getDiscount1Percent().divide(DIS.HUNDRED,
										        BigDecimal.ROUND_HALF_EVEN);
										rate2 = referenceData.getDiscount2Percent().divide(DIS.HUNDRED,
										        BigDecimal.ROUND_HALF_EVEN);
										discount1 = total.multiply(rate1);
										total = total.subtract(discount1);
										discount2 = total.multiply(rate2);
										total = total.subtract(discount2);
										vatable = total.divide(DIS.VAT, BigDecimal.ROUND_HALF_EVEN);
										vat = total.subtract(vatable);
										referenceData.setDiscount1Percent(rate1.multiply(DIS.HUNDRED));
										referenceData.setDiscount2Percent(rate2.multiply(DIS.HUNDRED));
										referenceData.setDiscount1Total(discount1);
										referenceData.setDiscount2Total(discount2);
										referenceData.setTotalVat(vat);
										referenceData.setTotalVatable(vatable);
										referenceData.setComputedTotal(total);
										referenceData.setData(tableData);
									}
								}
								orderData.setReferenceId(referenceId);
								view.getShell().close();
								if (isSI) {
									new InvoiceView(referenceData);
								} else {
									new DeliveryView(referenceData);
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
				for (int i = 0; i < tableData.length; i++) {
					qty = ((BigDecimal) tableData[i][4]).negate();
					tableData[i][4] = qty;
					referenceQtys.set(i, qty);
					subtotal = (BigDecimal) tableData[i][6];
					tableData[i][6] = subtotal.negate();
				}
				referenceData.setDiscount1Total(referenceData.getDiscount1Total().negate());
				referenceData.setDiscount2Total(referenceData.getDiscount2Total().negate());
				referenceData.setTotalVat(referenceData.getTotalVat().negate());
				referenceData.setTotalVatable(referenceData.getTotalVatable().negate());
				referenceData.setComputedTotal(referenceData.getComputedTotal().negate());
				referenceData.setReferenceId(-referenceId);
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
		if (view instanceof InvoiceView) {
			Text seriesInput = ((InvoiceView) view).getSeriesInput();
			seriesInput.setTouchEnabled(true);
			seriesInput.setFocus();
		} else {
			Text enteredTotalInput = ((DeliveryView) view).getEnteredTotalInput();
			enteredTotalInput.setTouchEnabled(true);
			enteredTotalInput.setFocus();
		}
	}
}

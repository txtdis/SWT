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
	private Text txtSoId, txtOrderId, txtSeries, txtActual;
	private int referenceId, orderId;
	private boolean isRMA;
	private String module;
	private Object[][] rmaData, refData;
	private BigDecimal vatRate = Constant.getInstance().getVat();
	private ArrayList<BigDecimal> refQtys;

	public SalesOrderIdEntry(final OrderView view, final Order order) {
		txtSoId = view.getReferenceIdInput();
		txtOrderId = view.getIdInput();
		txtSeries = view.getTxtSeries();
		txtActual = view.getTxtEnteredTotal();
		module = order.getModule();
		refData = order.getData();

		new IntegerVerifier(txtSoId);
		txtSoId.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				String strRefId = txtSoId.getText().trim();
				boolean isSI = order.isAnSI();
				boolean isDR = order.isA_DR();
				if (!strRefId.isEmpty()) {
					referenceId = Integer.parseInt(strRefId);
					if (referenceId == 0) {
						new ErrorDialog("Enter only positive integers for S/O;\nnegative for P/O.");
						clearText();
					} else {
						OrderHelper helper = new OrderHelper();
						orderId = helper.getOrderId(referenceId);
						boolean isFromExTruck = helper.isFromExTruck(referenceId);
						order.setForAnExTruck(isFromExTruck);
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
							new ErrorDialog((referenceId < 0 ? "P/O" : "S/O") + " #" + Math.abs(referenceId)
							        + "\nhas already been posted\nas " + currentModule + " #" + thisId);
							orderId = 0;
							clearText();
						} else {
							Order refOrder;
							if (referenceId < 0) {
								refOrder = new PurchaseOrder(-referenceId);
							} else {
								refOrder = new SalesOrder(referenceId);
							}

							BigDecimal refComputedTotal = refOrder.getComputedTotal();
							
							if (refComputedTotal == null || refComputedTotal.equals(BigDecimal.ZERO)) {
								new ErrorDialog("S/O #" + referenceId + "\nis not in our system");
								clearText();
								return;
							}

							refQtys = refOrder.getQtys();
							refData = refOrder.getData();
							if (!isFromExTruck) {
								if (isRMA = refOrder.getComputedTotal().compareTo(BigDecimal.ZERO) < 0) {
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
										for (int i = 0; i < refData.length; i++) {
											itemId = -(int) refData[i][1];
											qtyPer = new QtyPerUOM().getQty(itemId, (String) refData[i][3]);
											if (rmMap.containsKey(itemId)) {
												rmQty = (BigDecimal) rmMap.get(itemId);
											} else {
												rmQty = BigDecimal.ZERO;
											}
											soQty = ((BigDecimal) refData[i][4]).multiply(qtyPer);
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
								if ((isSI || isDR) && !isRMA) {
									Object[][] rrData = helper.getReceivedMaterials(referenceId);
									if (rrData != null) {
										Map<Object, Object> rrMap = ArrayUtils.toMap(rrData);
										int itemId;
										BigDecimal qtyPer, soQty, rmQty, netQty, subtotal;
										BigDecimal discount1, rate1, discount2, rate2;
										BigDecimal vatable, vat;
										BigDecimal total = BigDecimal.ZERO;
										// [1]item_id, [3]uom.unit, [4]qty
										for (int i = 0; i < refData.length; i++) {
											itemId = (int) refData[i][1];
											qtyPer = new QtyPerUOM().getQty(itemId, (String) refData[i][3]);
											if (rrMap.containsKey(itemId)) {
												rmQty = (BigDecimal) rrMap.get(itemId);
											} else {
												rmQty = BigDecimal.ZERO;
											}
											soQty = ((BigDecimal) refData[i][4]).multiply(qtyPer);
											netQty = (soQty.subtract(rmQty)).divide(
											        qtyPer.equals(BigDecimal.ZERO) ? BigDecimal.ONE : qtyPer,
											        BigDecimal.ROUND_HALF_EVEN);
											refData[i][4] = netQty;
											refQtys.set(i, netQty);
											subtotal = netQty.multiply((BigDecimal) refData[i][5]);
											refData[i][6] = subtotal;
											total = total.add(subtotal);
										}
										rate1 = refOrder.getFirstLevelDiscountRate().divide(DIS.HUNDRED,
										        BigDecimal.ROUND_HALF_EVEN);
										rate2 = refOrder.getSecondLevelDiscountRate().divide(DIS.HUNDRED,
										        BigDecimal.ROUND_HALF_EVEN);
										discount1 = total.multiply(rate1);
										total = total.subtract(discount1);
										discount2 = total.multiply(rate2);
										total = total.subtract(discount2);
										vatable = total.divide(vatRate, BigDecimal.ROUND_HALF_EVEN);
										vat = total.subtract(vatable);
										refOrder.setFirstLevelDiscount(rate1.multiply(DIS.HUNDRED));
										refOrder.setSecondLevelDiscount(rate2.multiply(DIS.HUNDRED));
										refOrder.setFirstLevelDiscountTotal(discount1);
										refOrder.setSecondLevelDiscountTotal(discount2);
										refOrder.setTotalVat(vat);
										refOrder.setTotalVatable(vatable);
										refOrder.setComputedTotal(total);
										refOrder.setData(refData);
									}
								}
								txtOrderId.getShell().close();
								if (isSI) {
									new InvoiceView(refOrder);
								} else {
									new DeliveryView(refOrder);
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
		});
	}

	private void clearText() {
		txtSoId.setText("");
		txtSoId.setEditable(true);
		txtSoId.setBackground(DIS.YELLOW);
		txtSoId.setFocus();
	}

	private void next() {
		txtSoId.setTouchEnabled(false);
		if (module.contains("Invoice")) {
			txtSeries.setTouchEnabled(true);
			txtSeries.setFocus();
		} else {
			txtActual.setTouchEnabled(true);
			txtActual.setFocus();
		}
	}
}

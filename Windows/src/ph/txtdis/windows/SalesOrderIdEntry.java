package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class SalesOrderIdEntry { 
	private Text txtSoId, txtOrderId, txtSeries, txtActual;
	private int soPOid, orderId;
	private String module;
	private Object[][] rmaData, soPoData;

	public SalesOrderIdEntry(final OrderView view, final Order siDr) {
		txtSoId = view.getTxtSoId();
		txtOrderId = view.getTxtId();
		txtSeries = view.getTxtSeries();
		txtActual = view.getTxtActual();
		module = siDr.getModule();
		soPoData = siDr.getData();

		new IntegerVerifier(txtSoId);
		txtSoId.addListener (SWT.DefaultSelection, new Listener () {
			@Override
			public void handleEvent (Event e) {
				String strSoId = txtSoId.getText().trim();
				if (!strSoId.isEmpty()) {
					soPOid = Integer.parseInt(strSoId);
					if (soPOid == 0) {
						new ErrorDialog("" +
								"Enter only positive integers\n" +
								"for S/O; negative for P/O.");
						clearText();
					} else {
						OrderHelper helper = new OrderHelper();
						orderId = helper.getOrderId(soPOid);
						boolean isNotFromExTruck = !helper.isFromExTruck(soPOid);
						String thisModule;
						if (orderId != 0 && isNotFromExTruck) {
							int thisId;
							if (orderId < 0) {
								thisModule = "Delivery Report";
								thisId = -orderId;
							} else {
								thisModule = "Invoice";
								thisId = orderId;
							}
							new ErrorDialog((soPOid < 0 ? "P/O" : "S/O") + " #" + 
									+ Math.abs(soPOid) + "\nhas already been posted\n" +
									"as " + thisModule + " #" + thisId);
							orderId = 0;
							clearText();
						} else {
							Order soPo;
							if(soPOid < 0) 
								soPo = new PurchaseOrder(-soPOid);
							else	
								soPo = new SalesOrder(soPOid);
							if (soPo.getSumTotal().equals(BigDecimal.ZERO)) {
								new ErrorDialog("S/O ID " + soPOid + 
										"\nis not in our system");
								clearText();
								return;
							}
							ReceivingHelper rr = new ReceivingHelper();
							if (isNotFromExTruck) {
								if(soPo.getSumTotal().compareTo(BigDecimal.ZERO) < 0) {
									if(module.contains("Invoice")) {
										soPoData = soPo.getData();
										rmaData = 
												rr.getReceivedReturnedMaterials(soPOid);
										if(rmaData == null) {
											new ErrorDialog("" +
													"Warehouse still has not received\n"+
													"Materials to be returned\n" + 
													"per S/O #" + soPOid);
											clearText();
											return;																					
										} 
										Map<Object, Object> rmMap = 
												ArrayUtils.toMap(rmaData);
										int itemId, uom;
										BigDecimal qtyPer, soQty, rmQty;
										// [1]item_id, [3]uom.unit, [4]qty
										for (int i = 0; i < soPoData.length; i++) {
											itemId = -(int) soPoData[i][1];
											uom = new UOM((String) soPoData[i][3]).getId();
											qtyPer = new QtyPer().get(itemId, uom);
											if(rmMap.containsKey(itemId)){
												rmQty = (BigDecimal) rmMap.get(itemId);
											} else {
												rmQty = BigDecimal.ZERO; 	
											}
											soQty = ((BigDecimal) soPoData[i][4])
													.multiply(qtyPer);
											if(!rmQty.equals(soQty)) {
												new ErrorDialog("" +
														"R/R received quantity\n" +
														"differs from approved S/O;\n" +
														"correct either or both\n" +
														"before continuing.");
												clearText();
												return;
											}
										}
									} else {
										new ErrorDialog("" +
												"Negative D/R\n" + 
												"is not an option");
										clearText();
										return;
									}
								} 
								txtOrderId.getShell().close();
								if (module.contains("Invoice")) {
									new InvoiceView(soPo);							
								} else {
									new DeliveryView(soPo);
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
		txtSoId.setBackground(View.yellow());
		txtSoId.setFocus();			
	}

	private void next() {
		txtSoId.setTouchEnabled(false);
		if(module.contains("Invoice")) {
			txtSeries.setTouchEnabled(true);
			txtSeries.setFocus();
		} else {
			txtActual.setTouchEnabled(true);
			txtActual.setFocus();
		}
	}
}

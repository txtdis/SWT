package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class OrderItemIdEntry {
	private ItemHelper iHelper;
	private int itemId;
	private InvoiceLineItem lineItem;
	private Button btnPost;
	private Order order;
	private TableItem tableItem;
	private Text txtItemId;
	private String itemName;
	private boolean isSO, isDisposal, isRMA, isMonetary;
	private int rowIdx;

	public OrderItemIdEntry(
			final OrderView view, 
			final InvoiceLineItem lineItem, 
			final Order order
			) {
		this.lineItem = lineItem;
		this.order = order;
		iHelper = new ItemHelper();
		tableItem = lineItem.getTableItem();
		btnPost = view.getBtnPost();
		txtItemId  = lineItem.getTxtItemId();
		new IntegerVerifier(txtItemId);
		txtItemId.addListener (SWT.DefaultSelection, new Listener () {
			@Override
			public void handleEvent (Event ev) { 
				OrderHelper helper = new OrderHelper();
				BigDecimal actual = order.getActual();
				String module = order.getModule();
				Text txtLimit = view.getTxtActual();
				rowIdx = lineItem.getRow();
				int outletId = order.getPartnerId();
				boolean isPO = module.equals("Purchase Order");
				boolean isDR = module.equals("Delivery Report");
				boolean isSI = module.equals("Invoice");
				isRMA = false;
				isMonetary = new ItemHelper().isMonetaryType(itemId);
				isSO = module.equals("Sales Order"); 
				isDisposal = view.getTxtPartnerName().getText().trim().equals("BO DISPOSAL");
				if (StringUtils.isBlank(txtItemId.getText())) {
					BigDecimal sumTotal = order.getSumTotal();
					if (rowIdx == 0 && actual.compareTo(BigDecimal.ZERO) > 0)
						return;
					// enable posting if difference between actual & computed <= 1
					if(btnPost != null && (
							actual.subtract(sumTotal).abs().compareTo(BigDecimal.ONE) < 1
							|| new CustomerHelper().isInternalOrOthers(order.getPartnerId())
							|| isRMA 
							|| isSO
							|| isPO)) {
						btnPost.setEnabled(true);
						btnPost.setFocus();
					}
				} else {
					itemId = Integer.parseInt(txtItemId.getText());
					boolean isMonetary = iHelper.isMonetaryType(itemId);
					if(!isDR && isMonetary && !(isSI && iHelper.getShortId(itemId).equals("DINS"))) {
						clearEntry("Tax, Petty Cash or O/R" +
								"\nmust be entered only on D/Rs");
						return;						
					}
					if(actual.compareTo(BigDecimal.ZERO) >= 1 && isMonetary) {
						clearEntry("Taxes, Petty Cash or O/Rs" +
								"\n must have negative actuals");
						return;												
					}
					if(isDR && actual.compareTo(BigDecimal.ZERO) < 1 && !isMonetary) {
						clearEntry("Negative actuals are for\n" +
								"taxes, petty cash or O/Rs only");
						return;												
					}
					if(itemId < 0) {
						isRMA = true;
						// ensure RMA is only done in an S/O
						if(!isSO && !isPO) {
							clearEntry("RMA must be imported\n" +
									"from an approved S/O or P/O");
							return;
						}
						if(isSO && rowIdx == 0) {
							// check for open RMA
							int openRMA = helper.getOpenRMA(outletId);
							if (openRMA != 0) {
								new ErrorDialog("" +
										"S/O #" + openRMA + "" +
										"\nmust be closed first " +
										"\nbefore opening a new RMA"
										);
								btnPost.getShell().dispose();
								new SalesOrderView(openRMA);
								return;
							}
							order.setActual(helper.getReturnedMaterialBalance(
									outletId,
									order.getPostDate()
									));
							txtLimit.setEnabled(true);
							txtLimit.setText("" + order.getActual());
							new InfoDialog("actual: " 
									+ DIS.LNF.format(order.getActual()));
							// ensure RMA done separately per S/O
						} else if(isSO && actual.equals(BigDecimal.ZERO)) {
							clearEntry("RMA must be\ndone separately");
							return;							
						} 
						lineItem.setReturnedMaterial(true);
						itemId = Math.abs(itemId);
					} else {
						if((isSO || isPO) 
								&& rowIdx != 0 
								&& !actual.equals(BigDecimal.ZERO)) {
							clearEntry("RMA must be\nexclusively done");
							return;							
						} else {
							lineItem.setReturnedMaterial(false);
						}
					}
					if(hasDatum()) {
						txtItemId.setEditable(true);
						txtItemId.setBackground(View.yellow());
						return;
					}
					tableItem.setText(2, iHelper.getName(itemId));
					next();
				}
			}
		});
	}

	private boolean hasDatum() {
		itemName = iHelper.getName(itemId);
		if (itemName == null) {
			clearEntry("Item ID " + itemId + "\nis not in our system");
			return true;
		} 
		// check if item has been previously entered
		boolean bo = lineItem.isReturnedMaterial();
		if (order.getItemIds().contains(bo ? -itemId : itemId))  {
			clearEntry("Item ID " + itemId + "\nis already on the list");
			return true;
		}
		// check if item has price in the system
		BigDecimal unitPrice;
		if (iHelper.isMonetaryType(itemId)) {
			if(rowIdx != 0) {
				clearEntry("Taxes, Petty Cash or O/Rs" +
						"\nmust be done separately");
				return true;											
			}
			unitPrice = new BigDecimal(-1);
		} else {
			unitPrice = new Price().get(itemId, order.getPartnerId(), order.getPostDate());
		}
		if (unitPrice == null)  {
			clearEntry("Item ID " + itemId + "\nhas no price in our system");
			return true;
		}
		// check if there are available stocks when making an S/O
		BigDecimal qty = iHelper.getAvailableStock(itemId);
		BigDecimal bad = iHelper.getBadStock(itemId);
		if(isSO) {
			if(isDisposal && bad.compareTo(BigDecimal.ZERO) <= 0) {
				clearEntry("No " + itemName + "\n for disposal;\n" +
						"go to Inventory Module for details ");
				return true;
			} else if (!isRMA && !isMonetary && !isDisposal && qty.compareTo(BigDecimal.ZERO) <= 0) {
				clearEntry("No bookable\n" + itemName + ";\n" +
						"go to Inventory Module for details ");
				return true;
			}
		}
		lineItem.setQty(qty);
		lineItem.setItemId(itemId);
		if(iHelper.isMonetaryType(itemId)) {
			lineItem.setUoms(new String[] {"₱"});
		} else {
			lineItem.setUoms(new UOM().getSoldUoms(itemId));
		}
		lineItem.setUnitPrice(unitPrice);
		return false;
	}

	private void next() {
		lineItem.getBtnItemId().dispose();
		btnPost.setEnabled(false);
		lineItem.setItemName(iHelper.getName(itemId));
		Combo cmbUom = lineItem.getCmbUnit();
		// show unit price (column 5)
		tableItem.setText(5, DIS.LNF.format(lineItem.getUnitPrice()));
		// move to cmbUnit
		cmbUom.setEnabled(true);
		cmbUom.setFocus();
		cmbUom.setItems(lineItem.getUoms());
		cmbUom.select(0);
		int partnerId = order.getPartnerId();
		Date date = order.getPostDate();
		// get discount per customer & product line
		PartnerDiscount discount = new PartnerDiscount(partnerId, itemId, date);
		order.setDiscountRate1(discount.getRate1());
		order.setDiscountRate2(discount.getRate2());
	}

	protected void clearEntry(String msg){
		new ErrorDialog(msg);
		txtItemId.setText("");
		tableItem.setText(2, "");
		txtItemId.setEditable(true);
		txtItemId.setBackground(View.yellow());
	}
}

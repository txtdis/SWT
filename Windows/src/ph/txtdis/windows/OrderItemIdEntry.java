package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class OrderItemIdEntry {
	protected Order order;
	protected OrderView view;
	protected OrderHelper helper;
	protected PartnerDiscount discount;
	protected TableItem tableItem;
	private int rowIdx;
	protected int partnerId;
	protected int itemId;
	private int refId;
	protected boolean isForExTruck;
	protected Date postDate;
	protected String itemName;
	protected BigDecimal price;
	private BigDecimal enteredTotal, currentDiscount;
	private BigDecimal vat = Constant.getInstance().getVat();
	protected ItemHelper item;
	protected Text txtItemId;
	protected Text txtLimit;
	private Button btnPost;
	private Button btnItem;
	protected boolean isEnteredTotalNegative, isFirstRow;

	public OrderItemIdEntry(OrderView orderView, Order report) {
		order = report;
		view = orderView;
		btnPost = view.getBtnPost();
		btnItem = view.getBtnItem();
		txtItemId = view.getTxtItemId();
		txtLimit = view.getTxtEnteredTotal();
		txtItemId.setTouchEnabled(true);
		txtItemId.setFocus();
		rowIdx = order.getRowIdx();
		tableItem = view.getTableItem(rowIdx);
		helper = new OrderHelper();
		item = new ItemHelper();
		partnerId = order.getPartnerId();
		isForExTruck = order.isForAnExTruck();
		enteredTotal = order.getEnteredTotal();
		postDate = order.getPostDate();
		refId = order.getSoId();
		isEnteredTotalNegative = enteredTotal.signum() == -1;
		isFirstRow = rowIdx == 0;

		new IntegerVerifier(txtItemId);
		txtItemId.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event ev) {
				String strItemId = txtItemId.getText().trim();
				if (!strItemId.isEmpty())
					if (!isItemIdInputValid())
						return;
			}
		});
	}

	private boolean isItemIdInputValid() {
		itemId = Integer.parseInt(txtItemId.getText().trim().replace("(", "-").replace(")", ""));
		if (itemId < 0 && !isNegativeItemIdInputValid())
			return false;
		System.out.println("was here 0");

		if (hasBeenEnteredBefore())
			return false;
		System.out.println("was here 1");

		if (!isItemOnFile())
			return false;
		System.out.println("was here 2");

		if (isFirstRow && arePartnerReceivablesAging() && postDate.after(DIS.OVERDUE_CUTOFF))
			return false;
		System.out.println("was here 3");

		discount = new PartnerDiscount(partnerId, Math.abs(itemId), postDate);
		order.setDiscountRate1(discount.getRate1());
		order.setDiscountRate2(discount.getRate2());
		currentDiscount = order.getTotalDiscountRate();
		System.out.println("row " + isFirstRow);
		System.out.println("curdisc " + currentDiscount);
		if (isFirstRow && currentDiscount == null) {
			System.out.println("was here 3.5");
			if (isItemDiscountSameAsFromSameDayOrders())
				return false;
		} else { 
			if (!isItemDiscountSameAsPrevious())
				return false;
		}
		System.out.println("was here 4");
		
		checkIfItemBizUnitSameAsPrevious();
		System.out.println("was here 5");

		if (!isItemOnReference())
			return false;
		System.out.println("was here 6");

		if (!doesItemHavePrice())
			return false;
		System.out.println("was here 7");

		btnPost.setEnabled(false);
		txtItemId.dispose();
		btnItem.dispose();
		// column 1 is item ID
		tableItem.setText(1, DIS.NO_COMMA_INTEGER.format(itemId));
		order.setItemId(itemId);
		// column 2 is item name
		tableItem.setText(2, item.getName(itemId));
		// column 5 is unit price
		tableItem.setText(5, DIS.TWO_PLACE_DECIMAL.format(price));
		order.setPrice(price);
		// column 3 is UOM

		String subTotalText = tableItem.getText(6).replace(",", "").replace("(", "-").replace(")", "");
		if (!subTotalText.isEmpty()) {
			BigDecimal total = new BigDecimal(subTotalText);
			BigDecimal discount1 = total.multiply(order.getDiscountRate1().divide(DIS.HUNDRED,
			        BigDecimal.ROUND_HALF_EVEN));
			total = total.subtract(discount1);
			BigDecimal discount2 = total.multiply(order.getDiscountRate2().divide(DIS.HUNDRED,
			        BigDecimal.ROUND_HALF_EVEN));
			total = total.subtract(discount2);
			BigDecimal vatable = total.divide(vat, BigDecimal.ROUND_HALF_EVEN);
			BigDecimal vat = total.subtract(vatable);

			total = order.getComputedTotal().subtract(total);
			vatable = order.getTotalVatable().subtract(vatable);
			vat = order.getTotalVat().subtract(vat);
			discount1 = order.getTotalDiscount1().subtract(discount1);
			discount2 = order.getTotalDiscount2().subtract(discount2);

			view.getTxtComputedTotal().setText(DIS.TWO_PLACE_DECIMAL.format(total));
			view.getTxtTotalVatable().setText(DIS.TWO_PLACE_DECIMAL.format(vatable));
			view.getTxtTotalVat().setText(DIS.TWO_PLACE_DECIMAL.format(vat));
			view.getDiscount1().getText().setText(DIS.TWO_PLACE_DECIMAL.format(discount1));
			view.getDiscount2().getText().setText(DIS.TWO_PLACE_DECIMAL.format(discount2));

			order.setComputedTotal(total);
			order.setTotalVatable(vatable);
			order.setTotalVat(vat);

			tableItem.setText(6, "");
		}

		order.setItemId(itemId);
		order.setPrice(price);
		doNext();
		return true;
	}

	private void checkIfItemBizUnitSameAsPrevious() {
		ArrayList<String> bizUnits = order.getBizUnits();
		String currentBizUnit = item.getBizUnit(itemId);
		if (bizUnits.isEmpty()) {
			bizUnits.add(currentBizUnit);			
		} else {
			int bizUnitSize = bizUnits.size();
			final int last = bizUnitSize - 1;
			final int beforeLast = bizUnitSize - 2;
			if (!bizUnits.get(last).equals(currentBizUnit)) {
				int currentBizUnitPreviousFirstOccurance = bizUnits.subList(0, last).indexOf(currentBizUnit);
				if (currentBizUnitPreviousFirstOccurance < 0) {
					String previousBizUnits = "";
					String conjugation = " ";
					for (int i = 0; i < bizUnitSize; i++) {
						if (i == beforeLast) {
							conjugation = " and\n";
						} else if (i != last) {
							conjugation = ",\n";
						}
						previousBizUnits += (bizUnits.get(i) + conjugation);
					}
					new InfoDialog("A/n " + currentBizUnit + "\nitem was just added;\n" + previousBizUnits
					        + "\ncan no longer be entered after this");
					bizUnits.add(currentBizUnit);
				} else { // biz unit has been entered before last
					clearEntry(currentBizUnit + "\nhas been entered before,\nstarting at line #"
					        + (currentBizUnitPreviousFirstOccurance + 1));
				}
			}
		}
    }

	protected boolean isItemDiscountSameAsFromSameDayOrders() {
		BigDecimal newItemDiscount = discount.getRate();
		System.out.println("newdisc" + newItemDiscount);
		System.out.println("xtruck " + isForExTruck);
		if (!isForExTruck) {
			System.out.println("itemId " + itemId);
			System.out.println("partnerId " + partnerId);
			System.out.println("date " + postDate);
			System.out.println("type " + order.getType());
			int soIdWithSameDiscount = helper.getOrderIdWithSameDiscount(itemId, partnerId, postDate, order.getType());
			System.out.println("so " + soIdWithSameDiscount);
			if (soIdWithSameDiscount != 0) {
				clearEntry("One S/O per discount rate per outlet per day:\n" + itemName + "\nis discounted "
				        + DIS.TWO_PLACE_DECIMAL.format(newItemDiscount) + "%, the same as items in S/O #"
				        + soIdWithSameDiscount);
				txtItemId.getShell().dispose();
				new SalesOrderView(soIdWithSameDiscount);
				return true;
			}
			order.setTotalDiscountRate(newItemDiscount);
		}
		return false;
	}

    protected boolean isItemDiscountSameAsPrevious() {
		currentDiscount = order.getTotalDiscountRate();
		BigDecimal newItemDiscount = discount.getRate();
		if (!currentDiscount.equals(newItemDiscount)) {
			clearEntry("One S/O per discount rate per outlet per day:\n" + itemName + "\nis discounted "
			        + DIS.TWO_PLACE_DECIMAL.format(newItemDiscount) + "%; other items in this S/O have "
			        + DIS.TWO_PLACE_DECIMAL.format(currentDiscount));
			return false;
		}
		return true;
    }

	protected boolean hasBeenEnteredBefore() {
		ArrayList<Integer> itemIds = order.getItemIds();
		if (!itemIds.isEmpty()) {
			int lineIdWithItemid = itemIds.indexOf(itemId);
			if (lineIdWithItemid > -1 && lineIdWithItemid != rowIdx) {
				clearEntry("Item # " + itemId + "\nis already on line #" + (rowIdx + 1));
				return true;
			}
		}
		return false;
	}

	protected boolean doesItemPassOrderNeeds() {
		return true;
	}

	protected void clearEntry(String msg) {
		new ErrorDialog(msg);
		if (tableItem.getText(3).isEmpty()) {
			tableItem.setText(2, "");
			txtItemId.setText("");
		} else {
			txtItemId.setText(view.getItemIdText());
		}
		txtItemId.setEditable(true);
		txtItemId.setBackground(DIS.YELLOW);
		txtItemId.selectAll();
	}

	private boolean arePartnerReceivablesAging() {
		if (!order.getOverdue().equals(BigDecimal.ZERO)) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					new OverdueView(partnerId, DIS.OVERDUE_CUTOFF);
				}
			});
			return true;
		} else {
			return false;
		}
	}

	protected void doNext() {
		String[] uoms = new UOM().getSellingUoms(itemId);
		if (uoms != null) {
			if (uoms.length == 1) {
				tableItem.setText(3, uoms[0]);
				new OrderItemQtyEntry(view, order);
			} else {
				order.setUoms(uoms);
				new OrderItemUom(view, order);
			}
		}
	}

	protected boolean isItemOnFile() {
		itemName = item.getName(Math.abs(itemId));
		if (itemName == null) {
			clearEntry("Item #" + itemId + "\nis not on file");
			return false;
		} else {
			doesItemPassOrderNeeds();
			return true;
		}
	}

	protected boolean doesItemHavePrice() {
		// check if item has price in the system
		price = new Price().get(Math.abs(itemId), partnerId, postDate);
		price = price.negate();
		if (price.equals(BigDecimal.ZERO)) {
			clearEntry("Item #" + itemId + "\nhas no price in our system");
			return false;
		} else {
			return true;
		}
	}

	protected boolean isItemOnReference() {
		Object[] refQtyAndUom = item.getRefQtyAndUOM(itemId, refId);
		if (refQtyAndUom == null) {
			clearEntry(itemName + "\nis not in S/O #" + refId);
			return false;
		} else {
			order.setRefQty((BigDecimal) refQtyAndUom[0]);
			return true;
		}
	}

	protected boolean isNegativeItemIdInputValid() {
		return false;
	}
}

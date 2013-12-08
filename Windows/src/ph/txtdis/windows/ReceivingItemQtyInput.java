package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.HashMap;

public class ReceivingItemQtyInput extends StockTakeItemQtyInput {

	public ReceivingItemQtyInput(ReceivingView view, Receiving order) {
		super(view, order);
	}

	@Override
    protected boolean isReferenceQtyEnough(Receiving order) {
		HashMap<Integer, BigDecimal> itemIdsAndQtys = order.getItemIdsAndQtysOnList();
		int itemId = order.getItemId();
		int referenceId = order.getReferenceId();
		int uomId = order.getUomId();
		int referenceUOM = order.getReferenceUOM();
		BigDecimal referenceQty = order.getReferenceQty();
		ItemHelper item = new ItemHelper();
		
		String uom = new UOM(uomId).getUnit();
		
		itemIdsAndQtys.put(itemId, quantity);
		BigDecimal qtyOnList = itemIdsAndQtys.get(itemId);
		if (qtyOnList == null)
			qtyOnList = BigDecimal.ZERO;
		
		System.out.println(qtyOnList);
		System.out.println("uom " + uom);
		System.out.println("quantity: " + quantity);
		
		BigDecimal qtyPer = new QtyPerUOM().getQty(itemId, uom);
		BigDecimal qty = quantity.multiply(qtyPer);
		BigDecimal qtyTakenFromReference = item.getQtyTakenFromReference(itemId, referenceId);
		BigDecimal balance = referenceQty.subtract(qtyTakenFromReference).subtract(qtyOnList);
		if(qty.compareTo(balance) > 0) {
			String remainingQty = DIS.INTEGER.format(balance) + " " + uom;
			BigDecimal fullQty, brokenQty;
			if (!uom.equals("PK")) {
				fullQty = balance.divideToIntegralValue(qtyPer);
				remainingQty = DIS.INTEGER.format(fullQty) + " " + uom; 
				brokenQty = balance.subtract(fullQty.multiply(qtyPer));
				if(brokenQty.compareTo(BigDecimal.ZERO) != 0) {
					remainingQty = remainingQty + " and " + DIS.INTEGER.format(brokenQty) + " PK";
				} 
			}
			new ErrorDialog("Only\n" + remainingQty + "\nremaining");
			return false;
		}
		return true;
    }
}

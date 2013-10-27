package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;

public class SalesOrderItemIdEntry extends ItemIdInput {

	public SalesOrderItemIdEntry(OrderView orderView, Order report) {
		super(orderView, report);
	}

	@Override
	protected boolean isNegativeItemIdInputValid() {
		ArrayList<Integer> itemIds = order.getItemIds();
		if (isAtFirstRow) {
			if (itemIds.size() == 1 && !order.isAnRMA()) {
					itemIds.remove(0);
					for (int i = 2; i <= 6; i++) {
						tableItem.setText(i, "");
					}
					order.setComputedTotal(BigDecimal.ZERO);
					order.setFirstLevelDiscountTotal(BigDecimal.ZERO);
					order.setSecondLevelDiscountTotal(BigDecimal.ZERO);
					order.setTotalVatable(BigDecimal.ZERO);
					order.setTotalVat(BigDecimal.ZERO);
			}
			if (itemIds.isEmpty()) {
				// check for open RMA
				int openRmaId = getOpenRmaId(partnerId);
				if (openRmaId != 0) {
					clearTableItemEntry("S/O #" + openRmaId + "\nmust be closed first\nbefore opening a new RMA");
					txtLimit.getShell().dispose();
					new SalesOrderView(openRmaId);
					return false;
				}
				BigDecimal rmaLimit = getRmaLimit(partnerId, order.getDate()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
				order.setEnteredTotal(rmaLimit);
				txtLimit.setText(rmaLimit.toPlainString());
				order.setRMA(true);
			} 
		}  
		if (isOrderNotAnRMA()) {
			return false;
		}
		return true;
	}

	private boolean isOrderNotAnRMA() {
		if (!order.isAnRMA()) {
			clearTableItemEntry("RMA must be\ndone separately");
			return true;
		}
		return false;
	}

	@Override
	protected boolean hasItemBeenEnteredBefore() {
		if (itemId > 0 && order.isAnRMA()) {
			clearTableItemEntry("RMA must be\ndone separately");
			return true;
		}
		return super.hasItemBeenEnteredBefore();
	}

	@Override
	protected boolean isItemOnReferenceOrder() {
		return true;
	}

	@Override
	protected boolean isItemBizUnitSameAsPrevious() {
		if (order.isAnRMA())
			return true;
		return super.isItemBizUnitSameAsPrevious();
	}
	
	public int getOpenRmaId(int outletId) {
		// @sql:on
		Object object = new Data().getDatum(new Object[] { outletId, outletId },""
				+ "WITH invoices "
				+ "		AS (SELECT ref_id AS id "
				+ "			  FROM invoice_header "
				+ " 		 WHERE 	   customer_id = ? "
				+ "				   AND actual < 0 ), "
				+ "		sales_orders "
				+ "		AS (SELECT DISTINCT sd.sales_id AS id "
				+ "			  FROM sales_header AS sh	"
				+ "				   INNER JOIN sales_detail AS sd "
				+ "					  ON sd.sales_id = sh.sales_id "
				+ "			 WHERE 	   sh.customer_id = ? "
				+ "				   AND sd.item_id < 0 ) "
				+ "  SELECT s.id "
				+ "    FROM sales_orders AS s "
				+ "         LEFT JOIN invoices AS i "
				+ "           ON s.id = i.id "
				+ "  WHERE i.id IS null");
		// @sql:off
		return object == null ? 0 : (int) object;
	}
	
	public BigDecimal getRmaLimit(int outletId, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.YEAR, -1);
		Date start = new Date(cal.getTimeInMillis());
		// @sql:on
		Object object = new Data().getDatum(new Object[] { outletId, start, date },""
				+ "WITH invoices "
				+ "     AS (SELECT invoice_id AS id "
				+ "			  FROM invoice_header "
				+ "		 	 WHERE	   customer_id = ? "
				+ "				   AND invoice_date BETWEEN ? AND ? ), "
				+ "		sold "
				+ "		AS (SELECT sum(CASE WHEN actual IS NULL "
				+ "						  THEN 0 ELSE actual END) AS sale "
				+ "			  FROM invoice_header AS ih "
				+ "			  	   INNER JOIN invoices AS i ON	ih.invoice_id = i.id "
				+ "	WHERE	actual >= 0 "
				+ "), returned AS ( "
				+ "	SELECT 	sum(CASE WHEN actual IS NULL "
				+ "				THEN 0 ELSE actual END) AS rebate    "
				+ "	FROM 	invoice_header AS ih"
				+ "	INNER JOIN invoices AS i ON	ih.invoice_id = i.id "
				+ "	WHERE	actual < 0 "
				+ ") "
				+ "SELECT CASE WHEN sale IS NULL THEN 0 ELSE sale END "
				+ "		* 0.01 "
				+ "		- CASE WHEN rebate IS NULL "
				+ "			THEN 0 ELSE rebate END "
				+ "FROM sold, returned");
		// @sql:off
		return object == null ? BigDecimal.ZERO : (BigDecimal) object;
	}
}

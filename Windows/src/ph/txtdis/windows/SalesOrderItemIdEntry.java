package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;

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
				for (int i = 2; i <= 6; i++) 
					tableItem.setText(i, "");
				order.setComputedTotal(BigDecimal.ZERO);
				order.setDiscount1Total(BigDecimal.ZERO);
				order.setDiscount2Total(BigDecimal.ZERO);
				order.setTotalVatable(BigDecimal.ZERO);
				order.setTotalVat(BigDecimal.ZERO);
			}
			if (itemIds.isEmpty()) {
				// check for open RMA
				Object[] openRmaId = getOpenRmaId(partnerId);
				if (openRmaId != null) {
					int id = (int) openRmaId[0];
					if (openRmaId.length == 1) {
						clearTableItemEntry("S/O #" + id + "\nmust be closed first\nbefore opening a new RMA");
						txtLimit.getShell().dispose();
						new SalesOrderView(id);
					} else {
						String series = (String) openRmaId[1];
						clearTableItemEntry("Negative S/I #" + id + series + "\nmust be used first\nbefore opening a new RMA");
						txtLimit.getShell().dispose();
						new InvoiceView(id, series);
					}
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

	public Object[] getOpenRmaId(int outletId) {
		return new Data().getData(new Object[] { outletId, DIS.NO_SO_WITH_OVERDUE_CUTOFF },""
				// @sql:on
				+ "SELECT CASE WHEN i.invoice_id IS NULL THEN sh.sales_id ELSE i.invoice_id END AS id, "
				+ "       CASE WHEN i.series IS NULL THEN NULL ELSE i.series END AS series "
				+ "  FROM sales_header AS sh "
				+ "	      INNER JOIN sales_detail AS sd "
				+ "	         ON     sd.sales_id = sh.sales_id "
				+ "	            AND sd.item_id < 0 "
				+ "	            AND sh.customer_id = ? "
				+ "             AND sh.sales_date > ? "
				+ "	       LEFT JOIN invoice_header AS i "
				+ "	         ON i.ref_id = sh.sales_id "
				+ "	       LEFT JOIN remittance_detail AS r "
				+ "	         ON     r.order_id = i.invoice_id "
				+ "	            AND r.series = i.series "
				+ "	WHERE    i.invoice_id IS NULL "
				+ "	      OR r.remit_id IS NULL "
				+ " ORDER BY sh.sales_id "
				+ "	LIMIT 1;"
				// @sql:off
		        );
	}

	public BigDecimal getRmaLimit(int outletId, Date date) {
		Object object = new Data().getDatum(new Object[] { outletId, DIS.addYears(date, -1), date },""
				// @sql:on
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
				+ "	         WHERE actual >= 0 "
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
				+ "FROM sold, returned"
				// @sql:off
				);
		return object == null ? BigDecimal.ZERO : (BigDecimal) object;
	}
}

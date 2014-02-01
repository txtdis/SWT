package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PurchaseData extends OrderData {
	private String bizUnit;
	private int count;

	public PurchaseData(int orderId) {
		super(orderId);
	}

	public PurchaseData(String bizUnit, int count) {
		super();
		this.bizUnit = bizUnit;
		this.count = count;
	}

	@Override
	protected void setProperties() {
		type = Type.PURCHASE;
		referenceAndActualStmt ="" 
				// @sql:on
				+ " CAST(0 AS NUMERIC(10,2)) AS actual, " 
				+ " CAST(0 AS INT) AS ref_id, "
		        + " CAST(0 AS NUMERIC(10,2)) AS payment, "
				// @sql:off
		;
		partnerId = DIS.PRINCIPAL;
		date = DIS.TOMORROW;
		leadTime = DIS.LEAD_TIME;
	}

	public void computeBasedOnVolume() {
	    tableData = new Query().getTableData(bizUnit,"" 
	    			// @sql:on
	    			+ Item.addParentChildCTE() + ",\n"
	    			+ Inventory.addCTE() + ",\n"
	    			+ SQL.addSTTperDayStmt() + ",\n"
	    	    	+ "     latest_purchase_receipt\n"
	    	        + "     AS (  SELECT rh.partner_id AS vendor_id,\n" 
	    			+ "                  CASE\n"
	    	        + "                     WHEN max (receiving_date) < current_date THEN (current_date - 1)\n"
	    	        + "                     ELSE max (receiving_date)\n" 
	    	        + "                  END\n"
	    	        + "                     AS receipt_date\n" 
	    	        + "             FROM receiving_header AS rh\n"
	    	        + "            WHERE rh.partner_id = 488\n" 
	    	        + "         GROUP BY rh.partner_id),\n"
	    	        + "     open_purchased_items\n"
	    	        + "     AS (  SELECT pd.item_id, sum (pd.qty * qp.qty) AS qty\n"
	    	        + "             FROM vendor_specific AS vs\n"
	    	        + "                  INNER JOIN latest_purchase_receipt AS lpr\n"
	    	        + "                     ON vs.vendor_id = lpr.vendor_id\n"
	    	        + "                  INNER JOIN purchase_header AS ph\n"
	    	        + "                     ON lpr.vendor_id = ph.customer_id\n"
	    	        + "                  INNER JOIN purchase_detail AS pd\n"
	    	        + "                     ON     ph.purchase_id = pd.purchase_id\n"
	    	        + "                        AND (ph.purchase_date + vs.lead_time) > lpr.receipt_date\n"
	    	        + "                  INNER JOIN qty_per AS qp\n"
	    	        + "                     ON qp.item_id = pd.item_id AND qp.uom = pd.uom\n"
	    	        + "         GROUP BY pd.item_id),\n" 
	    	        + "     latest_pricelist\n"
	    	        + "     AS (  SELECT p.item_id, max (p.start_date) AS start_date\n"
	    	        + "             FROM price AS p\n"
	    	        + "                  INNER JOIN parent_child AS ip ON p.item_id = ip.child_id\n"
	    	        + "                  INNER JOIN item_family AS if\n"
	    	        + "                     ON ip.parent_id = if.id AND if.name = ?\n"
	    	        + "            WHERE p.tier_id = 0\n" 
	    	        + "         GROUP BY p.item_id),\n"
	    	        + "     latest_price\n" 
	    	        + "     AS (SELECT price.item_id, price\n" 
	    	        + "           FROM price\n"
	    	        + "                INNER JOIN latest_pricelist AS latest\n"
	    	        + "                   ON     latest.item_id = price.item_id\n"
	    	        + "                      AND latest.start_date = price.start_date\n"
	    	        + "                      AND tier_id = 0)\n" 
	    	        + "  SELECT CAST (row_number() OVER (ORDER BY stt.qty / report.qty DESC) AS line_id AS int),\n" 
	    	        + "         stt.id,\n"
	    	        + "         im.name,\n" 
	    	        + "         uom.unit,\n" 
	    	        + "         0.0 AS qty,\n"
	    	        + "         price * buy.qty AS purchase_price,\n" 
	    	        + "         0.0 AS subtotal,\n"
	    	        + "         stt.qty / report.qty AS daily_stt,\n"
	    	        + "           (  CASE WHEN inv.good IS NULL THEN 0 ELSE inv.good END\n"
	    	        + "            + CASE WHEN open.qty IS NULL THEN 0 ELSE open.qty END)\n"
	    	        + "         / report.qty\n" 
	    	        + "            AS incoming_and_good_stock,\n"
	    	        + "         report.qty / buy.qty AS report_to_buy_qty_factor\n"
	    	        + "    FROM stt_per_day AS stt\n" 
	    	        + "         LEFT JOIN inventory AS inv ON stt.id = inv.id\n"
	    	        + "         LEFT JOIN open_purchased_items AS open ON stt.id = open.item_id\n"
	    	        + "         INNER JOIN qty_per AS buy ON stt.id = buy.item_id AND buy.buy IS TRUE\n"
	    	        + "         INNER JOIN qty_per AS report\n"
	    	        + "            ON stt.id = report.item_id AND report.report IS TRUE\n"
	    	        + "         INNER JOIN item_header AS im ON stt.id = im.id\n"
	    	        + "         INNER JOIN uom ON buy.uom = uom.id\n"
	    	        + "         INNER JOIN latest_price AS lp ON stt.id = lp.item_id\n"
	    	        + "ORDER BY stt.qty / report.qty DESC\n"
	    	        // @sql:off
	            );
	    BigDecimal target = new BigDecimal(count);
	    BigDecimal iteration = BigDecimal.ONE;
	    BigDecimal total = BigDecimal.ZERO;
	    boolean isTargetHigherTotal = true;

	    BigDecimal lastIteration, oldSubtotal, newSubtotal, stt, stock, reportToBuyQtyFactor, balance;
	    BigDecimal buyPrice, qtyInDataArray, subtotalInDataArray, qtyInPurchaseUOM;

	    while (isTargetHigherTotal) {
	    	for (int j = 0; j < tableData.length; j++) {
	    		stt = (BigDecimal) tableData[j][7];
	    		stock = (BigDecimal) tableData[j][8];
	    		reportToBuyQtyFactor = (BigDecimal) tableData[j][9];
	    		lastIteration = iteration.subtract(BigDecimal.ONE);

	    		// roundup previous iteration to buying uom then compute
	    		// back to report uom as the rounded-up buying qty was
	    		// the basis of saved converted value in the running
	    		// total

	    		oldSubtotal = stt.multiply(lastIteration).subtract(stock).multiply(reportToBuyQtyFactor);
	    		if (DIS.isNegative(oldSubtotal))
	    			oldSubtotal = BigDecimal.ZERO;

	    		newSubtotal = stt.multiply(iteration).subtract(stock);
	    		if (DIS.isNegative(newSubtotal))
	    			newSubtotal = BigDecimal.ZERO;

	    		total = total.subtract(oldSubtotal);
	    		if (DIS.isNegative(total))
	    			total = BigDecimal.ZERO;

	    		balance = target.subtract(total);
	    		qtyInDataArray = ((BigDecimal) tableData[j][4]);
	    		buyPrice = (BigDecimal) tableData[j][5];
	    		subtotalInDataArray = ((BigDecimal) tableData[j][6]);

	    		if (!DIS.isNegative(balance))
	    			qtyInPurchaseUOM = balance.setScale(0).multiply(reportToBuyQtyFactor);
	    		else
	    			qtyInPurchaseUOM = newSubtotal.setScale(0).multiply(reportToBuyQtyFactor);

	    		total = total.add(DIS.divide(qtyInPurchaseUOM, reportToBuyQtyFactor));
	    		tableData[j][4] = qtyInDataArray.add(qtyInPurchaseUOM);
	    		tableData[j][6] = subtotalInDataArray.add(qtyInPurchaseUOM.multiply(buyPrice));
	    		isTargetHigherTotal = target.compareTo(total) >= 0;
	    	}
	    	iteration = iteration.add(BigDecimal.ONE);
	    }

	    if (tableData != null) {
	    	ArrayList<Object[]> dataList = new ArrayList<>(tableData.length);
	    	itemIds = getItemIds();
	    	uoms = getUoms();
	    	quantities = getQtys();
	    	for (Object[] objects : tableData)
	    		if (((BigDecimal) objects[4]).compareTo(BigDecimal.ZERO) > 0) {
	    			dataList.add(objects);
	    			computedTotal = computedTotal.add((BigDecimal) objects[6]);
	    			itemIds.add((Integer) objects[1]);
	    			uoms.add(Type.valueOf((String) objects[3]));
	    			quantities.add((BigDecimal) objects[4]);
	    		}
	    	tableData = dataList.toArray(new Object[dataList.size()][]);
	    }

	    BigDecimal discount1Percent = PartnerDiscount.getVendorDiscount();
	    totalDiscount1 = computedTotal.multiply(DIS.getRate(discount1Percent));
	    computedTotal = computedTotal.subtract(totalDiscount1);
	    totalVatable = DIS.divide(computedTotal, DIS.VAT);
	    totalVat = computedTotal.subtract(totalVatable);
    }

	public void computeBasedOnDaysLevel() {
	    tableData = new Query().getTableData(new Object[] { bizUnit, count },""
	    			// @sql:on
	    			+ Item.addParentChildCTE() + ",\n"
	    			+ Inventory.addCTE() + ",\n"
	    			+ SQL.addSTTperDayStmt() + ",\n"
	    	        + "     latest_purchase_receipt\n" 
	    			+ "     AS (  SELECT rh.partner_id AS vendor_id,\n"
	    	        + "                  CASE\n"
	    	        + "                     WHEN max (receiving_date) < current_date THEN (current_date - 1)\n"
	    	        + "                     ELSE max (receiving_date)\n" 
	    	        + "                  END\n"
	    	        + "                     AS receipt_date\n" 
	    	        + "             FROM receiving_header AS rh\n"
	    	        + "            WHERE rh.partner_id = 488\n" 
	    	        + "         GROUP BY rh.partner_id),\n"
	    	        + "     open_purchased_items\n" 
	    	        + "     AS (  SELECT pd.item_id, sum (pd.qty) AS qty\n"
	    	        + "             FROM vendor_specific AS vs\n"
	    	        + "                  INNER JOIN latest_purchase_receipt AS lpr\n"
	    	        + "                     ON vs.vendor_id = lpr.vendor_id\n"
	    	        + "                  INNER JOIN purchase_header AS ph\n"
	    	        + "                     ON lpr.vendor_id = ph.customer_id\n"
	    	        + "                  INNER JOIN purchase_detail AS pd\n"
	    	        + "                     ON     ph.purchase_id = pd.purchase_id\n"
	    	        + "                        AND (ph.purchase_date + vs.lead_time) > lpr.receipt_date\n"
	    	        + "         GROUP BY pd.item_id),\n" 
	    	        + "     latest_pricelist\n"
	    	        + "     AS (  SELECT p.item_id, max (p.start_date) AS start_date\n"
	    	        + "             FROM price AS p\n"
	    	        + "                  INNER JOIN parent_child AS ip ON p.item_id = ip.child_id\n"
	    	        + "                  INNER JOIN item_family AS if\n"
	    	        + "                     ON ip.parent_id = if.id AND if.name = ?\n"
	    	        + "            WHERE p.tier_id = 0\n" 
	    	        + "         GROUP BY p.item_id),\n"
	    	        + "     latest_price\n" 
	    	        + "     AS (SELECT price.item_id, price\n" 
	    	        + "           FROM price\n"
	    	        + "                INNER JOIN latest_pricelist AS latest\n"
	    	        + "                   ON     latest.item_id = price.item_id\n"
	    	        + "                      AND latest.start_date = price.start_date\n"
	    	        + "                      AND tier_id = 0),\n" 
	    	        + "     level AS (SELECT ? AS days),\n"
	    	        + "     summary\n" 
	    	        + "     AS (SELECT stt.id,\n" 
	    	        + "                im.name,\n"
	    	        + "                uom.unit,\n" 
	    	        + "                  ceiling (\n"
	    	        + "                       (  stt.qty * days\n" 
	    	        + "                        - (CASE\n"
	    	        + "                              WHEN inv.good IS NULL THEN 0\n"
	    	        + "                              ELSE inv.good\n" 
	    	        + "                           END))\n"
	    	        + "                     / qp.qty)\n"
	    	        + "                - (CASE WHEN open.qty IS NULL THEN 0 ELSE open.qty END)\n"
	    	        + "                   AS qty,\n" 
	    	        + "                price\n" 
	    	        + "           FROM level,\n"
	    	        + "                stt_per_day AS stt\n"
	    	        + "                LEFT OUTER JOIN inventory AS inv ON stt.id = inv.id\n"
	    	        + "                LEFT OUTER JOIN open_purchased_items AS open\n"
	    	        + "                   ON stt.id = open.item_id\n"
	    	        + "                INNER JOIN qty_per AS qp\n"
	    	        + "                   ON stt.id = qp.item_id AND qp.buy IS TRUE\n"
	    	        + "                INNER JOIN item_header AS im\n" 
	    	        + "					ON stt.id = im.id\n"
	    	        + "				   AND im.type_id <> 5\n" 
	    	        + "                INNER JOIN uom ON qp.uom = uom.id\n"
	    	        + "                INNER JOIN latest_price AS lp ON stt.id = lp.item_id)\n"
	    	        + "  SELECT row_number () OVER (ORDER BY qty DESC) AS line,\n" 
	    	        + "         id,\n"
	    	        + "         name,\n" 
	    	        + "         unit,\n" 
	    	        + "         qty,\n" 
	    	        + "         price,\n"
	    	        + "         qty * price AS subtotal\n" 
	    	        + "    FROM summary\n" 
	    	        + "   WHERE qty > 0\n"
	    	        + "ORDER BY qty DESC;\n"
	    	        // @sql:off
	            );
    }
}

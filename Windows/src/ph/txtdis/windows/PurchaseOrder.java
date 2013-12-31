package ph.txtdis.windows;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class PurchaseOrder extends Order {

	public PurchaseOrder(int orderId) {
		this(orderId, null, null, null);
	}

	public PurchaseOrder(int orderId, String bizUnit, Boolean isDayNotUomBased, Integer uomOrDayCount) {
		super(orderId);
		if (isDayNotUomBased != null) {
			if (isDayNotUomBased) {
				data = new Data().getDataArray(new Object[] { bizUnit, uomOrDayCount }, ""
						// @sql:on
						+ "WITH " + SQL.addInventoryStmt() + ",\n"
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
				        + "                INNER JOIN item_master AS im\n" 
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
			} else {
				data = new Data().getDataArray(bizUnit, "" 
						// @sql:on
						+ SQL.addItemParentStmt() + ",\n"
						+ SQL.addInventoryStmt() + ",\n"
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
				        + "  SELECT 0 AS line,\n" 
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
				        + "         INNER JOIN item_master AS im ON stt.id = im.id\n"
				        + "         INNER JOIN uom ON buy.uom = uom.id\n"
				        + "         INNER JOIN latest_price AS lp ON stt.id = lp.item_id\n"
				        + "ORDER BY stt.qty / report.qty DESC\n"
				        // @sql:off
				        );
				BigDecimal target = new BigDecimal(uomOrDayCount);
				int i = 1;
				BigDecimal total = BigDecimal.ZERO;
				boolean isTargetHigherTotal = true;
				while (isTargetHigherTotal) {
					for (int j = 0; j < data.length; j++) {
						BigDecimal stt = (BigDecimal) data[j][7];
						BigDecimal stock = (BigDecimal) data[j][8];
						BigDecimal reportToBuyQtyFactor = (BigDecimal) data[j][9];
						// roundup previous iteration to buying uom then compute back to report uom
						// as the rounded-up buying qty was the basis of saved converted value
						// in the running total
						BigDecimal oldSubtotal = (stt.multiply(new BigDecimal(i - 1).subtract(stock)).multiply(
						        reportToBuyQtyFactor).setScale(0, RoundingMode.UP)).divide(reportToBuyQtyFactor,
						        RoundingMode.HALF_EVEN);
						if (oldSubtotal.compareTo(BigDecimal.ZERO) < 0)
							oldSubtotal = BigDecimal.ZERO;
						BigDecimal newSubtotal = (stt.multiply(new BigDecimal(i)).subtract(stock)).setScale(0,
						        RoundingMode.HALF_EVEN);
						if (newSubtotal.compareTo(BigDecimal.ZERO) < 0)
							newSubtotal = BigDecimal.ZERO;
						// compute balance excluding previous iteration's qty
						total = total.subtract(oldSubtotal);
						if (total.compareTo(BigDecimal.ZERO) < 0)
							total = BigDecimal.ZERO;
						BigDecimal balance = target.subtract(total);
						BigDecimal buyPrice = (BigDecimal) data[j][5];
						BigDecimal qtyInBuyUnits;
						if (newSubtotal.compareTo(balance) >= 0) {
							qtyInBuyUnits = balance.multiply(reportToBuyQtyFactor).setScale(0, RoundingMode.UP);
							total = total.add(qtyInBuyUnits.divide(reportToBuyQtyFactor, RoundingMode.HALF_EVEN));
							data[j][4] = ((BigDecimal) data[j][4]).add(qtyInBuyUnits);
							data[j][6] = ((BigDecimal) data[j][6]).add(qtyInBuyUnits.multiply(buyPrice));
							isTargetHigherTotal = false;
							break;
						} else {
							qtyInBuyUnits = newSubtotal.multiply(reportToBuyQtyFactor).setScale(0, RoundingMode.UP);
							total = total.add(qtyInBuyUnits.divide(reportToBuyQtyFactor, RoundingMode.HALF_EVEN));
							data[j][4] = ((BigDecimal) data[j][4]).add(qtyInBuyUnits);
							data[j][6] = ((BigDecimal) data[j][6]).add(qtyInBuyUnits.multiply(buyPrice));
							if (total.compareTo(target) >= 0) {
								isTargetHigherTotal = false;
								break;
							}
						}
					}
					if (total.compareTo(target) >= 0) {
						isTargetHigherTotal = false;
						// break;
					} else {
						i++;
					}

				}
			}
			if (data != null) {
				ArrayList<Object[]> dataList = new ArrayList<>(data.length);
				itemIds = getItemIds();
				uomIds = getUomIds();
				qtys = getQtys();
				for (Object[] objects : data)
					if (((BigDecimal) objects[4]).compareTo(BigDecimal.ZERO) > 0) {
						dataList.add(objects);
						computedTotal = computedTotal.add((BigDecimal) objects[6]);
						itemIds.add((Integer) objects[1]);
						uomIds.add(new UOM((String) objects[3]).getId());
						qtys.add((BigDecimal) objects[4]);
					}
				data = dataList.toArray(new Object[dataList.size()][]);
			}
			firstLevelDiscount = (BigDecimal) new Data().getDatum(DIS.PRINCIPAL, ""
					// @sql:on
					+ "SELECT level_1\n"
			        + "  FROM discount\n" 
					+ " WHERE customer_id = ?"
			        // @sql:off
			        );
			if (firstLevelDiscount == null)
				firstLevelDiscount = BigDecimal.ZERO;
			System.out.println("total: " + computedTotal);
			System.out.println("disc: " + firstLevelDiscount);
			totalDiscount1 = computedTotal.multiply(firstLevelDiscount).divide(new BigDecimal(100));
			computedTotal = computedTotal.subtract(totalDiscount1);
			totalVatable = computedTotal.divide(BigDecimal.ONE.add(DIS.VAT), 10,
			        BigDecimal.ROUND_HALF_EVEN);
			totalVat = computedTotal.subtract(totalVatable);
			rowIdx = data.length;
		}
	}

	@Override
	protected void setData() {
		module = "Purchase Order";
		type = "purchase";
		reference = "" 
				+ " CAST(0 AS NUMERIC(10,2)) AS actual, " 
				+ " CAST(0 AS INT) AS ref_id, "
		        + " CAST(0 AS NUMERIC(10,2)) AS payment, ";
		partnerId = 488;
		date = DIS.TOMORROW;
		leadTime = (int) new Data().getDatum(partnerId, ""
		        + "SELECT CASE WHEN lead_time IS NULL THEN 0 ELSE lead_time END AS lead_time "
		        + "FROM	vendor_specific " 
		        + "WHERE	vendor_id = ? " + "");
	}
}

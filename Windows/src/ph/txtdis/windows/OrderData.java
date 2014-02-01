package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public abstract class OrderData extends InputData implements ItemQuantifiable {

	protected ArrayList<BigDecimal> quantities;
	protected ArrayList<Integer> itemIds;
	protected ArrayList<Type> uoms;
	protected BigDecimal computedTotal, enteredTotal, discount1Percent, totalDiscount1, totalVatable, totalVat,
	        quantity, referenceQuantity;
	protected Date dueDate, inputDate;
	protected Object[] headerData;
	protected OrderControl helper;
	protected String address, inputter, series, referenceAndActualStmt;
	protected String[] units;
	protected Time inputTime;
	protected Type uom;
	protected boolean isAnSO, isA_PO, isA_DR, isAnRMA, isAnRR, isAnSI, isPartnerFromAnExTruckRoute, isForAnExTruck,
	        isFromAnExTruck, isForDisposal, isForSpecialCustomer, isDealerIncentive, isMaterialTransfer,
	        isReferenceAnSO, isSalaryCredit;
	protected int partnerId, referenceId, leadTime, itemId;
	protected long timestamp;

	private ArrayList<String> bizUnits;
	private BigDecimal overdue, totalDiscountRate, discount2Percent, totalDiscount2, price, volumeDiscountQty,
	        volumeDiscountValue, subtotal;
	private String partner, route, bizUnit;

	public OrderData() {
		super();
		// @sql:on
		tableHeaders = new String[][] { { StringUtils.center("#", 3), "Line" },
				{ StringUtils.center("ID", 6), "ID" },
				{ StringUtils.center("PRODUCT NAME", 18), "String" },
				{ StringUtils.center("UOM", 5), "String" },
				{ StringUtils.center("QTY", 9), "BigDecimal" },
				{ StringUtils.center(DIS.$ + " PRICE", 9), "BigDecimal" },
				{ StringUtils.center(DIS.$ + " SUBTOTAL", 12), "BigDecimal" } };
		// @sql:off

		quantities = new ArrayList<>();
		itemIds = new ArrayList<>();
		uoms = new ArrayList<>();
		computedTotal = BigDecimal.ZERO;
		enteredTotal = BigDecimal.ZERO;
		discount1Percent = BigDecimal.ZERO;
		discount2Percent = BigDecimal.ZERO;
		totalDiscount1 = BigDecimal.ZERO;
		totalDiscount2 = BigDecimal.ZERO;
		totalVatable = BigDecimal.ZERO;
		totalVat = BigDecimal.ZERO;
		computedTotal = BigDecimal.ZERO;
		computedTotal = BigDecimal.ZERO;
		computedTotal = BigDecimal.ZERO;
		inputDate = DIS.TODAY;
		inputTime = DIS.ZERO_TIME;
	}

	public OrderData(int id) {
		this(id, null);
	}

	public OrderData(Object[] id) {
		this((int) id[0], (String) id[2]);
	}

	public OrderData(int id, String series) {
		this();
		this.series = series;
		id = Math.abs(id);
		setProperties();
		switch (type) {
		case DELIVERY:
			isA_DR = true;
			break;
		case INVOICE:
			isAnSI = true;
			break;
		case PURCHASE:
			isA_PO = true;
			break;
		case RECEIVING:
			isAnRR = true;
			break;
		case SALES:
			isAnSO = true;
			break;
		default:
			break;
		}
		if (id == 0)
			return;

		String cteOrder ="" 
				// @sql:on
				+ "order_table AS ( " 
				+ "	SELECT	h." + type + "_id AS order_id, "
				+ (isAnSI ? "	h.series, " : "")
				+ "			h.customer_id, "
				+ "			h." + type + "_date AS order_date, "
				+ referenceAndActualStmt
				+ "			h.user_id, "
				+ "			h.time_stamp, "
				+ "			d.line_id, "
				+ "			abs(d.item_id) AS item_id, "
				+ "			d.qty, "
				+ "			d.uom, "
				+ "			d.qty * qp.qty AS pcs, "
				+ "			qp.qty AS qty_per,"
				+ "			CASE WHEN d.item_id < 0 THEN true ELSE false END AS is_rma "
				+ "	FROM " + type + "_header AS h " 
				+ "	        INNER JOIN " + type + "_detail AS d " 
				+ "		       ON     h." + type + "_id = d." + type + "_id " 
				+ (isAnSI ? "	  AND h.series = d.series " : "")
				+ "	        INNER JOIN qty_per AS qp " 
				+ "		       ON     d.uom = qp.uom "
				+ "			      AND abs(d.item_id) = qp.item_id " 
				+ "	WHERE h." + type + "_id = ? " 
				+ (isAnSI ? "	  AND h.series = ? " : "")
				;

		String ctePrice = ""
				// @sql:on
				+ "latest_price_start_date_per_order AS ( "
				+ "	SELECT	ot.order_id, "
				+ "			ot.item_id, "
				+ "			p.tier_id,"
				+ "			ot.is_rma, "
				+ "			max(p.start_date) AS max_date "
				+ "	FROM order_table AS ot "
				+ "	LEFT JOIN  price AS p "
				+ "	ON ot.item_id = p.item_id "
				+ "	LEFT JOIN parent_child AS pc "
				+ "	ON ot.item_id = pc.child_id "
				+ "	LEFT JOIN channel_price_tier AS cpt "
				+ "	ON p.tier_id = cpt.tier_id "
				+ "		AND cpt.family_id = pc.parent_id "
				+ "	INNER JOIN customer_header AS cm "
				+ "	ON ot.customer_id = cm.id "
				+ "		AND cm.type_id = cpt.channel_id "
				+ "	WHERE p.start_date <= ot.order_date "
				+ "	GROUP BY ot.order_id, "
				+ "			ot.item_id, "
				+ "			p.tier_id,"
				+ "			ot.is_rma "
				+ "), prices AS ( "
				+ "	SELECT	pd.order_id, "
				+ "			pd.item_id, "
				+ "			CASE WHEN pd.is_rma THEN -p.price ELSE p.price END AS price "
				+ "	FROM latest_price_start_date_per_order AS pd "
				+ "	LEFT JOIN  price AS p "
				+ "	ON pd.max_date = p.start_date "
				+ "		AND pd.tier_id = p.tier_id "
				+ "		And pd.item_id = p.item_id "
				;

		String cteVolumeDiscount = ""
				+ "latest_volume_discount_start_date_per_order AS ( "
				+ "	SELECT		ot.order_id, " + "				ot.item_id, "
				+ "				max(d.start_date) AS max_date "
				+ "	FROM 		order_table AS ot "
				+ "	INNER JOIN 	volume_discount AS d "
				+ "	ON 			ot.item_id = d.item_id "
				+ "	WHERE 		d.start_date <= ot.order_date "
				+ "	GROUP BY 	ot.order_id, " + "				ot.item_id " + "), "
				+ "volume_discounts AS ( " 
				+ "	SELECT	dd.order_id, "
				+ "			d.item_id, " 
				+ "			d.uom, " 
				+ "			d.per_qty, "
				+ "			d.less "
				+ "	FROM latest_volume_discount_start_date_per_order AS dd "
				+ "	INNER JOIN volume_discount AS d "
				+ "	ON dd.max_date = d.start_date "
				+ "		AND dd.item_id = d.item_id "
				;

		Object[] parameters = (isAnSI ? new Object[] { id, series }: new Object[] { id });
		
		tableData = sql.getTableData(parameters, ""
				// @sql:on
				+ Item.addParentChildCTE() + ", "
				+ cteOrder + "), "
				+ ctePrice + "), "
				+ cteVolumeDiscount + ") "
				+ "SELECT ot.line_id,\n" 
				+ "		 CASE WHEN ot.is_rma IS TRUE THEN -ot.item_id ELSE ot.item_id END AS item_id,\n" 
				+ "		 im.short_id,\n" 
				+ "		 uom.unit,\n" 
				+ "		 ot.qty,\n" 
				+ "		 CASE WHEN ot.qty = 0 "
				+ "          THEN 0\n" 
				+ "			 ELSE\n" 
				+ "				   (  p.price * ot.qty_per * ot.qty\n" 
				+ "					-	CASE WHEN less IS NULL THEN 0 ELSE less END\n" 
				+ "					  * round (\n" 
				+ "							  ot.qty_per\n" 
				+ "							* ot.qty\n" 
				+ "							/ CASE WHEN d.per_qty IS NULL THEN 1 ELSE d.per_qty END,\n" 
				+ "							0))\n" 
				+ "				 / ot.qty\n" 
				+ "		     END AS price,\n" 
				+ "		 (	p.price * ot.qty_per * ot.qty\n" 
				+ "		  -   CASE WHEN less IS NULL THEN 0 ELSE less END\n" 
				+ "			* round (ot.qty_per * ot.qty / CASE WHEN d.per_qty IS NULL THEN 1 ELSE d.per_qty END, 0))\n" 
				+ "			 AS subtotal,\n" 
				+ "		 im.short_id\n" 
				+ (isAnSO ? ", if.id " : "")
				+ "	FROM item_header AS im\n" 
				+ "		 INNER JOIN order_table AS ot ON ot.item_id = im.id\n" 
				+ "		 INNER JOIN uom ON ot.uom = uom.id\n" 
				+ (!isAnSO && !isA_PO ? "" : 
				( "		 INNER JOIN parent_child AS ip ON ot.item_id = ip.child_id\n" 
				+ "		 INNER JOIN item_family AS if ON ip.parent_id = if.id AND if.tier_id = 1\n")) 
				+ "		 LEFT JOIN prices AS p ON p.item_id = ot.item_id\n" 
				+ "		 LEFT JOIN volume_discounts AS d ON ot.item_id = d.item_id\n" 
				+ "ORDER BY ot.line_id\n" 
				// @sql:off
		        );
		if (tableData != null) {
			Object[] oih = sql.getList(parameters,""
					// @sql:on
					+ Item.addParentChildCTE() + ", "
					+ cteOrder + "), "
					+ ctePrice + "), "
					+ cteVolumeDiscount
					+ "), latest_credit_term_per_order AS ( "
					+ "	SELECT	ot.order_id, "
					+ "			cd.customer_id, "
					+ "			max(cd.start_date) AS latest_date "
					+ "	FROM	credit AS cd "
					+ "	INNER JOIN order_table AS ot "
					+ "	ON cd.customer_id = ot.customer_id "
					+ "	WHERE	cd.start_date <= ot.order_date "
					+ "	GROUP BY cd.customer_id,"
					+ "			ot.order_id "
					+ "), credit_terms AS ( "
					+ "	SELECT  cdd.order_id, "
					+ "			cdd.customer_id, "
					+ "			cd.term "
					+ "	FROM credit AS cd "
					+ "	INNER JOIN latest_credit_term_per_order AS cdd "
					+ "	ON cd.customer_id = cdd.customer_id "
					+ "		AND cd.start_date = cdd.latest_date "
					+ "), "
					+ "latest_discount_start_date_per_order AS ( "
					+ "	SELECT	ot.order_id, "
					+ "			ot.customer_id, "
					+ "			d.family_id, "
					+ "			max(d.start_date) AS max_date "
					+ "	FROM 	order_table AS ot, "
					+ "			discount AS d, "
					+ "			item_header AS im "
					+ "	WHERE	d.customer_id = ot.customer_id "
					+ "		AND	d.start_date <= ot.order_date "
					+ "	GROUP BY ot.order_id, "
					+ "			ot.customer_id, "
					+ "			d.family_id "
					+ "), "
					+ "latest_discount_per_family AS ( "
					+ "	SELECT	dd.order_id, "
					+ "			dd.family_id, "
					+ "			d.level_1 AS rate1, "
					+ "			d.level_2 AS rate2 "
					+ "	FROM latest_discount_start_date_per_order AS dd "
					+ "	INNER JOIN discount AS d "
					+ "	ON dd.customer_id = d.customer_id "
					+ "		AND dd.max_date = d.start_date "
					+ "		AND dd.family_id = d.family_id "
					+ "), "
					+ "leaf_family_per_customer_discount AS ( "
					+ "	SELECT	ot.order_id, "
					+ "			ot.item_id, "
					+ "			min(pc.parent_id) AS min_family "
					+ "	FROM  order_table AS ot "
					+ "	INNER JOIN latest_discount_start_date_per_order AS d "
					+ "	ON ot.customer_id = d.customer_id "
					+ "	INNER JOIN parent_child AS pc "
					+ "	ON ot.item_id = pc.child_id "
					+ "		AND d.family_id = pc.parent_id "
					+ "	INNER JOIN item_header AS im"
					+ "	ON ot.item_id = im.id "
					+ "		AND im.not_discounted = FALSE "
					+ "	GROUP BY  ot.order_id, "
					+ "			ot.item_id "
					+ "), "
					+ "partner_discounts AS ( "
					+ "	SELECT	ot.order_id, "
					+ "			ot.item_id, "
					+ "			CASE WHEN rate1 IS null THEN 0 ELSE rate1 END "
					+ "				AS rate1, "
					+ "			CASE WHEN rate2 IS null THEN 0 ELSE rate2 END "
					+ "				AS rate2 "
					+ "	FROM latest_discount_per_family AS d "
					+ "	INNER JOIN leaf_family_per_customer_discount AS f "
					+ "	ON d.order_id = f.order_id "
					+ "		AND d.family_id = f.min_family "
					+ "	RIGHT OUTER JOIN order_table AS ot "
					+ "	ON f.order_id = ot.order_id "
					+ "		AND f.item_id = ot.item_id "
					+ ")"
					+ "SELECT	ot.order_id, " // 0
					+ "		ot.order_date, " // 1
					+ "		c.term, " // 2
					+ "		ot.customer_id, " // 3
					+ "		sum	( "
					+ "				p.price * "
					+ "				ot.qty_per * "
					+ "				ot.qty - "
					+ "				CASE "
					+ "					WHEN less IS null "
					+ "					THEN 0 "
					+ "					ELSE less "
					+ "				END * "
					+ "				ROUND "
					+ "				( "
					+ "					ot.qty_per * "
					+ "					ot.qty / "
					+ "					CASE "
					+ "						WHEN vd.per_qty IS null "
					+ "						THEN 1 "
					+ "						ELSE vd.per_qty "
					+ "					END, 0 "
					+ "				) "
					+ "			) AS total, " // 4
					+ "		sum ("
					+ "				("
					+ "					p.price * "
					+ "					ot.qty_per * "
					+ "					ot.qty - "
					+ "					CASE WHEN less IS null THEN 0 ELSE less END * "
					+ "					ROUND "
					+ "					( "
					+ "						ot.qty_per * "
					+ "						ot.qty / "
					+ "						CASE "
					+ "							WHEN vd.per_qty IS null "
					+ "							THEN 1 "
					+ "							ELSE vd.per_qty "
					+ "						END, 0 "
					+ "					) "
					+ "				) * d.rate1/100 "
					+ "			) AS total_discount1, " // 6
					+ "		sum ( "
					+ "				( "
					+ "					( "
					+ "						p.price * "
					+ "						ot.qty_per * "
					+ "						ot.qty - "
					+ "						CASE "
					+ "							WHEN less IS null "
					+ "							THEN 0 "
					+ "							ELSE less "
					+ "						END * "
					+ "						ROUND "
					+ "						("
					+ "							ot.qty_per * "
					+ "							ot.qty / "
					+ "							CASE "
					+ "								WHEN vd.per_qty IS null "
					+ "								THEN 1 "
					+ "								ELSE vd.per_qty "
					+ "							END, 0 "
					+ "						) "
					+ "					) "
					+ "					- "
					+ "					( "
					+ "						p.price * "
					+ "						ot.qty_per * "
					+ "						ot.qty - "
					+ "						CASE "
					+ "							WHEN less IS null "
					+ "							THEN 0 "
					+ "							ELSE less "
					+ "						END * "
					+ "						ROUND "
					+ "						( "
					+ "							ot.qty_per * "
					+ "							ot.qty / "
					+ "							CASE "
					+ "								WHEN vd.per_qty IS null "
					+ "								THEN 1 ELSE vd.per_qty "
					+ "							END, 0 "
					+ "						) "
					+ "					) * d.rate1/100 "
					+ "				) * d.rate2/100 "
					+ "			) AS total_discount2, " // 7
					+ "		avg(d.rate1), " // 8
					+ "		avg(d.rate2), " // 9
					+ "		ot.actual, " // 10
					+ "		ot.ref_id, " // 11
					+ "		ot.user_id, " // 12
					+ (isAnSI ? "ot.series, " : "") // 13
					+ "		ot.time_stamp " // 13 or 14
					+ "FROM order_table AS ot " 
					+ "LEFT JOIN prices AS p "
					+ "ON ot.item_id = p.item_id "
					+ "	AND ot.order_id = p.order_id "
					+ "LEFT OUTER JOIN volume_discounts AS vd "
					+ "ON ot.order_id = vd.order_id "
					+ "	AND ot.item_id = vd.item_id "
					+ "LEFT OUTER JOIN credit_terms AS c "
					+ "ON ot.order_id = c.order_id "
					+ "	AND ot.customer_id = c.customer_id "
					+ "LEFT OUTER JOIN partner_discounts AS d "
					+ "ON ot.order_id = d.order_id "
					+ "	AND ot.item_id = d.item_id " + "GROUP BY "
					+ "		ot.order_id, " + "		ot.order_date, " + "		c.term, "
					+ "		ot.customer_id, " + "		ot.actual, " + "		ot.ref_id, "
					+ "		ot.user_id, " + (isAnSI ? "	ot.series, " : "")
					+ "		ot.time_stamp "
					// @sql:off
			        );
			id = oih[0] == null ? 0 : (int) oih[0];
			date = oih[1] == null ? null : (Date) oih[1];
			leadTime = oih[2] == null ? 0 : (int) oih[2];
			setPartnerId(oih[3] == null ? 0 : (int) oih[3]);
			address = new Address(partnerId).getAddress();
			computedTotal = oih[4] == null ? BigDecimal.ZERO : (BigDecimal) oih[4];
			totalDiscount1 = oih[5] == null ? BigDecimal.ZERO : (BigDecimal) oih[5];

			totalDiscount2 = oih[6] == null ? BigDecimal.ZERO : (BigDecimal) oih[6];
			discount1Percent = oih[7] == null ? BigDecimal.ZERO : (BigDecimal) oih[7];
			discount2Percent = oih[8] == null ? BigDecimal.ZERO : (BigDecimal) oih[8];
			enteredTotal = oih[9] == null ? BigDecimal.ZERO : (BigDecimal) oih[9];

			if (isA_PO || isAnSO) {
				referenceId = id;
			} else {
				referenceId = oih[10] == null ? 0 : (int) oih[10];
			}
			inputter = ((String) oih[11]).toUpperCase();
			if (isAnSI) {
				series = (String) oih[12];
				timestamp = ((Timestamp) oih[13]).getTime();
			} else {
				timestamp = ((Timestamp) oih[12]).getTime();
			}
			inputDate = new Date(timestamp);
			inputTime = new Time(timestamp);
			computedTotal = computedTotal.subtract(totalDiscount1).subtract(totalDiscount2);
			totalVatable = DIS.divide(computedTotal, DIS.VAT);
			totalVat = computedTotal.subtract(totalVatable);
			isAnRMA = DIS.isNegative(computedTotal) && (int) tableData[0][1] != DIS.DEALERS_INCENTIVE;

			for (int i = 0; i < tableData.length; i++) {
				itemIds.add((int) tableData[i][1] * (isAnRMA ? -1 : 1));
				uoms.add(Type.valueOf((String) tableData[i][3]));
				quantities.add((BigDecimal) tableData[i][4]);
			}
			return;
		}
		this.referenceId = 0;
		String strActual;
		if (isA_PO || isAnSO) {
			strActual = " 0.0 AS actual, ";
		} else {
			strActual = " actual, ";
		}
		headerData = sql.getList(id,""
					// @sql:on
					+ "SELECT " + strActual
					+ "		customer_id, " 
					+ "	" + type + "_date, "
					+ "		user_id, " 
					+ "		time_stamp " 
					+ "FROM " + type + "_header  " 
					+ "WHERE	" + type + "_id = ? "
					+ (isAnSI ? "AND series = '" + series + "'" : "")
					// @sql:off
		        );
		if (headerData != null) {
			if (headerData[0] != null)
				enteredTotal = (BigDecimal) headerData[0];
			if (headerData[1] != null)
				partnerId = (int) headerData[1];
			if (headerData[2] != null)
				date = (Date) headerData[2];
			if (headerData[3] != null)
				inputter = ((String) headerData[3]).toUpperCase();
			if (headerData[4] != null) {
				timestamp = ((Timestamp) headerData[4]).getTime();
				inputDate = new Date(timestamp);
				inputTime = new Time(timestamp);
			}
		}
		isSalaryCredit = OrderControl.getFirstLineItemId(Type.DELIVERY, id) == DIS.SALARY_CREDIT;
		if (((isA_DR || isAnSI) && (getEnteredTotal().compareTo(BigDecimal.ZERO) < 0 || isSalaryCredit))) {
			tableData = sql.getTableData(id,""
						// @sql:on
						+ "SELECT dd.line_id, " 
						+ "		  dd.item_id, "
						+ "		  im.short_id, " 
						+ "		  uom.unit, " 
						+ "		  dd.qty, "
						+ "		  -1.0 AS price, " 
						+ "		  -1 * qty AS subtotal "
						+ "  FROM " + type + "_detail as dd "
						+ "       INNER JOIN item_header as im "
						+ "          ON dd.item_id = im.id " 
						+ "       INNER JOIN uom "
						+ "          ON dd.uom = uom.id " 
						+ " WHERE dd." + type + "_id = ?;"
						// @sql:off
			        );
			if (tableData != null)
				computedTotal = (BigDecimal) tableData[0][6];
		} else {
			tableData = new Object[0][0];
		}
	}

	protected void setProperties() {
	}

	public void setPartnerId(int partnerId) {
		this.partnerId = partnerId;
		partner = Customer.getName(partnerId);
		if (partner != null) {
			isMaterialTransfer = partner.contains("MATERIAL TRANSFER");
			address = new Address(partnerId).getAddress();
			if (!type.equals("remit")) {
				isForAnExTruck = Customer.getName(partnerId).contains("EX-TRUCK");
				isPartnerFromAnExTruckRoute = Route.isPartnerFromAnExTruck(partnerId, DIS.TODAY);
				isForDisposal = partner.contains("DISPOSAL");
				isForSpecialCustomer = Channel.isSpecial(partnerId);
				int routeId = Route.getId(partnerId, DIS.TODAY);
				route = Route.getName(routeId);
			}
		}
	}

	public void setTotals(BigDecimal total) {
		BigDecimal firstLevelDiscount = total.multiply(DIS.divide(getDiscount1Percent(), DIS.HUNDRED));
		total = total.subtract(firstLevelDiscount);

		BigDecimal secondLevelDiscount = total.multiply(DIS.divide(getDiscount2Percent(), DIS.HUNDRED));
		total = total.subtract(secondLevelDiscount);

		computedTotal = getComputedTotal().add(total);
		firstLevelDiscount = getDiscount1Total().add(firstLevelDiscount);
		secondLevelDiscount = getDiscount2Total().add(secondLevelDiscount);
	}

	protected boolean isAPostiveReferenceIdInputValid(int referenceId) {
		isReferenceAnSO = true;
		isForAnExTruck = new CustomerData().isForAnExTruck();
		return true;
	}

	public String getInputMessage() {
		return "Enter\n" + type.getName() + " #";
	}

	public Object[] getHeaderData(Type type, Object id) {
		headerData = sql.getList(id,""
				// @sql:on
				+ "SELECT " + type + "_date,\n"
				+ "       customer_id,\n"
				+ "       actual,\n"
				+ "       ref_id,\n"
				+ "		  user_id,\n"
				+ "       time_stamp\n"
 				+ "  FROM " + type + "_header  " 
				+ " WHERE " + type + "_id = ? "
				+ addSeriesSQL(series)
				// @sql:off
		        );
		return headerData;
	}

	private String addSeriesSQL(String series) {
		return series == null ? ";" : "       AND series = ?;";
	}

	@SuppressWarnings("unchecked")
	public void saveLineItem(ArrayList<?> list, Object value, int rowIdx) {
		if (rowIdx < list.size()) {
			list.getClass().cast(list).set(rowIdx, value.getClass().cast(value));
		} else {
			list.getClass().cast(list).add(value.getClass().cast(value));
		}
	}

	public BigDecimal getDifferenceOfTotals() {
		return computedTotal.subtract(enteredTotal).abs();
	}

	public boolean isDifferenceOfTotalsAcceptable() {
		return getDifferenceOfTotals().compareTo(BigDecimal.ONE) < 1;
	}

	public String getPartner() {
		return partner;
	}

	public String getAddress() {
		return address;
	}

	public BigDecimal getOverdue() {
		return overdue == null ? BigDecimal.ZERO : overdue;
	}

	public void setOverdue(BigDecimal overdue) {
		this.overdue = overdue;
	}

	public BigDecimal getTotalDiscountRate() {
		return totalDiscountRate;
	}

	public void setTotalDiscountRate(BigDecimal totalDiscountRate) {
		this.totalDiscountRate = totalDiscountRate;
	}

	public BigDecimal getDiscount1Percent() {
		return discount1Percent == null ? BigDecimal.ZERO : discount1Percent;
	}

	public void setDiscount1Percent(BigDecimal discount1Percent) {
		this.discount1Percent = discount1Percent;
	}

	public BigDecimal getDiscount1Total() {
		return totalDiscount1;
	}

	public void setDiscount1Total(BigDecimal totalDiscount1) {
		this.totalDiscount1 = totalDiscount1;
	}

	public BigDecimal getDiscount2Percent() {
		return discount2Percent;
	}

	public void setDiscount2Percent(BigDecimal discount2Percent) {
		this.discount2Percent = discount2Percent;
	}

	public BigDecimal getDiscount2Total() {
		return totalDiscount2;
	}

	public void setDiscount2Total(BigDecimal totalDiscount2) {
		this.totalDiscount2 = totalDiscount2;
	}

	public BigDecimal getQty() {
		return quantity;
	}

	public void setQty(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getReferenceQuantity() {
		return referenceQuantity;
	}

	public void setReferenceQuantity(BigDecimal referenceQuantity) {
		this.referenceQuantity = referenceQuantity;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getVolumeDiscountQty() {
		return volumeDiscountQty;
	}

	public void setVolumeDiscountQty(BigDecimal volumeDiscountQty) {
		this.volumeDiscountQty = volumeDiscountQty;
	}

	public BigDecimal getVolumeDiscountValue() {
		return volumeDiscountValue;
	}

	public void setVolumeDiscountValue(BigDecimal volumeDiscountValue) {
		this.volumeDiscountValue = volumeDiscountValue;
	}

	public BigDecimal getTotalVatable() {
		return totalVatable;
	}

	public void setTotalVatable(BigDecimal totalVatable) {
		this.totalVatable = totalVatable;
	}

	public int getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(int referenceId) {
		this.referenceId = referenceId;
	}

	public int getLeadTime() {
		return leadTime;
	}

	public void setLeadTime(int leadTime) {
		this.leadTime = leadTime;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public String getInputter() {
		return inputter;
	}

	public Date getInputDate() {
		return inputDate;
	}

	public Time getInputTime() {
		return inputTime;
	}

	public BigDecimal getTotalVat() {
		return totalVat;
	}

	public void setTotalVat(BigDecimal totalVat) {
		this.totalVat = totalVat;
	}

	public BigDecimal getComputedTotal() {
		return computedTotal == null ? BigDecimal.ZERO : computedTotal;
	}

	public void setComputedTotal(BigDecimal computedTotal) {
		this.computedTotal = computedTotal;
	}

	public String getBizUnit() {
		return bizUnit;
	}

	public void setBizUnit(String bizUnit) {
		this.bizUnit = bizUnit;
	}

	public ArrayList<String> getBizUnits() {
		return bizUnits;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public ArrayList<Integer> getItemIds() {
		return itemIds;
	}

	public ArrayList<Type> getUoms() {
		return uoms;
	}

	public ArrayList<BigDecimal> getQtys() {
		return quantities;
	}

	public String[] getUomList() {
		return units;
	}

	public void setUnitsOfMeasure(String[] uoms) {
		this.units = uoms;
	}

	public BigDecimal getEnteredTotal() {
		return enteredTotal;
	}

	public void setEnteredTotal(BigDecimal enteredTotal) {
		this.enteredTotal = enteredTotal;
	}

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	public Type getUom() {
		return uom;
	}

	public void setUom(Type uom) {
		this.uom = uom;
	}

	public String getRoute() {
		return route;
	}

	public int getPartnerId() {
		return partnerId;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public boolean isReferenceAnSO() {
		return isReferenceAnSO;
	}

	public void setReferenceAnSO(boolean isReferenceAnSO) {
		this.isReferenceAnSO = isReferenceAnSO;
	}

	public boolean isAnRMA() {
		return isAnRMA;
	}

	public boolean isAnRR() {
		return isAnRR;
	}

	public void setRMA(boolean isRMA) {
		this.isAnRMA = isRMA;
	}

	public boolean isSO() {
		return isAnSO;
	}

	public boolean isA_PO() {
		return isA_PO;
	}

	public boolean isA_DR() {
		return isA_DR;
	}

	public boolean isSI() {
		return isAnSI;
	}

	public boolean isForDisposal() {
		return isForDisposal;
	}

	public boolean isForAnExTruck() {
		return isForAnExTruck;
	}

	public boolean isFromAnExTruck() {
		return isFromAnExTruck;
	}

	public void setFromAnExTruck(boolean isFromAnExTruck) {
		this.isFromAnExTruck = isFromAnExTruck;
	}

	public void setForAnExTruck(boolean isForAnExTruck) {
		this.isForAnExTruck = isForAnExTruck;
	}

	public boolean isMaterialTransfer() {
		return isMaterialTransfer;
	}

	public boolean isPartnerFromAnExTruckRoute() {
		return isPartnerFromAnExTruckRoute;
	}

	public void setPartnerFromAnExTruckRoute(boolean isPartnerFromAnExTruckRoute) {
		this.isPartnerFromAnExTruckRoute = isPartnerFromAnExTruckRoute;
	}

	public boolean isForInternalCustomerOrOthers() {
		return isForSpecialCustomer;
	}

	public boolean isDealerIncentive() {
		return isDealerIncentive;
	}

	public void setDealerIncentive(boolean isDealerIncentive) {
		this.isDealerIncentive = isDealerIncentive;
	}

	protected void processId(Object id) {
		this.id = (int) id;
	}

	@Override
	public boolean isEnteredItemQuantityValid(String qty) {
		quantity = DIS.parseBigDecimal(qty);
		subtotal = price.multiply(quantity);

		if (!isAnRMA && !isInventoryEnough())
			return false;
		return true;
	}

	private boolean isInventoryEnough() {
	    BigDecimal goodStock = Inventory.getGoodStock(itemId);
	    boolean hasEnoughGoodStock = goodStock.compareTo(quantity) >= 0;

	    BigDecimal badStock = Inventory.getBadStock(itemId);
	    boolean hasEnoughBadStock = badStock.compareTo(quantity) >= 0;

	    boolean hasEnoughSOqty = referenceQuantity.compareTo(quantity) >= 0;
	    if (isForDisposal && !hasEnoughBadStock) {
	    	new ErrorDialog("Only " + DIS.NO_COMMA_INTEGER.format(badStock) + " left;\nplease adjust quantity");
	    	return false;
	    }
	    if (!isAnRMA && !isForDisposal && !hasEnoughGoodStock) {
	    	new ErrorDialog("Only " + DIS.NO_COMMA_INTEGER.format(goodStock) + " left;\nplease adjust quantity");
	    	return false;
	    }

	    if (isAnSI || isA_DR && !hasEnoughSOqty) {
	    	new ErrorDialog("Only " + DIS.NO_COMMA_INTEGER.format(referenceQuantity)
	    	        + " is in S/O;\nplease adjust quantity");
	    	return false;
	    }
	    return true;
    }

	@Override
	public void processQuantityInput(String qty, int rowIdx) {

		Type volumeDiscountedUom = new VolumeDiscount().getUom(itemId, date);
		if (uom == volumeDiscountedUom) {
			BigDecimal volumeDiscountBucket = quantity.divideToIntegralValue(volumeDiscountQty);
			BigDecimal volumeDiscount = volumeDiscountValue.multiply(volumeDiscountBucket);
			subtotal = subtotal.subtract(volumeDiscount);
		}

		if (isAnRMA) {
			BigDecimal balance = enteredTotal.add(subtotal);
			if (DIS.isNegative(balance)) {
				new ErrorDialog("Exceeded RMA limit;\nadjust quantity");
				return;
			} else {
				enteredTotal = balance;
				//enteredTotalInput.setText(DIS.formatTo2Places(data.getEnteredTotal()));
			}
		}
		BigDecimal net = subtotal;
			// Label discount1Label = discount1Box.getLabel();
			// discount1Label.setText(DIS.formatTo2Places(discount1Percent) +
			// "%");
			BigDecimal discount1 = subtotal.multiply(DIS.getRate(discount1Percent));
			totalDiscount1 = totalDiscount1.add(discount1);

			// discount1Box.getText().setText(DIS.formatTo2Places(totalDiscount1));
			//Label discount2Label = discount2Box.getLabel();
			//discount2Label.setText(DIS.formatTo2Places(data.getDiscount2Percent()) + "%");
			BigDecimal netOfDiscount1 = subtotal.subtract(discount1);
			BigDecimal discount2 = netOfDiscount1.multiply(DIS.getRate(discount2Percent));
			totalDiscount2 = totalDiscount2.add(discount2);
			//discount2Box.getText().setText(DIS.formatTo2Places(data.getDiscount2Total()));

			net = netOfDiscount1.subtract(discount2);

		BigDecimal vatable = DIS.divide(net, DIS.VAT);
		totalVatable = totalVatable.add(vatable);

		// show VATable
		BigDecimal vat = net.subtract(vatable);
		totalVat = totalVat.add(vat);

		computedTotal = computedTotal.add(net);

		saveLineItem(itemIds, itemId, rowIdx);
		saveLineItem(uoms, uom, rowIdx);
		saveLineItem(quantities, quantity, rowIdx);

		isMaterialTransfer = partner.contains("MATERIAL TRANSFER");
		price = null;

		//if (isDifferenceOfTotalsAcceptable() || isSO() || isA_PO() || isMaterialTransfer) postButton.setEnabled(true);
	}
}

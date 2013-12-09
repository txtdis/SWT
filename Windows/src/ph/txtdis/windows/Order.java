package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public abstract class Order extends Report {

	protected boolean isEditable;
	protected int referenceId, leadTime, rowIdx, qtyColumnNo = 4;
	protected ArrayList<BigDecimal> qtys;
	protected ArrayList<Integer> itemIds, uomIds;
	protected BigDecimal computedTotal = BigDecimal.ZERO, enteredTotal, firstLevelDiscount,
			totalDiscount1, totalVatable, totalVat, qty, referenceQty;
	protected Date dueDate, inputDate;
	protected String address, inputter, series, type, reference;
	protected String[] uoms;
	protected Time inputTime;

	private boolean isACount, isAnSO, isA_PO, isA_DR, isAnRMA, isAnRR, isAnSI,
	isPartnerFromAnExTruckRoute, isForAnExTruck, isFromAnExTruck, isForDisposal,
	isForInternalCustomerOrOthers, isMonetary, isDealerIncentive,
	isReferenceAnSO;
	private int uomId;
	private long timestamp;
	private ArrayList<String> bizUnits;
	private BigDecimal overdue, totalDiscountRate, secondLevelDiscount, totalDiscount2, price, volumeDiscountQty, volumeDiscountValue;
	private String partner, route, bizUnit;
	private BigDecimal vat = Constant.getInstance().getVat();

	public static final int ITEM_COLUMN = 2;
	public static final int ITEM_ID_COLUMN = 1;
	public static final int PRICE_COLUMN = 5;
	public static final int TOTAL_COLUMN = 6;
	public static final int UOM_COLUMN = 3;

	public Order() {
		super();
	}

	public Order(Integer orderId) {
		this(orderId, null);
	}

	public Order(Integer orderId, String series) {
		this();
		this.series = series;
		id = Math.abs(orderId);
		// @sql:on
		headers = new String[][] { { StringUtils.center("#", 3), "Line" },
				{ StringUtils.center("ID", 6), "ID" },
				{ StringUtils.center("PRODUCT NAME", 50), "String" },
				{ StringUtils.center("UOM", 5), "String" },
				{ StringUtils.center("QTY", 9), "BigDecimal" },
				{ StringUtils.center("PRICE", 9), "BigDecimal" },
				{ StringUtils.center("SUBTOTAL", 12), "BigDecimal" } };
		// @sql:off
		setData();
		switch (type) {
		case "count":
			isACount = true;
			break;
		case "delivery":
			isA_DR = true;
			break;
		case "invoice":
			isAnSI = true;
			break;
		case "purchase":
			isA_PO = true;
			break;
		case "receiving":
			isAnRR = true;
			break;
		case "sales":
			isAnSO = true;
			break;
		default:
			break;
		}
		// @sql:on
		String cteOrder = "" + "order_table AS ( " + "	SELECT	h."
				+ type
				+ "_id AS order_id, "
				+ (isAnSI ? "	h.series, " : "")
				+ "			h.customer_id, "
				+ "			h."
				+ type
				+ "_date AS order_date, "
				+ reference
				+ "			h.user_id, "
				+ "			h.time_stamp, "
				+ "			d.line_id, "
				+ "			abs(d.item_id) AS item_id, "
				+ "			d.qty, "
				+ "			d.uom, "
				+ "			d.qty * qp.qty AS pcs, "
				+ "			qp.qty AS qty_per,"
				+ "			CASE WHEN d.item_id < 0 THEN true ELSE false END AS is_rma "
				+ "	FROM " + type + "_header AS h " + "	INNER JOIN " + type
				+ "_detail AS d " + "		ON h." + type + "_id = d." + type
				+ "_id " + (isAnSI ? "	AND h.series = d.series " : "")
				+ "	INNER JOIN qty_per AS qp " + "		ON d.uom = qp.uom "
				+ "			AND	abs(d.item_id) = qp.item_id " + "	WHERE h." + type
				+ "_id = ? " + (isAnSI ? "	AND h.series = ? " : "");

		String ctePrice = ""
				+ "latest_price_start_date_per_order AS ( "
				+ "	SELECT	ot.order_id, "
				+ "			ot.item_id, "
				+ "			p.tier_id,"
				+ "			ot.is_rma, "
				+ "			max(p.start_date) AS max_date "
				+ "	FROM order_table AS ot "
				+ "	INNER JOIN  price AS p "
				+ "	ON ot.item_id = p.item_id "
				+ "	INNER JOIN parent_child AS pc "
				+ "	ON ot.item_id = pc.child_id "
				+ "	INNER JOIN channel_price_tier AS cpt "
				+ "	ON p.tier_id = cpt.tier_id "
				+ "		AND cpt.family_id = pc.parent_id "
				+ "	INNER JOIN customer_master AS cm "
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
				+ "	INNER JOIN  price AS p "
				+ "	ON pd.max_date = p.start_date "
				+ "		AND pd.tier_id = p.tier_id "
				+ "		And pd.item_id = p.item_id ";

		String cteVolumeDiscount = ""
				+ "latest_volume_discount_start_date_per_order AS ( "
				+ "	SELECT		ot.order_id, " + "				ot.item_id, "
				+ "				max(d.start_date) AS max_date "
				+ "	FROM 		order_table AS ot "
				+ "	INNER JOIN 	volume_discount AS d "
				+ "	ON 			ot.item_id = d.item_id "
				+ "	WHERE 		d.start_date <= ot.order_date "
				+ "	GROUP BY 	ot.order_id, " + "				ot.item_id " + "), "
				+ "volume_discounts AS ( " + "	SELECT	dd.order_id, "
				+ "			d.item_id, " + "			d.uom, " + "			d.per_qty, "
				+ "			d.less "
				+ "	FROM latest_volume_discount_start_date_per_order AS dd "
				+ "	INNER JOIN volume_discount AS d "
				+ "	ON dd.max_date = d.start_date "
				+ "		AND dd.item_id = d.item_id ";
		// @sql:off

		Object[] parameters = (isAnSI ? new Object[] { id, series }
		: new Object[] { id });

		// @sql:on
		data = sql.getDataArray(parameters,
						SQL.addItemParentStmt() + ", "
						+ cteOrder + "), "
						+ ctePrice + "), "
						+ cteVolumeDiscount + ") "
						+ "SELECT	"
						+ "		ot.line_id, "
						+ // 0
						"		CASE WHEN ot.is_rma IS TRUE THEN -ot.item_id ELSE ot.item_id END AS item_id, "
						+ // 1
						"		im.name, "
						+ // 2
						"		uom.unit, "
						+ // 3
						"		ot.qty, "
						+ // 4
						"			(p.price * ot.qty_per * ot.qty "
						+ "			- CASE WHEN less IS null THEN 0 ELSE less END "
						+ "			* ROUND(ot.qty_per * ot.qty "
						+ "			/ CASE WHEN d.per_qty IS null "
						+ "				THEN 1 ELSE d.per_qty END,0)) "
						+ "			/ ot.qty "
						+ "		AS price, "
						+ "			p.price * ot.qty_per * ot.qty "
						+ "			- CASE WHEN less IS null THEN 0 ELSE less END "
						+ "			* ROUND(ot.qty_per * ot.qty "
						+ "			/ CASE WHEN d.per_qty IS null THEN 1 ELSE d.per_qty END,0) "
						+ "		AS subtotal, "
						+ "		im.short_id "
						+ (isAnSO ? ", if.id " : "")
						+ "FROM item_master AS im "
						+ "INNER JOIN order_table AS ot "
						+ "ON ot.item_id = im.id "
						+ "INNER JOIN uom "
						+ "ON ot.uom = uom.id "
						+ (isAnSO ? ("INNER JOIN parent_child AS ip "
								+ "ON ot.item_id = ip.child_id "
								+ "INNER JOIN item_family as if "
								+ "ON ip.parent_id = if.id "
								+ "AND if.tier_id = 1 ") : "")
								+ "INNER JOIN	prices AS p "
								+ "ON p.item_id = ot.item_id "
								+ "LEFT OUTER JOIN volume_discounts AS d "
								+ "ON ot.item_id = d.item_id "
								+ "ORDER BY ot.line_id ");
		// @sql:off
		if (data != null) {
			Object[] oih = sql.getData(parameters, "" +
					// @sql:on
					SQL.addItemParentStmt() + ", "
					+ cteOrder
					+ "), "
					+ ctePrice
					+ "), "
					+ cteVolumeDiscount
					+ "), latest_credit_term_per_order AS ( "
					+ "	SELECT	ot.order_id, "
					+ "			cd.customer_id, "
					+ "			max(cd.start_date) AS latest_date "
					+ "	FROM	credit_detail AS cd "
					+ "	INNER JOIN order_table AS ot "
					+ "	ON cd.customer_id = ot.customer_id "
					+ "	WHERE	cd.start_date <= ot.order_date "
					+ "	GROUP BY cd.customer_id,"
					+ "			ot.order_id "
					+ "), credit_terms AS ( "
					+ "	SELECT  cdd.order_id, "
					+ "			cdd.customer_id, "
					+ "			cd.term "
					+ "	FROM credit_detail AS cd "
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
					+ "			item_master AS im "
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
					+ "	INNER JOIN item_master AS im"
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
					+ "		ot.order_date, " // 2
					+ "		c.term, " // 3
					+ "		ot.customer_id, " // 4
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
					+ "			) AS total, " // 5
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
					+ "FROM order_table AS ot " + "INNER JOIN prices AS p "
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
					+ "		ot.time_stamp ");
			// @sql:off
			id = oih[0] == null ? 0 : (int) oih[0];
			date = (Date) oih[1];
			leadTime = oih[2] == null ? 0 : (int) oih[2];
			setPartnerId(oih[3] == null ? 0 : (int) oih[3]);
			address = new Address(partnerId).getAddress();
			computedTotal = oih[4] == null ? BigDecimal.ZERO
					: (BigDecimal) oih[4];
			totalDiscount1 = oih[5] == null ? BigDecimal.ZERO
					: (BigDecimal) oih[5];
			totalDiscount2 = oih[6] == null ? BigDecimal.ZERO
					: (BigDecimal) oih[6];
			firstLevelDiscount = oih[7] == null ? BigDecimal.ZERO
					: (BigDecimal) oih[7];
			secondLevelDiscount = oih[8] == null ? BigDecimal.ZERO
					: (BigDecimal) oih[8];
			enteredTotal = oih[9] == null ? BigDecimal.ZERO
					: (BigDecimal) oih[9];
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
			computedTotal = computedTotal.subtract(totalDiscount1).subtract(
					totalDiscount2);
			totalVatable = computedTotal
					.divide(vat, BigDecimal.ROUND_HALF_EVEN);
			totalVat = computedTotal.subtract(totalVatable);
			int rmaSign = computedTotal.signum();
			for (int i = 0; i < data.length; i++) {
				getItemIds().add((int) data[i][1] * rmaSign);
				getUomIds().add(new UOM((String) data[i][3]).getId());
				getQtys().add((BigDecimal) data[i][4]);
			}
		} else {
			this.referenceId = 0;
			String strActual;
			if (isA_PO || isAnSO) {
				strActual = " 0.0 AS actual, ";
			} else {
				strActual = " actual, ";
			}

			// @sql:on
			objects = sql.getData(id, "" + "SELECT " + strActual
					+ "		customer_id, " + "	" + type + "_date, "
					+ "		user_id, " + "		time_stamp " + "FROM " + type
					+ "_header  " + "WHERE	" + type + "_id = ? "
					+ (isAnSI ? "AND series = '" + series + "'" : ""));
			// @sql:off
			if (objects != null) {
				if (objects[0] != null)
					enteredTotal = (BigDecimal) objects[0];
				if (objects[1] != null)
					partnerId = (int) objects[1];
				if (objects[2] != null)
					date = (Date) objects[2];
				if (objects[3] != null)
					inputter = ((String) objects[3]).toUpperCase();
				if (objects[4] != null) {
					timestamp = ((Timestamp) objects[4]).getTime();
					inputDate = new Date(timestamp);
					inputTime = new Time(timestamp);
				}
			}
			if (isA_DR && getEnteredTotal().compareTo(BigDecimal.ZERO) < 0) {
				data = sql.getDataArray(id, ""
						+
						// @sql:on
						"SELECT dd.line_id, " + "		dd.item_id, "
						+ "		im.name, " + "		uom.unit, " + "		dd.qty, "
						+ "		-1.0 AS price, " + "		-1 * qty AS subtotal "
						+ "FROM	delivery_detail as dd "
						+ "INNER JOIN item_master as im "
						+ "ON dd.item_id = im.id " + "INNER JOIN uom "
						+ "ON dd.uom = uom.id " + "WHERE dd.delivery_id = ?;");
				// @sql:off
				if (data != null)
					computedTotal = (BigDecimal) data[0][6];
			} else {
				data = new Object[0][0];
			}
		}
	}

	protected void setData() {
	}

	public void setPartnerId(int partnerId) {
		this.partnerId = partnerId;
		Customer customer = new Customer();
		Route routing = new Route();
		partner = customer.getName(partnerId);
		if (!partner.isEmpty()) {
			address = new Address(partnerId).getAddress();
			if (!type.equals("remit")) {
				isForAnExTruck = customer.isForAnExTruck(partnerId);
				isPartnerFromAnExTruckRoute = routing.isPartnerFromAnExTruck(
						partnerId, date);
				isForDisposal = partner.equals("BO DISPOSAL");
				isForInternalCustomerOrOthers = customer
						.isInternalOrOthers(partnerId);
				routeId = routing.getId(partnerId);
				route = routing.getName(routeId);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void saveLineItem(ArrayList<?> list, Object value, int rowIdx) {
		if (rowIdx < list.size()) {
			list.getClass().cast(list)
			.set(rowIdx, value.getClass().cast(value));
		} else {
			list.getClass().cast(list).add(value.getClass().cast(value));
		}
	}

	public String getModule() {
		return module;
	}

	public String getPartner() {
		return partner;
	}

	public String getAddress() {
		return address;
	}

	public BigDecimal getOverdue() {
		if (overdue == null)
			overdue = BigDecimal.ZERO;
		return overdue;
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

	public BigDecimal getFirstLevelDiscountRate() {
		if (firstLevelDiscount == null)
			firstLevelDiscount = BigDecimal.ZERO;
		return firstLevelDiscount;
	}

	public void setFirstLevelDiscount(BigDecimal firstLevelDiscount) {
		this.firstLevelDiscount = firstLevelDiscount;
	}

	public BigDecimal getFirstLevelDiscountTotal() {
		if (totalDiscount1 == null)
			totalDiscount1 = BigDecimal.ZERO;
		return totalDiscount1;
	}

	public void setFirstLevelDiscountTotal(BigDecimal totalDiscount1) {
		this.totalDiscount1 = totalDiscount1;
	}

	public BigDecimal getSecondLevelDiscountRate() {
		if (secondLevelDiscount == null)
			secondLevelDiscount = BigDecimal.ZERO;
		return secondLevelDiscount;
	}

	public void setSecondLevelDiscount(BigDecimal secondLevelDiscount) {
		this.secondLevelDiscount = secondLevelDiscount;
	}

	public boolean isEditable() {
		return isEditable;
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

	public BigDecimal getSecondLevelDiscountTotal() {
		if (totalDiscount2 == null)
			totalDiscount2 = BigDecimal.ZERO;
		return totalDiscount2;
	}

	public void setSecondLevelDiscountTotal(BigDecimal totalDiscount2) {
		this.totalDiscount2 = totalDiscount2;
	}

	public BigDecimal getQty() {
		if (qty == null)
			qty = BigDecimal.ZERO;
		return qty;
	}

	public void setQty(BigDecimal qty) {
		this.qty = qty;
	}

	public BigDecimal getReferenceQty() {
		if (referenceQty == null)
			referenceQty = BigDecimal.ZERO;
		return referenceQty;
	}

	public void setReferenceQty(BigDecimal referenceQty) {
		this.referenceQty = referenceQty;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public boolean isReferenceAnSO() {
		return isReferenceAnSO;
	}

	public void setReferenceAnSO(boolean isReferenceAnSO) {
		this.isReferenceAnSO = isReferenceAnSO;
	}

	public BigDecimal getVolumeDiscountQty() {
		if (volumeDiscountQty == null)
			volumeDiscountQty = new BigDecimal(999_999);
		return volumeDiscountQty;
	}

	public void setVolumeDiscountQty(BigDecimal volumeDiscountQty) {
		this.volumeDiscountQty = volumeDiscountQty;
	}

	public BigDecimal getVolumeDiscountValue() {
		if (volumeDiscountValue == null)
			volumeDiscountValue = BigDecimal.ZERO;
		return volumeDiscountValue;
	}

	public void setVolumeDiscountValue(BigDecimal volumeDiscountValue) {
		this.volumeDiscountValue = volumeDiscountValue;
	}

	public BigDecimal getTotalVatable() {
		if (totalVatable == null)
			totalVatable = BigDecimal.ZERO;
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

	public int getRowIdx() {
		return rowIdx;
	}

	public void setRowIdx(int rowIdx) {
		this.rowIdx = rowIdx;
	}

	public Date getDueDate() {
		if (dueDate == null)
			dueDate = DIS.TODAY;
		return dueDate;
	}

	public String getInputter() {
		if (inputter == null)
			inputter = Login.getUser().toUpperCase();
		return inputter;
	}

	public Date getInputDate() {
		if (inputDate == null)
			inputDate = DIS.TODAY;
		return inputDate;
	}

	public Time getInputTime() {
		if (inputTime == null)
			inputTime = DIS.ZERO_TIME;
		return inputTime;
	}

	public BigDecimal getTotalVat() {
		if (totalVat == null)
			totalVat = BigDecimal.ZERO;
		return totalVat;
	}

	public void setTotalVat(BigDecimal totalVat) {
		this.totalVat = totalVat;
	}

	public BigDecimal getComputedTotal() {
		if (computedTotal == null)
			computedTotal = BigDecimal.ZERO;
		return computedTotal;
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
		if (bizUnits == null)
			bizUnits = new ArrayList<>();
			return bizUnits;
	}

	public ArrayList<Integer> getItemIds() {
		if (itemIds == null)
			itemIds = new ArrayList<>();
			return itemIds;
	}

	public ArrayList<Integer> getUomIds() {
		if (uomIds == null)
			uomIds = new ArrayList<>();
			return uomIds;
	}

	public ArrayList<BigDecimal> getQtys() {
		if (qtys == null)
			qtys = new ArrayList<>();
			return qtys;
	}

	public String[] getUoms() {
		return uoms;
	}

	public void setUoms(String[] uoms) {
		this.uoms = uoms;
	}

	public BigDecimal getEnteredTotal() {
		if (enteredTotal == null)
			enteredTotal = BigDecimal.ZERO;
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

	public int getUomId() {
		return uomId;
	}

	public void setUomId(int uomId) {
		this.uomId = uomId;
	}

	public String getRoute() {
		return route;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isAnRMA() {
		return isAnRMA;
	}

	public boolean isAnRR() {
		return isAnRR;
	}

	public boolean isACount() {
		return isACount;
	}

	public void setRMA(boolean isRMA) {
		this.isAnRMA = isRMA;
	}

	public boolean isAnSO() {
		return isAnSO;
	}

	public boolean isA_PO() {
		return isA_PO;
	}

	public boolean isA_DR() {
		return isA_DR;
	}

	public boolean isAnSI() {
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

	public boolean isPartnerFromAnExTruckRoute() {
		return isPartnerFromAnExTruckRoute;
	}

	public void setPartnerFromAnExTruckRoute(boolean isPartnerFromAnExTruckRoute) {
		this.isPartnerFromAnExTruckRoute = isPartnerFromAnExTruckRoute;
	}

	public boolean isForInternalCustomerOrOthers() {
		return isForInternalCustomerOrOthers;
	}

	public boolean isAMonetaryTransaction() {
		return isMonetary;
	}

	public void setAMonetaryTransaction(boolean isMonetary) {
		this.isMonetary = isMonetary;
	}

	public boolean isDealerIncentive() {
		return isDealerIncentive;
	}

	public void setDealerIncentive(boolean isDealerIncentive) {
		this.isDealerIncentive = isDealerIncentive;
	}

	public void recomputeTotals(String subtotalText) {
		setTotals(subtotalText.isEmpty() ? BigDecimal.ZERO : DIS.parseBigDecimal(subtotalText).negate());
	}

	public int getQtyColumnNo() {
		return qtyColumnNo;
	}

	public void setQtyColumnNo(int qtyColumnNo) {
		this.qtyColumnNo = qtyColumnNo;
	}

	public void setTotals(BigDecimal total) {

		BigDecimal firstLevelDiscount = total
				.multiply(getFirstLevelDiscountRate().divide(DIS.HUNDRED,
						BigDecimal.ROUND_HALF_EVEN));
		total = total.subtract(firstLevelDiscount);

		BigDecimal secondLevelDiscount = total
				.multiply(getSecondLevelDiscountRate().divide(DIS.HUNDRED,
						BigDecimal.ROUND_HALF_EVEN));
		total = total.subtract(secondLevelDiscount);

		if (isAMonetaryTransaction() && isA_DR) {
			BigDecimal vatable = total.divide(DIS.VAT, BigDecimal.ROUND_HALF_EVEN);
			BigDecimal vat = total.subtract(vatable);
			totalVatable = getTotalVatable().add(vatable);
			totalVat = getTotalVat().add(vat);
		}

		computedTotal = getComputedTotal().add(total);
		firstLevelDiscount = getFirstLevelDiscountTotal().add(
				firstLevelDiscount);
		secondLevelDiscount = getSecondLevelDiscountTotal().add(
				secondLevelDiscount);
	}

	protected boolean isAPostiveReferenceIdInputValid(int referenceId) {
		isReferenceAnSO = true;
		isForAnExTruck = new Customer().isForAnExTruck();
		return true;
	}

	public int getIdWithSameDiscount(int itemId) {
		System.out.println("itemId: " + itemId + ", partnerId: " + partnerId + ", date: " + date);
		// @sql:on 
		object = sql.getDatum(new Object[] {itemId, partnerId, date }, 
				SQL.addItemParentStmt() + ", "
				+ "parameter " 
				+ "     AS (SELECT cast (? AS int) AS item_id, "
				+ "                cast (? AS int) AS customer_id, "
				+ "                cast (? AS date) AS post_date), "
				+ "     latest_discount_date "
				+ "     AS (  SELECT child_id AS item_id, "
				+ "                  d.customer_id, "
				+ "                  max (start_date) AS max_date "
				+ "             FROM parent_child AS ip "
				+ "                  INNER JOIN discount AS d ON ip.parent_id = d.family_id "
				+ "                  INNER JOIN parameter AS p "
				+ "                     ON     d.customer_id = p.customer_id "
				+ "                        AND start_date <= p.post_date "
				+ "         GROUP BY child_id, "
				+ "                  d.customer_id), "
				+ "     latest_discount "
				+ "     AS (SELECT item_id, "
				+ "                CASE WHEN im.not_discounted IS TRUE THEN 0 ELSE level_1 END "
				+ "                   AS level_1, "
				+ "                CASE WHEN im.not_discounted IS TRUE THEN 0 ELSE level_2 END "
				+ "                   AS level_2 "
				+ "           FROM parent_child AS ip "
				+ "                INNER JOIN item_master AS im ON im.id = ip.child_id "
				+ "                INNER JOIN discount AS d ON ip.parent_id = d.family_id "
				+ "                INNER JOIN latest_discount_date AS ldd "
				+ "                   ON     start_date = ldd.max_date "
				+ "                      AND ldd.item_id = ip.child_id "
				+ "                      AND ldd.customer_id = d.customer_id), "
				+ "     " + type + "_order "
				+ "     AS (SELECT sd." + type + "_id, "
				+ "                CASE WHEN level_1 IS NULL THEN 0 ELSE level_1 END AS level_1, "
				+ "                CASE WHEN level_2 IS NULL THEN 0 ELSE level_2 END AS level_2 "
				+ "           FROM " + type + "_header AS sh "
				+ "                INNER JOIN " + type + "_detail AS sd ON sd." + type + "_id = sh." + type + "_id "
				+ "                INNER JOIN parameter AS p "
				+ "                   ON     p.post_date = sh." + type + "_date "
				+ "                      AND p.customer_id = sh.customer_id "
				+ "                LEFT JOIN latest_discount AS ld ON sd.item_id = ld.item_id "
				+ "          WHERE line_id = 1), "
				+ "     item_id "
				+ "     AS (SELECT CASE WHEN level_1 IS NULL THEN 0 ELSE level_1 END AS level_1, "
				+ "                CASE WHEN level_2 IS NULL THEN 0 ELSE level_2 END AS level_2 "
				+ "           FROM parameter AS p "
				+ "                LEFT JOIN latest_discount AS ld ON p.item_id = ld.item_id) "
				+ "SELECT " + type + "_id "
				+ "  FROM " + type + "_order AS so "
				+ "       INNER JOIN item_id AS ii "
				+ "          ON so.level_1 = ii.level_1 AND so.level_2 = ii.level_2; "
				);
		// @sql:off
		System.out.println("salesid: " + object);
		return object == null ? 0 : (int) object;
	}

}

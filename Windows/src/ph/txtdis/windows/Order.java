package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public abstract class Order extends Report {
	protected int partnerId, soId, leadTime;
	protected Date postDate, dueDate, encDate;
	protected Time encTime;
	protected ArrayList<Integer> itemIds, uoms;
	protected ArrayList<BigDecimal> qtys;
	protected String name, address, encoder, series;
	protected String type, reference;
	protected BigDecimal discountRate1, discountRate2, totalDiscount1, totalDiscount2;
	protected BigDecimal subTotal, totalVatable, totalVat, sumTotal;
	protected BigDecimal actual;
	private long ts;

	public Order() {
	}

	public Order(Integer orderId) {
		this(orderId, null);
	}

	public Order(Integer orderId, String series) {
		this.series = series;
		id = Math.abs(orderId);
		Calendar cal = Calendar.getInstance();
		name = "";
		address = "";
		postDate = new Date(cal.getTimeInMillis());
		itemIds = new ArrayList<>();
		uoms = new ArrayList<>();
		qtys = new ArrayList<>();
		totalVat = BigDecimal.ZERO;
		sumTotal = BigDecimal.ZERO;
		subTotal = BigDecimal.ZERO;
		actual = BigDecimal.ZERO;
		totalDiscount1 = BigDecimal.ZERO;
		totalDiscount2 = BigDecimal.ZERO;
		discountRate1 = BigDecimal.ZERO;
		discountRate2 = BigDecimal.ZERO;
		totalVatable = BigDecimal.ZERO;
		encDate = new Date(cal.getTimeInMillis());
		encTime = new Time(cal.getTimeInMillis());
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("ID", 6), "ID"},
				{StringUtils.center("PRODUCT NAME", 50), "String"},
				{StringUtils.center("UOM", 5), "String"},
				{StringUtils.center("QTY", 9), "BigDecimal"},
				{StringUtils.center("PRICE", 9), "BigDecimal"},
				{StringUtils.center("SUBTOTAL", 10), "BigDecimal"}
		};
		setOrder();
		String cteOrder = "" +
				"order_table AS ( " + 
				"	SELECT	h." + type + "_id AS order_id, " +
				(type.equals("invoice") ? "	h.series, " : "") + 
				"			h.customer_id, " +
				"			h." + type + "_date AS order_date, " +
				reference +
				"			h.user_id, " +
				"			h.time_stamp, " +
				"			d.line_id, " +
				"				CASE WHEN d.item_id < 0 THEN -1 ELSE 1 END " +
				"				* d.item_id AS " +
				"			item_id, " +
				"			d.qty, " +
				"			d.uom, " +
				"			d.qty * qp.qty AS pcs, " +
				"			qp.qty AS qty_per," +
				"				CASE WHEN d.item_id < 0 THEN true ELSE false END AS " +
				"			bo " +
				"	FROM " + type + "_header AS h " +
				"	INNER JOIN " + type + "_detail AS d " +
				"		ON h." + type + "_id = d." + type + "_id " +
				(type.equals("invoice") ? "	AND h.series = d.series " : "") + 
				"	INNER JOIN qty_per AS qp " +
				"		ON d.uom = qp.uom " +
				"			AND	CASE WHEN d.item_id < 0 THEN -1 ELSE 1 END " +
				"				* d.item_id = qp.item_id " +
				"	WHERE h." + type + "_id = ? " +
				(type.equals("invoice") ? "	AND h.series = ? " : "") + 
				""; 

		String ctePrice = "" +
				"latest_price_start_date_per_order AS ( " + 
				"	SELECT	ot.order_id, " +
				"			ot.item_id, " +
				"			p.tier_id," +
				"			ot.bo, " +
				"			max(p.start_date) AS max_date " +
				"	FROM order_table AS ot " +
				"	INNER JOIN  price AS p " +
				"	ON ot.item_id = p.item_id " +
				"	INNER JOIN item_parent AS pc " +
				"	ON ot.item_id = pc.child_id " +
				"	INNER JOIN channel_price_tier AS cpt " +
				"	ON p.tier_id = cpt.tier_id " +
				"		AND cpt.family_id = pc.parent_id " +
				"	INNER JOIN customer_master AS cm " +
				"	ON ot.customer_id = cm.id " +
				"		AND cm.type_id = cpt.channel_id " +
				"	WHERE p.start_date <= ot.order_date " +
				"	GROUP BY ot.order_id, " +
				"			ot.item_id, " +
				"			p.tier_id," +
				"			ot.bo " +
				"), prices AS ( " + 
				"	SELECT	pd.order_id, " +
				"			pd.item_id, " +
				"				CASE WHEN pd.bo THEN -1 ELSE 1 END * " +
				"				p.price AS " +
				"			price " +
				"	FROM latest_price_start_date_per_order AS pd " +
				"	INNER JOIN  price AS p " +
				"	ON pd.max_date = p.start_date " +
				"		AND pd.tier_id = p.tier_id " +
				"		And pd.item_id = p.item_id " +
				"";

		String cteVolumeDiscount = "" +
				"latest_volume_discount_start_date_per_order AS ( " + 
				"	SELECT		ot.order_id, " +
				"				ot.item_id, " +
				"				max(d.start_date) AS max_date " +
				"	FROM 		order_table AS ot " +
				"	INNER JOIN 	volume_discount AS d " +
				"	ON 			ot.item_id = d.item_id " +
				"	WHERE 		d.start_date <= ot.order_date " +
				"	GROUP BY 	ot.order_id, " +
				"				ot.item_id " +
				"), " +
				"volume_discounts AS ( " + 
				"	SELECT	dd.order_id, " +
				"			d.item_id, " +
				"			d.uom, " +
				"			d.per_qty, " +
				"			d.less " +
				"	FROM latest_volume_discount_start_date_per_order AS dd " +
				"	INNER JOIN volume_discount AS d " +
				"	ON dd.max_date = d.start_date " +
				"		AND dd.item_id = d.item_id " +
				"";
		
		Object[] parameters = (type.equals("invoice") ? 
				new Object[] {id, series} : new Object[] {id}); 

		data = new SQL().getDataArray(parameters, "" +
				"WITH " +
				cteOrder + "), " + 
				ctePrice + "), " +
				cteVolumeDiscount + ") " +
				"SELECT	" +
				"		ot.line_id, " + 									//0
				"			CASE WHEN ot.bo IS TRUE THEN -1 ELSE 1 END * " +
				"			ot.item_id AS " +
				"		item_id, " +										//1
				"		im.name, " +										//2
				"		uom.unit, " +										//3
				"		ot.qty, " +											//4
				"			(p.price * ot.qty_per * ot.qty " +
				"			- CASE WHEN less IS null THEN 0 ELSE less END " +
				"			* ROUND(ot.qty_per * ot.qty " +
				"			/ CASE WHEN d.per_qty IS null " +
				"				THEN 1 ELSE d.per_qty END,0)) " +
				"			/ ot.qty " +
				"		AS price, " +
				"			p.price * ot.qty_per * ot.qty " +
				"			- CASE WHEN less IS null THEN 0 ELSE less END " +
				"			* ROUND(ot.qty_per * ot.qty " +
				"			/ CASE WHEN d.per_qty IS null THEN 1 ELSE d.per_qty END,0) " +
				"		AS subtotal, " +
				"		im.short_id " +
				(!type.equals("sales") ? "" :
						", if.id ") +
				"FROM item_master AS im " +
				"INNER JOIN order_table AS ot " +
				"ON ot.item_id = im.id " +
				"INNER JOIN uom " +
				"ON ot.uom = uom.id " +
				(!type.equals("sales") ?  "" : "" +
						"INNER JOIN item_parent AS ip " +
						"ON ot.item_id = ip.child_id " +
						"INNER JOIN item_family as if " +
						"ON ip.parent_id = if.id " +
						"AND if.tier_id = 1 ") +
				"INNER JOIN	prices AS p " +
				"ON p.item_id = ot.item_id " +
				"LEFT OUTER JOIN volume_discounts AS d " +
				"ON ot.item_id = d.item_id " +
				"ORDER BY ot.line_id ");
		if (data != null) {
			Object[] oih = new SQL().getData(parameters, "" +
					"WITH " + 
					cteOrder + "), " + 
					ctePrice + "), " +
					cteVolumeDiscount + "), " +
					"latest_credit_term_per_order AS ( " +
					"	SELECT	ot.order_id, " +
					"			cd.customer_id, " +
					"			max(cd.start_date) AS latest_date " +
					"	FROM	credit_detail AS cd " +
					"	INNER JOIN order_table AS ot " +
					"	ON cd.customer_id = ot.customer_id " +
					"	WHERE	cd.start_date <= ot.order_date " +
					"	GROUP BY cd.customer_id," +
					"			ot.order_id " +
					"), credit_terms AS ( " +
					"	SELECT  cdd.order_id, " +
					"			cdd.customer_id, " +
					"			cd.term " +
					"	FROM credit_detail AS cd " +
					"	INNER JOIN latest_credit_term_per_order AS cdd " +
					"	ON cd.customer_id = cdd.customer_id " +
					"		AND cd.start_date = cdd.latest_date " +
					"), " +
					"latest_discount_start_date_per_order AS ( " + 
					"	SELECT	ot.order_id, " +
					"			ot.customer_id, " +
					"			d.family_id, " +
					"			max(d.start_date) AS max_date " +
					"	FROM 	order_table AS ot, " +
					"			discount AS d, " +
					"			item_master AS im " +
					"	WHERE	d.customer_id = ot.customer_id " +
					"		AND	d.start_date <= ot.order_date " +
					"	GROUP BY ot.order_id, " +
					"			ot.customer_id, " +
					"			d.family_id " +
					"), " +
					"latest_discount_per_family AS ( " + 
					"	SELECT	dd.order_id, " +
					"			dd.family_id, " +
					"			d.level_1 AS rate1, " +
					"			d.level_2 AS rate2 " +
					"	FROM latest_discount_start_date_per_order AS dd " +
					"	INNER JOIN discount AS d " +
					"	ON dd.customer_id = d.customer_id " +
					"		AND dd.max_date = d.start_date " +
					"		AND dd.family_id = d.family_id " +
					"), " +
					"leaf_family_per_customer_discount AS ( " + 
					"	SELECT	ot.order_id, " +
					"			ot.item_id, " +
					"			min(pc.parent_id) AS min_family " +
					"	FROM  order_table AS ot " +
					"	INNER JOIN latest_discount_start_date_per_order AS d " +
					"	ON ot.customer_id = d.customer_id " +
					"	INNER JOIN item_parent AS pc " +
					"	ON ot.item_id = pc.child_id " +
					"		AND d.family_id = pc.parent_id " +
					"	INNER JOIN item_master AS im" +
					"	ON ot.item_id = im.id " +
					"		AND im.not_discounted = FALSE " +
					"	GROUP BY  ot.order_id, " +
					"			ot.item_id " +
					"), " +
					"partner_discounts AS ( " + 
					"	SELECT	ot.order_id, " +
					"			ot.item_id, " +
					"			CASE WHEN rate1 IS null THEN 0 ELSE rate1 END " +
					"				AS rate1, " +
					"			CASE WHEN rate2 IS null THEN 0 ELSE rate2 END " +
					"				AS rate2 " +
					"	FROM latest_discount_per_family AS d " +
					"	INNER JOIN leaf_family_per_customer_discount AS f " +
					"	ON d.order_id = f.order_id " +
					"		AND d.family_id = f.min_family " +
					"	RIGHT OUTER JOIN order_table AS ot " +
					"	ON f.order_id = ot.order_id " +
					"		AND f.item_id = ot.item_id " +
					")" +
					"SELECT	ot.order_id, " + 							//0
					"		ot.order_date, " + 							//2
					"		c.term, " +									//3
					"		ot.customer_id, " +							//4
					"		sum	( " +			
					"				p.price * " +
					"				ot.qty_per * " +
					"				ot.qty - " +
					"				CASE " +
					"					WHEN less IS null " +
					"					THEN 0 " +
					"					ELSE less " +
					"				END * " +
					"				ROUND " +
					"				( " +
					"					ot.qty_per * " +
					"					ot.qty / " +
					"					CASE " +
					"						WHEN vd.per_qty IS null " +
					"						THEN 1 " +
					"						ELSE vd.per_qty " +
					"					END, 0 " +
					"				) " +
					"			) AS total, " +							//5
					"		sum (" +
					"				(" +
					"					p.price * " +
					"					ot.qty_per * " +
					"					ot.qty - " +
					"					CASE WHEN less IS null THEN 0 ELSE less END * " +
					"					ROUND " +
					"					( " +
					"						ot.qty_per * " +
					"						ot.qty / " +
					"						CASE " +
					"							WHEN vd.per_qty IS null " +
					"							THEN 1 " +
					"							ELSE vd.per_qty " +
					"						END, 0 " +
					"					) " +
					"				) * d.rate1/100 " +
					"			) AS total_discount1, " +				//6
					"		sum ( " +
					"				( " +
					"					( " +
					"						p.price * " +
					"						ot.qty_per * " +
					"						ot.qty - " +
					"						CASE " +
					"							WHEN less IS null " +
					"							THEN 0 " +
					"							ELSE less " +
					"						END * " +
					"						ROUND " +
					"						(" +
					"							ot.qty_per * " +
					"							ot.qty / " +
					"							CASE " +
					"								WHEN vd.per_qty IS null " +
					"								THEN 1 " +
					"								ELSE vd.per_qty " +
					"							END, 0 " +
					"						) " +
					"					) " +
					"					- " +
					"					( " +
					"						p.price * " +
					"						ot.qty_per * " +
					"						ot.qty - " +
					"						CASE " +
					"							WHEN less IS null " +
					"							THEN 0 " +
					"							ELSE less " +
					"						END * " +
					"						ROUND " +
					"						( " +
					"							ot.qty_per * " +
					"							ot.qty / " +
					"							CASE " +
					"								WHEN vd.per_qty IS null " +
					"								THEN 1 ELSE vd.per_qty " +
					"							END, 0 " +
					"						) " +
					"					) * d.rate1/100 " +
					"				) * d.rate2/100 " +
					"			) AS total_discount2, " +	//7
					"		avg(d.rate1), " +				//8
					"		avg(d.rate2), " +				//9
					"		ot.actual, " +					//10
					"		ot.ref_id, " +					//11
					"		ot.user_id, " +					//12
					(type.equals("invoice") ? "	ot.series, " : "") +  //13
					"		ot.time_stamp " +				//13 or 14
					"FROM order_table AS ot " +
					"INNER JOIN prices AS p " +
					"ON ot.item_id = p.item_id " +
					"	AND ot.order_id = p.order_id " +
					"LEFT OUTER JOIN volume_discounts AS vd " +
					"ON ot.order_id = vd.order_id " +
					"	AND ot.item_id = vd.item_id " +
					"LEFT OUTER JOIN credit_terms AS c " +
					"ON ot.order_id = c.order_id " +
					"	AND ot.customer_id = c.customer_id " +
					"LEFT OUTER JOIN partner_discounts AS d " +
					"ON ot.order_id = d.order_id " +
					"	AND ot.item_id = d.item_id " +
					"GROUP BY " +
					"		ot.order_id, " +
					"		ot.order_date, " +
					"		c.term, " +
					"		ot.customer_id, " +
					"		ot.actual, " +
					"		ot.ref_id, " +
					"		ot.user_id, " +
					(type.equals("invoice") ? "	ot.series, " : "") + 
					"		ot.time_stamp " 
					);
			id 				= oih[0] == null ? 0 : (int) oih[0];
			postDate 		= (Date) oih[1];
			leadTime 		= oih[2] == null ? 0 : (int) oih[2];
			partnerId 		= oih[3] == null ? 0 : (int) oih[3];
			address 		= new Address(partnerId).getAddress();
			sumTotal 		= oih[4] == null ? BigDecimal.ZERO : (BigDecimal) oih[4];
			totalDiscount1 	= oih[5] == null ? BigDecimal.ZERO : (BigDecimal) oih[5];
			totalDiscount2 	= oih[6] == null ? BigDecimal.ZERO : (BigDecimal) oih[6];
			discountRate1 	= oih[7] == null ? BigDecimal.ZERO : (BigDecimal) oih[7];
			discountRate2 	= oih[8] == null ? BigDecimal.ZERO : (BigDecimal) oih[8];
			actual 			= oih[9] == null ? BigDecimal.ZERO : (BigDecimal) oih[9];
			if(type.equals("purchase") || type.equals("sales"))
				soId = id;
			else
				soId		= oih[10] == null ? 0 : (int) oih[10];
			encoder 		= ((String) oih[11]).toUpperCase();
			if (type.equals("invoice")) {
				series 		= (String) oih[12];
				ts			= ((Timestamp) oih[13]).getTime();
			} else {
				ts			= ((Timestamp) oih[12]).getTime();
			}
			encDate 		= new Date(ts);
			encTime			= new Time(ts);
			sumTotal		= sumTotal.subtract(totalDiscount1).subtract(totalDiscount2);
			totalVatable 	= sumTotal
					.divide(BigDecimal.ONE.add(DIS.VAT), 10, BigDecimal.ROUND_HALF_EVEN);
			totalVat 		= sumTotal.subtract(totalVatable);
			int bo = actual.compareTo(BigDecimal.ZERO) < 0 ? -1 : 1;
			for (int i = 0; i < data.length; i++) {
				itemIds.add((int) data[i][1] * bo);
				uoms.add(new UOM((String) data[i][3]).getId());
				qtys.add((BigDecimal) data[i][4]);
			}
		} else {
			this.soId = 0;
			String strActual;
			if(type.equals("sales") || type.equals("purchase")) {
				strActual = " 0.0 AS actual, ";
			} else {
				strActual  = " actual, ";
			}
			
			Object[] ao = new SQL().getData(id, "" +
					"SELECT " + strActual +
					"		customer_id, " +
					"	" + type + "_date, " +
					"		user_id, " +
					"		time_stamp " +
					"FROM " + type + "_header  " +
					"WHERE	" + type + "_id = ? " +
					(type.equals("invoice") ? "	AND series = '" + series + "'" : "") 
					);
			if (ao != null) {
				if(ao[0] != null) actual = (BigDecimal) ao[0];
				if(ao[1] != null) partnerId = (int) ao[1];
				if(ao[2] != null) postDate = (Date) ao[2];				
				if(ao[3] != null) encoder = ((String) ao[3]).toUpperCase();				
				if(ao[4] != null) {
					ts = ((Timestamp) ao[4]).getTime();
					encDate = new Date(ts);
					encTime	= new Time(ts);
				}
			}
			if(type.equals("delivery") && actual.compareTo(BigDecimal.ZERO) < 0) {
				data = new SQL().getDataArray(id, "" +
						"SELECT dd.line_id, " +
						"		dd.item_id, " +
						"		im.name, " +								
						"		uom.unit, " +
						"		dd.qty, " +
						"		-1.0 AS price, " +
						"		-1 * qty AS subtotal " +
						"FROM	delivery_detail as dd " +
						"INNER JOIN item_master as im " +
						"ON dd.item_id = im.id " +
						"INNER JOIN uom " +
						"ON dd.uom = uom.id " +
						"WHERE dd.delivery_id = ?;");
				if(data != null) 
					sumTotal = (BigDecimal) data[0][6];
			} else {
				data = new Object[0][0];
			}
		}
	}

	protected void setOrder() {
	}

	public int getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(int partnerId) {
		this.partnerId = partnerId;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public BigDecimal getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(BigDecimal subtotal) {
		this.subTotal = subtotal;
	}

	public BigDecimal getDiscountRate1() {
		return discountRate1;
	}

	public void setDiscountRate1(BigDecimal discountRate1) {
		this.discountRate1 = discountRate1;
	}

	public BigDecimal getTotalDiscount1() {
		return totalDiscount1;
	}

	public void setTotalDiscount1(BigDecimal totalDiscount1) {
		this.totalDiscount1 = totalDiscount1;
	}

	public BigDecimal getDiscountRate2() {
		return discountRate2;
	}

	public void setDiscountRate2(BigDecimal discountRate2) {
		this.discountRate2 = discountRate2;
	}

	public BigDecimal getTotalDiscount2() {
		return totalDiscount2;
	}

	public void setTotalDiscount2(BigDecimal totalDiscount2) {
		this.totalDiscount2 = totalDiscount2;
	}

	public BigDecimal getTotalVatable() {
		return totalVatable;
	}

	public void setTotalVatable(BigDecimal totalVatable) {
		this.totalVatable = totalVatable;
	}

	public int getSoId() {
		return soId;
	}

	public void setSoId(int soId) {
		this.soId = soId;
	}

	public int getLeadTime() {
		return leadTime;
	}

	public void setLeadTime(int leadTime) {
		this.leadTime = leadTime;
	}

	public Date getPostDate() {
		return postDate;
	}

	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getEncoder() {
		return encoder;
	}

	public Date getEncDate() {
		return encDate;
	}

	public Time getEncTime() {
		return encTime;
	}

	public BigDecimal getTotalVat() {
		return totalVat;
	}

	public void setTotalVat(BigDecimal totalVat) {
		this.totalVat = totalVat;
	}

	public BigDecimal getSumTotal() {
		return sumTotal;
	}

	public void setSumTotal(BigDecimal sumTotal) {
		this.sumTotal = sumTotal;
	}

	public ArrayList<Integer> getItemIds() {
		return itemIds;
	}

	public ArrayList<Integer> getUoms() {
		return uoms;
	}

	public ArrayList<BigDecimal> getQtys() {
		return qtys;
	}

	public BigDecimal getActual() {
		return actual;
	}

	public void setActual(BigDecimal actual) {
		this.actual = actual;
	}

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}
}

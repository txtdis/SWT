package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

public class PartnerDiscount {
	
	private int customerId;
	private int itemFamilyId;
	private BigDecimal rate1, rate2;
	private Object[][] data;
	private Date date;
	
	public PartnerDiscount(int itemFamilyId, BigDecimal rate1, BigDecimal rate2,
			Date date) {
		this.itemFamilyId = itemFamilyId;
		this.rate1 = rate1;
		this.rate2 = rate2;
		this.date = date;
	}

	public PartnerDiscount(int customerId) {
		data = new SQL().getDataArray(customerId, "" +
				"SELECT	row_number() OVER(ORDER BY d.start_date, -if.id), " +
				"		-if.id, " +
				"		if.name, " +
				"		d.level_1, " +
				"		CASE WHEN d.level_2 IS null THEN 0 ELSE d.level_2 END, " +
				"		d.start_date " +
				"FROM	discount AS d " +
				"INNER JOIN item_family AS if " +
				"	ON	d.family_id = if.id " +
				"WHERE	d.customer_id = ? " +
				"ORDER BY d.start_date, -if.id "
				);
	}
	
	public PartnerDiscount(int customerId, int itemId, Date date)  {
		Object[] ao = new SQL().getData(new Object[] {itemId, customerId, date}, "" +
				"WITH " + 
				"RECURSIVE item_tree_per_id (child_id, parent_id) AS ( " + 
				"	SELECT	it.child_id, " +
				"			it.parent_id " +
				"	FROM	item_tree AS it " +
				"	WHERE	it.child_id = ? " +
				"	UNION ALL " +
				"	SELECT	item_tree_per_id.child_id, " +
				"			it.parent_id " +
				"	FROM 	item_tree it " +
				"	JOIN 	item_tree_per_id " +
				"	ON 		it.child_id = item_tree_per_id.parent_id " +
				") " +
				"SELECT		CASE WHEN d.level_1 IS NULL THEN 0 ELSE d.level_1 END AS rate1, " +
				"			CASE WHEN d.level_2 IS NULL THEN 0 ELSE d.level_2 END AS rate2 " +
				"FROM  		discount AS d, " +
				"			item_tree_per_id AS pc " +
				"WHERE		d.family_id = pc.parent_id " +
				"	AND		d.customer_id = ? " +
				"	AND		d.start_date <= ? " +
				"ORDER BY 	d.start_date DESC, " +
				"			pc.parent_id " +
				"LIMIT 1 " +
				"" 
				);
		if (ao == null) ao = new BigDecimal[2];
		rate1 = ao[0] == null ? BigDecimal.ZERO : (BigDecimal) ao[0];
		if (ao.length > 1) {
			rate2 = ao[1] == null ? BigDecimal.ZERO : (BigDecimal) ao[1];
		} else {
			rate2 = BigDecimal.ZERO;
		}
	}
	
	public Object[][] getData() {
		return data;
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}

	public int getItemFamilyId() {
		return itemFamilyId;
	}

	public void setItemFamilyId(int itemFamilyId) {
		this.itemFamilyId = itemFamilyId;
	}

	public BigDecimal getRate1() {
		return rate1;
	}

	public void setRate1(BigDecimal rate1) {
		this.rate1 = rate1;
	}

	public BigDecimal getRate2() {
		return rate2;
	}

	public void setRate2(BigDecimal rate2) {
		this.rate2 = rate2;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}

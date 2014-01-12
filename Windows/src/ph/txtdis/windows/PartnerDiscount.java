package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

public class PartnerDiscount {

	private int customerId;
	private int itemFamilyId;
	private BigDecimal firstLevel, secondLevel;
	private Object[][] data;
	private Date date;

	public PartnerDiscount(int itemFamilyId, BigDecimal firstLevel, BigDecimal secondLevel, Date date) {
		this.itemFamilyId = itemFamilyId;
		this.firstLevel = firstLevel;
		this.secondLevel = secondLevel;
		this.date = date;
	}

	public PartnerDiscount(int customerId) {
		// @sql:on
		data = new Data().getDataArray(customerId, "" 
				+ "SELECT row_number() OVER(ORDER BY d.start_date, -if.id), "
				+ "		  -if.id, "
		        + "		  if.name, " 
				+ "		  d.level_1, "
		        + "		  CASE WHEN d.level_2 IS null THEN 0 ELSE d.level_2 END, "
		        + "		  d.start_date,"
		        + "		  upper(d.user_id) " 
				+ " FROM discount AS d "
		        + "INNER JOIN item_family AS if " 
				+ "	  ON d.family_id = if.id "
		        + "WHERE d.customer_id = ? " 
				+ "ORDER BY d.start_date, "
		        + "         -if.id ");
		// @sql:off
	}

	public PartnerDiscount(int customerId, int itemId, Date date) {
		Object[] objects = new Data().getData(new Object[] { itemId, customerId, date },""
				// @sql:on
				+ SQL.addItemParentStmt()
		        + "SELECT CASE WHEN d.level_1 IS NULL "
		        + "         THEN 0 ELSE d.level_1 END AS rate1, " 
		        + "	      CASE WHEN d.level_2 IS NULL "
		        + "         THEN 0 ELSE d.level_2 END AS rate2  " 
		        + "  FROM discount AS d "
		        + " INNER JOIN parent_child AS ip " 
		        + "    ON d.family_id = ip.parent_id "
		        + " INNER JOIN item_master AS im " 
		        + "    ON ip.child_id = im.id "
		        + " WHERE     im.not_discounted IS NOT TRUE " 
		        + "       AND ip.child_id = ? "
		        + "		  AND d.customer_id = ? " 
		        + "	      AND d.start_date <= ? "
		        + "ORDER BY d.family_id, " 
		        + "			d.start_date DESC "
		        + "LIMIT 1; "
				// @sql:on
				);
			firstLevel = objects != null ? (BigDecimal) objects[0] : BigDecimal.ZERO;
			secondLevel = objects != null ? (BigDecimal) objects[1] : BigDecimal.ZERO;
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

	public BigDecimal getTotal() {
		BigDecimal netOfDiscount1 = DIS.HUNDRED.subtract(firstLevel);
		BigDecimal netOfDiscount2 = DIS.HUNDRED.subtract(secondLevel);
		BigDecimal netOfDiscounts = netOfDiscount1.multiply(DIS.getRate(netOfDiscount2));
		return DIS.HUNDRED.subtract(netOfDiscounts);
	}

	public BigDecimal getFirstLevel() {
		return firstLevel;
	}

	public void setFirstLevel(BigDecimal firstLevel) {
		this.firstLevel = firstLevel;
	}

	public BigDecimal getSecondLevel() {
		return secondLevel;
	}

	public void setSecondLevel(BigDecimal secondLevel) {
		this.secondLevel = secondLevel;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}

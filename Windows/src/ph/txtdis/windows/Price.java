package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

public class Price {
	private BigDecimal value;
	private int tierId;
	private Date date;

	public Price() {
	}

	public Price(BigDecimal value, int tierId, Date date) {
		this.value = value;
		this.tierId = tierId;
		this.date = date;
	}

	public BigDecimal getValue() {
		return value;
	}

	public int getTierId() {
		return tierId;
	}

	public Date getDate() {
		return date;
	}
	
	public BigDecimal get(int itemId, int custId, Date date)  {
		Object o =  new Data().getDatum(new Object[] {itemId, custId, date}, "" +
				"WITH " + 
				"RECURSIVE parent_child (child_id, parent_id) AS ( " + 
				"	SELECT	it.child_id, " +
				"			it.parent_id " +
				"	FROM	item_tree AS it " +
				"	UNION ALL " +
				"	SELECT	parent_child.child_id, " +
				"			it.parent_id " +
				"	FROM 	item_tree it " +
				"	JOIN 	parent_child " +
				"	ON 		it.child_id = parent_child.parent_id " +
				"), " + 
				"latest_price_start_date AS ( " + 
				"	SELECT		p.item_id, " +
				"				p.tier_id, " +
				"				max(p.start_date) AS max_date " +
				"	FROM		price AS p " +
				"	INNER JOIN 	parent_child AS pc " +
				"		ON 		p.item_id = pc.child_id " +
				"	INNER JOIN 	channel_price_tier AS cpt " +
				"		ON 		p.tier_id = cpt.tier_id " +
				"			AND cpt.family_id = pc.parent_id " +
				"	INNER JOIN 	customer_master AS cm " +
				"		ON 		cm.type_id = cpt.channel_id " +
				"	WHERE 		p.item_id = ? " +
				"		AND		cm.id = ? " +
				"		AND		p.start_date <= ? " +
				"	GROUP BY 	p.item_id, " +
				"				p.tier_id " +
				") " + 
				"SELECT			price " +
				"FROM 			latest_price_start_date AS pd " +
				"INNER JOIN  	price AS p " +
				"ON 			pd.max_date = p.start_date " +
				"	AND 		pd.tier_id = p.tier_id " +
				"	AND 		pd.item_id = p.item_id " +
				"" 
				);
		return o != null ? (BigDecimal) o : BigDecimal.ZERO;
	}
	
	
}

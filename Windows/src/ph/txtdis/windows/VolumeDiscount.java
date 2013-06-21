package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

public class VolumeDiscount {
	private BigDecimal less;
	private int perQty, uom, channelId;
	private Date date;

	public VolumeDiscount() {
	}

	public VolumeDiscount(BigDecimal less, int perQty, int uom, int channelId,
			Date date) {
		this.less = less;
		this.perQty = perQty;
		this.uom = uom;
		this.channelId = channelId;
		this.date = date;
	}

	public BigDecimal getLess() {
		return less;
	}

	public int getPerQty() {
		return perQty;
	}

	public int getUom() {
		return uom;
	}

	public int getChannelId() {
		return channelId;
	}

	public Date getDate() {
		return date;
	}
	
	public int getPerQty(int itemId, Date date)  {
		Object o = new SQL().getDatum(new Object[] {date, itemId},  "" + 
				"With t AS ( " +
				"	SELECT	item_id, " +
				"			max(start_date) AS latest_date " +
				"	FROM volume_discount " +
				"	WHERE start_date <= ? " +
				"	GROUP BY item_id " +
				") " +
				"SELECT	per_qty " +
				"FROM 	volume_discount AS vd, t " +
				"WHERE 	t.item_id = vd.item_id " +
				"	AND t.latest_date = vd.start_date " +
				"	AND vd.item_id = ? " 
				);
		return o == null ? 999_999 : (int) o;
	}

	public BigDecimal get(int itemId, Date date)  {
		return (BigDecimal) new SQL().getDatum(new Object[] {date, itemId},  "" + 
				"With t AS ( " +
				"	SELECT	item_id, " +
				"			max(start_date) AS latest_date " +
				"	FROM volume_discount " +
				"	WHERE start_date <= ? " +
				"	GROUP BY item_id " +
				") " +
				"SELECT	less " +
				"FROM 	volume_discount AS vd, t " +
				"WHERE 	t.item_id = vd.item_id " +
				"	AND t.latest_date = vd.start_date " +
				"	AND vd.item_id = ? " 
				);
	}

	public int getUom(int itemId, Date date)  {
		Object o = new SQL().getDatum(new Object[] {date, itemId},  "" + 
				"With t AS ( " +
				"	SELECT	item_id, " +
				"			max(start_date) AS latest_date " +
				"	FROM volume_discount " +
				"	WHERE start_date <= ? " +
				"	GROUP BY item_id " +
				") " +
				"SELECT	uom " +
				"FROM 	volume_discount AS vd, t " +
				"WHERE 	t.item_id = vd.item_id " +
				"	AND t.latest_date = vd.start_date " +
				"	AND vd.item_id = ? " 
				);
		return o != null ? (int) o : 0;
	}
}

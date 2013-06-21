package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

public class ItemCount {
	private int id, uom, qc;
	private BigDecimal qty;
	private Date date;
	
	public ItemCount(int id, int uom, int qc, BigDecimal qty, Date date) {
		super();
		this.id = id;
		this.uom = uom;
		this.qc = qc;
		this.qty = qty;
		this.date = date;
	}

	public int getId() {
		return id;
	}

	public int getUom() {
		return uom;
	}

	public int getQc() {
		return qc;
	}

	public BigDecimal getQty() {
		return qty;
	}

	public Date getDate() {
		return date;
	}
}

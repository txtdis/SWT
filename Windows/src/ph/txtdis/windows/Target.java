package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.Arrays;

public class Target {
	private int id, outletId, productLineId;
	private BigDecimal qty;
	private String type;

	public Target() {
	}
	
	public Target(String name) {
		id = (int) new Query().getDatum(name, "" +
				"SELECT	id " +
				"FROM	target_type " +
				"WHERE	name = ? "
				);
	}

	public Target(int id) {
		type = (String) new Query().getDatum(id, "" +
				"SELECT	name " +
				"FROM	target_type " +
				"WHERE	id = ? "
				);
	}

	public Target(int outletId, int productLineId, BigDecimal qty) {
		this.outletId = outletId;
		this.productLineId = productLineId;
		this.qty = qty;
	}

	public int getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public int getOutletId() {
		return outletId;
	}

	public void setOutletId(int outletId) {
		this.outletId = outletId;
	}

	public int getProductLineId() {
		return productLineId;
	}

	public void setProductLineId(int productLineId) {
		this.productLineId = productLineId;
	}

	public BigDecimal getQty() {
		return qty;
	}

	public void setQty(BigDecimal qty) {
		this.qty = qty;
	}

	public String[] getTargets() {
		Object[] objects = new Query().getList("" +
				"SELECT	name " +
				"FROM	target_type " + 
				"ORDER BY name " +
				"");
		return Arrays.copyOf(objects, objects.length, String[].class);		
	}	
}

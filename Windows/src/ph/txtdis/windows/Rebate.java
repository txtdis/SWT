package ph.txtdis.windows;

import java.math.BigDecimal;

public class Rebate {
	private int productLineId;
	private BigDecimal value;

	public Rebate() {
	}

	public Rebate(int productLineId, BigDecimal value) {
		this.productLineId = productLineId;
		this.value = value;
	}

	public int getProductLineId() {
		return productLineId;
	}

	public void setProductLineId(int productLineId) {
		this.productLineId = productLineId;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}
}

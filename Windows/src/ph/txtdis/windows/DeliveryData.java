package ph.txtdis.windows;

import java.math.BigDecimal;

public class DeliveryData extends OrderData {

	protected BigDecimal enteredTotal;
	protected int referenceId;

	public DeliveryData(int id) {
		super(id);
	}

	@Override
    protected void setProperties() {
		type = Type.DELIVERY;
    }

	public void setEnteredTotal(BigDecimal enteredTotal) {
		this.enteredTotal = enteredTotal;
	}

	public int getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(int referenceId) {
		this.referenceId = referenceId;
	}
}

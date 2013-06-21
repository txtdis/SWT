package ph.txtdis.windows;

public class DeliveryPosting extends OrderPosting {

	public DeliveryPosting() {
		super();
	}

	@Override
	protected void setType() {
		type = "delivery";
	}
}

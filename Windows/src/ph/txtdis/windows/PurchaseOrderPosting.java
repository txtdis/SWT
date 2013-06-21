package ph.txtdis.windows;

public class PurchaseOrderPosting extends SalesOrderPosting {

	public PurchaseOrderPosting() {
		super();
	}

	@Override
	protected void setType() {
		type = "purchase";
	}
}

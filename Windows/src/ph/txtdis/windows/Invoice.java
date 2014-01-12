package ph.txtdis.windows;

public class Invoice extends Order implements Startable {

	public Invoice() {}

	public Invoice(int orderId) {
		super(orderId);
	}

	public Invoice(int orderId, String series) {
		super(orderId, series);
	}

	@Override
	protected void setData() {
		module = "Invoice";
		type = "invoice";
		referenceAndActualStmt = " h.actual, h.ref_id,\n";
	}

	@Override
    public void start() {
		new InvoiceView(0);
    }
}

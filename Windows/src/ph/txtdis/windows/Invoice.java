package ph.txtdis.windows;

public class Invoice extends Order {

	public Invoice() {
		super();
	}

	public Invoice(int orderId) {
		super(orderId);
	}

	public Invoice(int orderId, String series) {
		super(orderId, series);
	}

	@Override
	protected void setOrder() {
		module = "Invoice";
		type = "invoice";
		reference = "" +
				" h.actual, " +
				" h.ref_id, " +
				"";
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Invoice i = new Invoice(48136, "B");
		if(i.getData() !=null) {
			for (Object[] os : i.getData()) {
				for (Object o : os) {
					System.out.print(o + ", ");
				}
				System.out.println();
			}
		} else {
			System.out.println("No data");
		}
		Database.getInstance().closeConnection();
	}
}

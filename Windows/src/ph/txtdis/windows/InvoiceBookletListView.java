package ph.txtdis.windows;

public class InvoiceBookletListView extends ListView {

	public InvoiceBookletListView() {
		this("");
	}

	public InvoiceBookletListView(String text) {
		this(new InvoiceBookletList(text));
	}

	public InvoiceBookletListView(Data data) {
		super(data);
		type = Type.INVOICE_BOOKLET_LIST;
		display();
	}
}

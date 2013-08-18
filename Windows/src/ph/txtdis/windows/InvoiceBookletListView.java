package ph.txtdis.windows;

public class InvoiceBookletListView extends ListView {
	public InvoiceBookletListView(String string) {
		super(string);
	}

	@Override
	protected void runClass() {
		report = new InvoiceBookletList(string);
	}
	
	@Override
	protected void setFocus() {
		addButton.setFocus();
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin","localhost");
		new InvoiceBookletListView("");
		Database.getInstance().closeConnection();
	}
}

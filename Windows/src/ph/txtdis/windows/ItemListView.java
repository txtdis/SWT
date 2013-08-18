package ph.txtdis.windows;

public class ItemListView extends ListView {
	public ItemListView(String string) {
		super(string);
	}

	@Override
	protected void runClass() {
		report = new ItemList(string);
	}
	
	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin","localhost");
		new ItemListView("");
		Database.getInstance().closeConnection();
	}
}

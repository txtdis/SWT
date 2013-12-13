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
		//Database.getInstance().getConnection("irene","ayin","localhost");
		Database.getInstance().getConnection("sheryl", "10-8-91", "192.168.1.100");
		new ItemListView("");
		Database.getInstance().closeConnection();
	}
}

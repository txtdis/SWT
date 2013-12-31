package ph.txtdis.windows;

public class InventoryView extends ListView {

	public InventoryView(String itemName) {
		super(itemName);
	}

	@Override
	protected void runClass() {
		report = new Inventory(string);
	}
	
	@Override
	protected void setTitleBar() {
		new InventoryTitleBar(this, (Inventory) report);
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("badette","013094","magnum_sta_maria_42");
		new InventoryView("");
		Database.getInstance().closeConnection();
	}
}

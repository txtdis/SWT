package ph.txtdis.windows;

public class InventoryView extends ReportView {

	public InventoryView() {
		this("");
	}
	
	public InventoryView(String string) {
	    this(new Inventory(string));
    }

	public InventoryView(Inventory inventory) {
		super(inventory);
		type = Type.INVENTORY;
	    addHeader();
		addTable();
		show();
	}
	
	@Override
	protected void addHeader() {
		new Header(this, data) {
			@Override
            protected void layButtons() {
				new SearchButton(buttons, type);
				new ReportGenerationButton(buttons, data);
				new InventoryImportButton(buttons, module);
				new ImgButton(buttons, Type.EXCEL, view);
            }
		};
	}
}

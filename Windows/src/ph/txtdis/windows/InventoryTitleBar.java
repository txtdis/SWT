package ph.txtdis.windows;

public class InventoryTitleBar extends ListTitleBar{
	
	public InventoryTitleBar(InventoryView view, Inventory report) {
		super(view, report);
	}

	@Override
	protected void layButtons() {
		new SearchButton(buttons, module);
		new ReportGenerationButton(buttons, report);
		new InventoryImportButton(buttons, module);
		new ExcelButton(buttons, report);
		new ExitButton(buttons, module);
	}
}

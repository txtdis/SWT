package ph.txtdis.windows;

public class PricelistBar extends ListTitleBar {

	public PricelistBar(PricelistView view, Pricelist report) {
		super(view, report);
		new PricelistImportButton(buttons, module);
		new ExcelButton(buttons, report);
		new ExitButton(buttons, module);
	}
}

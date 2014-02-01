package ph.txtdis.windows;

public class PricelistView extends ReportView {
	private Pricelist pricelist;
		
	public PricelistView() {
		super(new Pricelist());
		type = Type.PRICE_LIST;
		addHeader();
		addTable();
		show();
	}
	
	@Override
	protected void addHeader() {
		new Header(this, pricelist) {
			@Override
            protected void layButtons() {
				new PricelistImportButton(buttons, module);
				new ImgButton(buttons, Type.EXCEL, view);
            }
		};
	}
}
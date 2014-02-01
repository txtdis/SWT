package ph.txtdis.windows;

public class SalesTargetListView extends ReportView {

	public SalesTargetListView() {
		super();
		type = Type.SALES_TARGET_LIST;
		data = new SalesTargetList();
		addHeader();
		addTable();
		show();
	}

	@Override
	protected void addHeader() {
		new Header(this, data) {
			@Override
			protected void layButtons() {
				new AddButton(buttons, module);
				new ImgButton(buttons, Type.EXCEL, view);
			}
		};
	}
}

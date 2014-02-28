package ph.txtdis.windows;

public abstract class ListView extends ReportView  {
	public ListView(Data data) {
		super(data);
		this.data = data;
	}

	protected void display() {
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
				new ImgButton(buttons, Type.ADD, ((Listed) data).getListedType());
				new ImgButton(buttons, Type.EXCEL, view);
            }
		};
	}
}

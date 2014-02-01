package ph.txtdis.windows;

public class SettlementView extends ReportView implements Subheaderable {

	public SettlementView() {
		new SettlementView(new LoadSettlement(null, 1));
	}
		
	public SettlementView(Data data) {
		super(data);
		type = data.getType();
		addHeader();
		addSubheader();
		addTable();
		addTotalBar();
		show();
	}
	
	
	@Override
	protected void addHeader() {
		new Header(this, data) {
			@Override
            protected void layButtons() {
				new OptionButton(buttons, data);
				new CalendarButton(buttons, data);
				new BackwardButton(buttons, data);
				new ForwardButton(buttons, data);
				new ImgButton(buttons, Type.EXCEL, view);
            }
		};
	}
	
	@Override
    public void addSubheader() {
		new Subheading(shell, (Subheaded) data);
    }
}
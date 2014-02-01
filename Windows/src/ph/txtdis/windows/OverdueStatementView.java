package ph.txtdis.windows;

public class OverdueStatementView extends ReportView implements Subheaderable {
	
	public OverdueStatementView(int customerId) {
		super(new OverdueStatement(customerId));
		type = Type.OVERDUE;
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
				new ImgButton(buttons, Type.EXCEL, view).getButton();
			}
		};
    }

	@Override
    public void addSubheader() {
		new Subheading(shell, (Subheaded) data);
	}
}

package ph.txtdis.windows;


public class ReceivablesView extends ReportView {

	public ReceivablesView() {
		super(new Receivables());
		type = Type.RECEIVABLES;
		proceed();
		show();
	}

	@Override
    protected void proceed() {
		super.proceed();
		addTotalBar();
    }

	@Override
    protected void addHeader() {
		new Header(this, data) {
			@Override
            protected void layButtons() {
				new SearchButton(buttons, type);
				new ReportButton(buttons, data, "Database", "Dump aging receivable data\nto a spreadsheet") {
					@Override
                    protected void proceed() {
						Object[][] data = new Overdue().getDataDump();
						String[] header = new String[] {
						        "ID", "OUTLET", "SI/(DR)", "SERIES", "POST DATE", "DUE DATE", "DAYS OVER", "BALANCE" };
						new ExcelWriter(header, data);
                    }
				};
				new ImgButton(buttons, Type.EXCEL, view);
            }
		};
    }
}

package ph.txtdis.windows;

public class ReceivablesView extends ReportView {

	public ReceivablesView() {
		super();
		setProgress();
		setTitleBar();
		setHeader();
		getTable();
		setTotalBar();
		setFooter();
		setListener();
		setFocus();
		showReport();
	}

	@Override
	protected void runClass() {
		report = new Receivables();
	}

	@Override
    protected void setTitleBar() {
		new ReportTitleBar(this, report) {
			@Override
            protected void layButtons() {
				new SearchButton(buttons, module);
				new ReportButton(buttons, report, "Database", "Dump aging receivable data\nto a spreadsheet") {
					@Override
					protected void doWithProgressMonitorWhenSelected() {
						Object[][] data = new Overdue().getDataDump();
						String[] header = new String[] {
						        "ID", "OUTLET", "SI/(DR)", "SERIES", "POST DATE", "DUE DATE", "DAYS OVER", "BALANCE" };
						new ExcelWriter(header, data);
					}
				};
				new ExcelButton(buttons, report);
				new ExitButton(buttons, module);
            }
		};
    }

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene", "ayin", "localhost");
		new ReceivablesView();
		Database.getInstance().closeConnection();
	}
}

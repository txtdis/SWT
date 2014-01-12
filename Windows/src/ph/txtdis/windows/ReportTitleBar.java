package ph.txtdis.windows;

public class ReportTitleBar extends ModuleTitleBar{
	protected ReportView reportView;
	protected Report report;
	
	public ReportTitleBar(ReportView view, Report report) {
		super(view, report.getModule());
		this.reportView = view;
		this.report = report;
		layButtons();
	}

	@Override
	protected void layButtons() {
		new SearchButton(buttons, module);
		new ExcelButton(buttons, report);
	}
}

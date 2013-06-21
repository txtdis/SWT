package ph.txtdis.windows;


public class ReceivablesView extends ReportView {

	public ReceivablesView() {
		super();
		setProgress();
		setTitleBar();
		setHeader();
		setTableBar();
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
		new ReportTitleBar(this, report);
	}

}

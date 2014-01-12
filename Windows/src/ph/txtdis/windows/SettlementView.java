package ph.txtdis.windows;

public class SettlementView extends ReportView {
		
	public SettlementView(Report report) {
		this.report = report;
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
	protected void setTitleBar() {
		new FilterTitleBar(this, report);
	}
	
	@Override
    protected void setHeader() {
		new ReportHeaderBar(shell, report);
    }
	
	public static void main(String[] args) {
		Database.getInstance().getConnection("badette","013094","mgdc_smis");
		//new SettlementView(new LoadSettlement(null, 0));
		new SettlementView(new CashSettlement(null, 0));
		Database.getInstance().closeConnection();
	}
}
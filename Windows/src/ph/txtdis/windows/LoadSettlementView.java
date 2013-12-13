package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

public class LoadSettlementView extends ReportView {
	private LoadSettlement loadBalance;
	private int routeId;
	private Date[] dates;
		
	public LoadSettlementView(Date[] dates, int routeId) {
		this.routeId = routeId;
		if (dates == null) 
			dates = new Date[] {DIS.TODAY, DIS.TODAY };
		this.dates = dates;
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
		report = loadBalance = new LoadSettlement(dates, routeId);
	}
	
	@Override
	protected void setTitleBar() {
		new FilterTitleBar(this, loadBalance);
	}
	
	@Override
    protected void setHeader() {
		new ReportHeaderBar(shell, loadBalance);
    }
	
	public static void main(String[] args) {
		//Database.getInstance().getConnection("irene","ayin","localhost");
		Database.getInstance().getConnection("irene","ayin","192.168.1.100");
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.DECEMBER, 4);
		new LoadSettlementView(null, 0);
		Database.getInstance().closeConnection();
	}
}
package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

public class LoadedMaterialBalanceView extends ReportView {
	private LoadedMaterialBalance loadBalance;
	private int routeId;
	private Date[] dates;
		
	public LoadedMaterialBalanceView(Date[] dates, int routeId) {
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
		report = loadBalance = new LoadedMaterialBalance(dates, routeId);
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
		Database.getInstance().getConnection("irene","ayin","localhost");
		//Database.getInstance().getConnection("irene","ayin","192.168.1.100");
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.AUGUST, 8);
		new LoadedMaterialBalanceView(null, 0);
		Database.getInstance().closeConnection();
	}
}
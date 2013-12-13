package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

public class CashSettlementView extends ReportView {
	private CashSettlement settlement;
	private int routeId;
	private Date[] dates;
		
	public CashSettlementView(Date[] dates, int routeId) {
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
		report = settlement = new CashSettlement(dates, routeId);
	}
	
	@Override
	protected void setTitleBar() {
		new FilterTitleBar(this, settlement);
	}
	
	@Override
    protected void setHeader() {
		new ReportHeaderBar(shell, settlement);
    }
	
	public static void main(String[] args) {
		Database.getInstance().getConnection("badette", "013094", "192.168.1.100");
		//Database.getInstance().getConnection("badette","013094","localhost");
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.DECEMBER, 4);
		new CashSettlementView(null, 0);
		Database.getInstance().closeConnection();
	}
}
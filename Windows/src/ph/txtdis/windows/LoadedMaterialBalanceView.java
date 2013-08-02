package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

public class LoadedMaterialBalanceView extends ReportView {
	private LoadedMaterialBalance loadBal;
	private int routeId;
	private Date[] dates;
		
	public LoadedMaterialBalanceView(Date[] dates, int routeId) {
		this.routeId = routeId;
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
		report = loadBal = new LoadedMaterialBalance(dates, routeId);
	}
	
	@Override
	protected void setTitleBar() {
		new FilterTitleBar(this, loadBal);
	}
	
	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.JULY, 1);
		Date first = new Date(cal.getTimeInMillis());
		Date last = first;
		new LoadedMaterialBalanceView(new Date[] {
				first, last }, 2);
		Database.getInstance().closeConnection();
	}
}
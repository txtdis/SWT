package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

public class StockTakeListView extends ReportView {
	private Date date;
	private int itemId;

	public StockTakeListView(Date date, int itemId) {
		this.date = date;
		this.itemId = itemId;
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
		report = new StockTakeList(date, itemId);
	}

	@Override
	protected void setTitleBar() {
		new ReportTitleBar(this, report);
	}
 
	@Override
	protected void setHeader() {
		new ReportHeaderBar(shell, report);
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.MAY, 4);
		Date date = new Date(cal.getTimeInMillis());
		new StockTakeListView(date, 248);
		Database.getInstance().closeConnection();
	}
}

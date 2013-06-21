package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

public class ReceivingListView extends ReportView {
	private Date[] dates;
	private int row;
	private Integer col;

	public ReceivingListView(Date[] dates, int row, Integer col) {
		this.dates = dates;
		this.row = row;
		this.col = col;
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
		report = new ReceivingList(dates, row, col);
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
		Date[] dates = new Date[2];
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.MAY, 4);
		dates[0] = new Date(cal.getTimeInMillis());
		cal.set(2013, Calendar.MAY, 11);
		dates[1]= new Date(cal.getTimeInMillis());
		new ReceivingListView(dates, 248, 1);
		Database.getInstance().closeConnection();
	}
}

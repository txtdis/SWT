package ph.txtdis.windows;

import java.sql.Date;

public class SalesOrderListView extends ReportView {
	private Date[] dates;
	private int row, col;

	public SalesOrderListView(Date[] dates, int row, int col) {
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
		report = new SalesOrderList(dates, row, col);
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
		new SalesOrderListView(null, 3, 4);
		Database.getInstance().closeConnection();
	}
}

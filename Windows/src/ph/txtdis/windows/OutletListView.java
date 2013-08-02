package ph.txtdis.windows;

import java.sql.Date;

public class OutletListView extends ReportView {
	private Date[] dates;
	private int row, col, cat;

	public OutletListView(Date[] dates, int row, int col, int cat) {
		this.dates = dates;
		this.row = row;
		this.col = col;
		this.cat = cat;
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
		report = new OutletList(dates, row, col, cat);
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
		new OutletListView(null, 3, 4, -10);
		Database.getInstance().closeConnection();
	}
}

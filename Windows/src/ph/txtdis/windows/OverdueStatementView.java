package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

public class OverdueStatementView extends ReportView {
	protected int customerId;
	protected Date startDate;
	
	public OverdueStatementView(int customerId) {
		this(customerId, null);
	}

	public OverdueStatementView(int customerId, Date startDate) {
		this.customerId = customerId;
		this.startDate = startDate;
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
		report = new OverdueStatement(customerId, startDate);
	}
	
	@Override
	protected void setHeader() {
		new ReportHeaderBar(shell, report);
	}
	
	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.MARCH, 1);
		new OverdueStatementView(1, new Date(cal.getTimeInMillis()));
		Database.getInstance().closeConnection();
	}
}

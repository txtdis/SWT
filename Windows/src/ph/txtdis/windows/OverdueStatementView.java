package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

public class OverdueStatementView extends ReportView {
	protected int customerId;
	protected Date startDate;
	
	public OverdueStatementView(int customerId) {
		this(customerId, DIS.FAR_PAST);
	}

	public OverdueStatementView(int customerId, Date startDate) {
		this.customerId = customerId;
		this.startDate = startDate;
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
		report = new OverdueStatement(customerId);
	}
	
	@Override
	protected void setHeader() {
		new ReportHeaderBar(shell, report);
	}
	
	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin","localhost");
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.MARCH, 1);
		new OverdueStatementView(22);
		Database.getInstance().closeConnection();
	}
}

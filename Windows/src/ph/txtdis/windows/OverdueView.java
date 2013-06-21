package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

public class OverdueView extends OverdueStatementView {
	public OverdueView(int customerId) {
		this(customerId, null);
	}
	public OverdueView(int customerId, Date startDate) {
		super(customerId, startDate);
	}
	@Override
	protected void runClass() {
		report = new Overdue(customerId, startDate);
	}
	@Override
	protected void setTitleBar() {
		new ReportTitleBar(this, report) {
			@Override
			protected void layButtons() {
				new ImageButton(buttons, module, "Mobile", "Send text to request for" +
								"\nhold status override"){
					@Override
					protected void open() {
						System.err.println("Sent a text message.");
					}					
				};
				
				new PrintingButton(buttons, report, true) {
					@Override
					public void open() {
						new OverduePrinting(report);
					}
				};
				
				new ExitButton(buttons, module);
			}			
		};
	}
	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.MARCH, 1);
		new OverdueView(90, new Date(cal.getTimeInMillis()));
		Database.getInstance().closeConnection();
	}
}

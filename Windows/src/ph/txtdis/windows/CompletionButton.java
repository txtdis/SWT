package ph.txtdis.windows;

import java.sql.Date;
import java.sql.SQLException;

import org.eclipse.swt.widgets.Composite;

public class CompletionButton extends ReportButton {
	private StockTake stockTake;
	private Date date; 

	public CompletionButton(Composite parent, Report report) {
		super(parent, report, "Stop", "Comfirm Task Completion");
	}

	@Override
	protected void doWhenSelected() {
		new WarningDialog("" + "Tagging this date as complete\nmeans no more count input.\n\nAre you sure?") {

			@Override
			protected void setOkButtonAction() {
				stockTake = (StockTake) report;
				date = stockTake.getDate();
				boolean isCompleted = new Posting(stockTake) {
					@Override
					protected void postData() throws SQLException {
						ps = conn.prepareStatement("" 
								//  @sql:on
								+ "INSERT INTO count_completion " 
								+ "	(count_date) VALUES (?); " 
								//  @sql:off
						        );
						ps.setDate(1, date);
						ps.executeUpdate();
					}
				}.wasCompleted();
				
				if (isCompleted) {
					parent.getShell().dispose();
					new StockTakeView(date);
				}
			}

		};
	}

}

package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.Shell;

public class CountReportSelectedDateAction {

	public CountReportSelectedDateAction(Shell shell, CountData data) {
	    Date[] dates = new Date[] { data.getDate() };
		Date date = new CalendarDialog(dates).getDate();
		if (Count.isDone(date)) {
			shell.close();
			new CountReportView(date);
			return;
		}
		new ErrorDialog("No stock take on\n" + date);
    }
}
